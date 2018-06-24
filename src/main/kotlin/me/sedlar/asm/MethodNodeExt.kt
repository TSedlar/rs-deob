package me.sedlar.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
val METHOD_OWNERS = HashMap<Int, ClassNode>()

val MethodNode.hash: Int
    get() = System.identityHashCode(this)

val MethodNode.owner: ClassNode
    get() = METHOD_OWNERS[this.hash] ?: error("method has not been patched")

val MethodNode.key: String
    get() = "${owner.name}.${this.name}${this.desc}"

fun MethodNode.isLocal() = (this.access and Opcodes.ACC_STATIC) == 0

fun MethodNode.patch(owner: ClassNode) {
    METHOD_OWNERS[this.hash] = owner
}

fun MethodNode.visitCalls(classes: Map<String, ClassNode>): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    this.accept(object: MethodVisitor(Opcodes.ASM6) {
        override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
            if (owner in classes) {
                calls.addAll(classes.findMethodTree(owner, name, desc))
            }
        }
    })
    return calls
}

fun MethodNode.visitSuperCalls(classes: Map<String, ClassNode>): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    calls.addAll(classes.findMethodTree(owner.name, name, desc))
    calls.remove(this)
    return calls
}