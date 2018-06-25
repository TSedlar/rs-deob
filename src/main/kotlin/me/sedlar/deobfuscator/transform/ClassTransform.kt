package me.sedlar.deobfuscator.transform

import org.objectweb.asm.tree.ClassNode
import kotlin.system.measureNanoTime

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
abstract class ClassTransform(private var subtransform: Boolean = false) {

    abstract fun output(): String

    abstract fun transform(classes: Map<String, ClassNode>)

    open fun end() {}

    fun apply(classes: Map<String, ClassNode>) {
        measureNanoTime {
            transform(classes)
            end()
        }.let {
            if (!subtransform) {
                System.out.printf("[%s] %s (%.02fs)\n", javaClass.simpleName, output(), it / 1e9)
            }
        }
    }
}