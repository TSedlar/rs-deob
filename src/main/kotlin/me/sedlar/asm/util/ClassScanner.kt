package me.sedlar.asm.util

import me.sedlar.asm.methodList
import me.sedlar.asm.patch
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.jar.JarFile

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
object ClassScanner {

    fun scanClassPath(predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        val list = System.getProperty("java.class.path")
        for (path in list.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val file = File(path)
            if (file.isDirectory) {
                scanDirectory(file, predicate, consumer)
            } else if (path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer)
            }
        }
    }

    fun scanDirectory(directory: File, predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        for (entry in directory.list()!!) {
            val path = "${directory.path}${File.separator}$entry"
            val file = File(path)
            if (file.isDirectory) {
                scanDirectory(file, predicate, consumer)
            } else if (file.isFile && path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer)
            }
        }
    }

    fun scanClassFile(path: String, predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        try {
            FileInputStream(path).use { input -> scanInputStream(input, predicate, consumer) }
        } catch (e: IOException) {
            println("File was not found: $path")
        }
    }

    fun scanInputStream(inputStream: InputStream, predicate: Predicate<ClassNode>,
                        consumer: Consumer<ClassNode>) {
        try {
            val node = ClassNode()
            val reader = ClassReader(inputStream)
            reader.accept(node, ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG)
            if (!predicate.test(node)) {
                return
            }
            node.methodList.forEach { it.patch(node) }
            consumer.accept(node)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun scanJar(file: File): Map<String, ClassNode> {
        val classes = HashMap<String, ClassNode>()
        val predicate = Predicate<ClassNode>({ true })
        val consumer = Consumer<ClassNode>({ classes[it.name] = it })
        try {
            JarFile(file).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".class")) {
                        scanInputStream(jar.getInputStream(entry), predicate, consumer)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return classes
    }
}