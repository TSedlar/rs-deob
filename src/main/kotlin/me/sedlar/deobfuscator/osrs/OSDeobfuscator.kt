package me.sedlar.deobfuscator.osrs

import me.sedlar.asm.export
import me.sedlar.asm.isLocal
import me.sedlar.asm.util.ClassScanner
import me.sedlar.deobfuscator.*
import me.sedlar.deobfuscator.transform.ClassTransform
import java.io.File

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
val JAR = "D:/RuneScape/packs/oldschool/171.jar"

fun main(args: Array<String>) {
    val classes = ClassScanner.scanJar(File(JAR))

    val transforms: List<ClassTransform> = listOf(
            TryCatchTransform(),
            DeadMethodTransform(DeadMethodTransform.OSRS_ENTRY_NAMES, { cn, mn ->
                (cn.name == "client" || cn.name == classes["client"]!!.superName) && mn.isLocal()
            }),
            OpaqueTransform(),
            DeadFieldTransform()
//            EuclideanTransform()
    )

    transforms.forEach { it.apply(classes) }

    classes.export("./test.jar")
}
