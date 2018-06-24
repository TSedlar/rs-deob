package me.sedlar.asm

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
val ClassNode.interfaceList: List<String>
    @Suppress("UNCHECKED_CAST")
    get() = this.interfaces as List<String>

val ClassNode.fieldList: List<FieldNode>
    @Suppress("UNCHECKED_CAST")
    get() = this.fields as List<FieldNode>

val ClassNode.methodList: List<MethodNode>
    @Suppress("UNCHECKED_CAST")
    get() = this.methods as List<MethodNode>