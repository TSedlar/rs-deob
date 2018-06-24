package me.sedlar.deobfuscator.transform

import org.objectweb.asm.tree.ClassNode
import kotlin.system.measureNanoTime

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
abstract class ClassTransform {

    abstract fun transform(classes: Map<String, ClassNode>)

    abstract fun output(): String

    fun apply(classes: Map<String, ClassNode>) {
        measureNanoTime {
            transform(classes)
        }.let {
            System.out.printf("[%s] %s (%.02fs)\n", javaClass.simpleName, output(), it / 1e9)
        }
    }
}