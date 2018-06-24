package me.sedlar.deobfuscator

import me.sedlar.asm.nextPattern
import me.sedlar.asm.tryCatchBlockList
import me.sedlar.deobfuscator.transform.MethodTransform
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode
import java.util.*

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
class TryCatchTransform: MethodTransform() {

    private var removed = 0

    override fun output(): String {
        return "Removed $removed TryCatchBlocks"
    }

    override fun transform(method: MethodNode) {
        val blocks = method.tryCatchBlockList
        if (!blocks.isEmpty()) {
            val dead: MutableList<TryCatchBlockNode> = ArrayList()
            blocks.forEach { block ->
                if (block.type == null) {
                    dead.add(block)
                    removed++
                } else if (block.type.equals("java/lang/RuntimeException")) {
                    val current = block.handler.next
                    if (current != null && current.opcode == NEW) {
                        current.nextPattern(
                                DUP, INVOKESPECIAL, LDC, INVOKEVIRTUAL, LDC,
                                INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKESTATIC, ATHROW
                        )?.let {
                            method.instructions.remove(current)
                            it.forEach { method.instructions.remove(it) }
                            dead.add(block)
                            removed++
                        }
                    }
                }
            }
            blocks.removeAll(dead)
        }
    }
}