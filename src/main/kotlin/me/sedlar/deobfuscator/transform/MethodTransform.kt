package me.sedlar.deobfuscator.transform

import me.sedlar.asm.methodList
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
abstract class MethodTransform(subtransform: Boolean = false): ClassTransform(subtransform) {

    abstract fun transform(method: MethodNode)

    override fun transform(classes: Map<String, ClassNode>) {
        classes.values.forEach {
            it.methodList.forEach {
                transform(it)
            }
        }
    }
}