package me.sedlar.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
fun Map<String, ClassNode>.export(jar: String) {
    val out = JarOutputStream(FileOutputStream(jar))

    values.forEach {
        val entryKey = it.name.replace("\\.", "/") + ".class"
        out.putNextEntry(JarEntry(entryKey))
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        it.accept(writer)
        out.write(writer.toByteArray())
        out.closeEntry()
    }

    out.close()
}

fun Map<String, ClassNode>.findMethod(owner: String, method: String, desc: String): MethodNode {
    val cn = this[owner] ?: error("Class '$owner' not found")
    return cn.methodList.find { it.name == method && it.desc == desc } ?: error("Method '$method$desc' not found")
}

fun Map<String, ClassNode>.findMethodTree(owner: String, name: String, desc: String): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    var ownerClass = this[owner]
    while (ownerClass != null) {
        calls.addAll(ownerClass.methodList.filter {
            it.name == name && it.desc == desc
        })
        ownerClass = this[ownerClass.superName]
    }
    return calls
}