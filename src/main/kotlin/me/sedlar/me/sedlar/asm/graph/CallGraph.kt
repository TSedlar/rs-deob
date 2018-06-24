package me.sedlar.me.sedlar.asm.graph

import me.sedlar.me.sedlar.asm.methodList
import me.sedlar.me.sedlar.asm.visitCalls
import org.jgrapht.graph.DefaultDirectedGraph
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
object CallGraph {

    fun create(classes: Map<String, ClassNode>): DefaultDirectedGraph<MethodNode, MethodEdge> {
        val graph = DefaultDirectedGraph<MethodNode, MethodEdge>(MethodEdge::class.java)

        classes.values.forEach {
            it.methodList.forEach {
                graph.addVertex(it)
            }
        }

        classes.values.forEach {
            it.methodList.forEach {
                it.visitCalls(classes).forEach { call ->
                    graph.addEdge(it, call)
                }
            }
        }

        return graph
    }
}