package me.sedlar.deobfuscator.osrs

import me.sedlar.deobfuscator.DeadMethodTransform
import me.sedlar.deobfuscator.transform.ClassTransform
import me.sedlar.me.sedlar.asm.export
import me.sedlar.me.sedlar.asm.isLocal
import me.sedlar.me.sedlar.asm.util.ClassScanner
import java.io.File

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
val JAR = "D:/RuneScape/packs/oldschool/171.jar"

fun main(args: Array<String>) {
    val classes = ClassScanner.scanJar(File(JAR))

    val transforms: List<ClassTransform> = listOf(
            DeadMethodTransform(DeadMethodTransform.OSRS_ENTRY_NAMES, { cn, mn ->
                (cn.name == "client" || cn.name == classes["client"]!!.superName) && mn.isLocal()
            })
    )

    transforms.forEach { it.apply(classes) }

    classes.export("./test.jar")
}
