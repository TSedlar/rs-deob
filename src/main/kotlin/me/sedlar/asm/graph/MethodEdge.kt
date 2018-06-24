package me.sedlar.asm.graph

import me.sedlar.asm.key
import org.jgrapht.graph.DefaultEdge
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
class MethodEdge: DefaultEdge() {

    public override fun getSource(): MethodNode {
        return super.getSource() as MethodNode
    }

    public override fun getTarget(): MethodNode {
        return super.getTarget() as MethodNode
    }

    override fun toString(): String {
        return "(${source.key} : ${target.key})"
    }
}