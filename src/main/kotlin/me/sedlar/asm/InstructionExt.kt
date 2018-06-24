package me.sedlar.asm

import org.objectweb.asm.tree.AbstractInsnNode

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
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