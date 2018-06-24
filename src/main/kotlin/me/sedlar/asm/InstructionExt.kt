package me.sedlar.asm

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.util.Printer

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
val AbstractInsnNode.opname: String
    get() = try {
        Printer.OPCODES[this.opcode]
    } catch (e: Exception) {
        this.javaClass.simpleName
    }

fun AbstractInsnNode.hasNext(amount: Int): Boolean {
    var insn: AbstractInsnNode? = this
    for (i in 0..amount) {
        insn = insn!!.next
        if (insn == null) {
            return false
        }
    }
    return true
}

fun AbstractInsnNode.nextPattern(vararg opcodes: Int): List<AbstractInsnNode>? {
    if (hasNext(opcodes.size)) {
        val insns: MutableList<AbstractInsnNode> = ArrayList()
        var insn = this
        for (i in 0..opcodes.size) {
            insn = insn.next
            if (insn.opcode != opcodes[i]) {
                return null
            }
            insns.add(insn)
        }
        return insns
    }
    return null
}

fun AbstractInsnNode.nextValid(): AbstractInsnNode? {
    var next: AbstractInsnNode? = this.next
    while (next != null && next is LabelNode) {
        next = next.next
    }
    return next
}

fun AbstractInsnNode.nextValidPattern(vararg opcodes: Int): List<AbstractInsnNode>? {
    val results: MutableList<AbstractInsnNode> = ArrayList()
    var current = this
    for (i in opcodes.indices) {
        current = current.nextValid() ?: return null
        results.add(current)
    }
    return results
}