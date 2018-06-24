package me.sedlar.deobfuscator

import me.sedlar.asm.*
import me.sedlar.deobfuscator.transform.ClassTransform
import me.sedlar.deobfuscator.transform.MethodTransform
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.SimpleRemapper
import org.objectweb.asm.tree.*

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
class OpaqueTransform : ClassTransform() {

    var removed = 0
    var renames: MutableMap<String, String> = HashMap()

    override fun output(): String {
        return "Removed $removed opaque predicates"
    }

    override fun transform(classes: Map<String, ClassNode>) {
        createTransform(classes).apply(classes)
        val classMapper = SimpleRemapper(renames)
        keepIfUnremoved(classes)
        renames.clear() // renaming currently does not work on R171 - Illegal method name "client.en()V" in class client
        removeInvokes(classes)
        (classes as MutableMap<String, ClassNode>).remap(classMapper)
    }

    private fun createTransform(classes: Map<String, ClassNode>):
            MethodTransform = object : MethodTransform(true) {
        override fun transform(method: MethodNode) {
            val parameters = method.paramSize
            if (parameters == 0 || !method.desc.contains("I)") && !method.desc.contains("B)") &&
                    !method.desc.contains("S)")) {
                return
            }
            val loadVar = if (method.access and ACC_STATIC > 0) parameters - 1 else parameters

            method.instructions.toArray().forEach { variable ->
                if (variable is VarInsnNode && variable.`var` == loadVar) {
                    var proceed = false
                    val deadInsns: MutableList<AbstractInsnNode> = ArrayList()
                    deadInsns.add(variable)
                    variable.nextValid()?.let {
                        if (it !is JumpInsnNode) {
                            deadInsns.add(it)
                        }
                        it.nextValid()?.let {
                            if (it.opcode != GOTO && it is JumpInsnNode) {
                                val jump = it
                                deadInsns.add(it)
                                it.nextValid()?.let {
                                    if (it.opcode == RETURN) {
                                        proceed = deadInsns.add(it)
                                    } else if (it is TypeInsnNode && isError(it)) {
                                        val name = it.desc
                                        deadInsns.add(it)
                                        it.nextValidPattern(DUP, INVOKESPECIAL, ATHROW)?.let {
                                            proceed = (it[1] as MethodInsnNode).owner == name && deadInsns.addAll(it)
                                        }
                                    }
                                }
                                if (proceed) {
                                    method.instructions.insert(jump, JumpInsnNode(GOTO, jump.label))
                                    deadInsns.stream()
                                            .filter { insn -> insn != null }
                                            .forEach { insn -> method.instructions.remove(insn) }
                                    classes.findMethodTree(method.owner.name, method.name, method.desc).forEach {
                                        var newKey = "${it.owner.name}.${it.name}("
                                        Type.getArgumentTypes(it.desc).dropLast(1).forEach {
                                            newKey += it.descriptor
                                        }
                                        newKey += ")${Type.getReturnType(it.desc).descriptor}"
                                        renames[it.key] = newKey
                                    }
                                    removed++
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun output(): String = ""
    }

    private fun isError(tin: TypeInsnNode): Boolean {
        return tin.desc == "java/lang/IllegalStateException"
    }

    private fun keepIfUnremoved(classes: Map<String, ClassNode>) {
        classes.values.forEach {
            it.methodList.forEach {
                val parameters = it.paramSize
                val loadVar = if (it.access and ACC_STATIC > 0) parameters - 1 else parameters
                it.instructions.toArray().forEach { variable ->
                    if (variable is VarInsnNode && variable.`var` == loadVar) {
                        classes.findMethodTree(it.owner.name, it.name, it.desc).forEach {
                            renames.remove(it.key)
                        }
                    }
                }
            }
        }
    }

    private fun removeInvokes(classes: Map<String, ClassNode>) {
        classes.values.forEach {
            it.methodList.forEach { method ->
                val deadVars: MutableList<AbstractInsnNode> = ArrayList()
                method.instructions.toArray().forEach {
                    if (it is MethodInsnNode) {
                        val key = "${it.owner}.${it.name}${it.desc}"
                        if (key in renames) {
                            deadVars.add(it.previous)
                        }
                    }
                }
                deadVars.forEach { method.instructions.remove(it) }
            }
        }
    }
}