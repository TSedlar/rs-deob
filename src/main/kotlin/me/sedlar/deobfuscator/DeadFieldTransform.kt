package me.sedlar.deobfuscator

import me.sedlar.asm.findFieldTree
import me.sedlar.asm.visitMethods
import me.sedlar.deobfuscator.transform.ClassTransform
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
class DeadFieldTransform: ClassTransform() {

    private val visited: MutableSet<String> = HashSet()
    private var removed = 0

    override fun output(): String {
        val total = visited.size
        return "Removed $removed/$total fields -> ${total - removed}"
    }

    override fun transform(classes: Map<String, ClassNode>) {
        classes.visitMethods(object: MethodVisitor(Opcodes.ASM6) {
            override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
                visited.addAll(classes.findFieldTree(owner, name, desc))
            }
        })
        classes.values.forEach { cls ->
            val size = cls.fields.size
            cls.fields.removeIf {
                it as FieldNode
                "${cls.name}.${it.name}" !in visited
            }
            removed += (size - cls.fields.size)
        }
    }
}