package me.sedlar.deobfuscator

import me.sedlar.asm.graph.CallGraph
import me.sedlar.asm.isLocal
import me.sedlar.asm.methodList
import me.sedlar.asm.owner
import me.sedlar.asm.visitSuperCalls
import me.sedlar.deobfuscator.transform.ClassTransform
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
class DeadMethodTransform(
        private val entryNames: List<String>,
        private val entryFilter: (ClassNode, MethodNode) -> Boolean
) : ClassTransform() {

    private var total = 0
    private var removed = 0

    override fun output(): String {
        return "Removed $removed/$total methods -> ${total - removed}"
    }

    override fun transform(classes: Map<String, ClassNode>) {
        val entries = findEntries(classes, entryNames, entryFilter)
        val calls = CallGraph.create(classes)

        val dead: MutableSet<MethodNode> = HashSet()

        classes.values.forEach {
            it.methodList.forEach {
                total++
                if (calls.inDegreeOf(it) == 0 && it !in entries) {
                    if (!it.isLocal() || it.visitSuperCalls(classes).isEmpty()) {
                        removed++
                        dead.add(it)
                    }
                }
            }
        }

        dead.forEach { it.owner.methods.remove(it) }
    }

    private fun findEntries(classes: Map<String, ClassNode>, entryNames: List<String>,
                            filter: (ClassNode, MethodNode) -> Boolean): Set<MethodNode> {
        val entries: MutableSet<MethodNode> = HashSet()

        classes.values.forEach { cn ->
            cn.methodList.forEach { method ->
                if (filter(cn, method)) {
                    entries.add(method)
                }

                if (method.name.startsWith("<") || method.name in entryNames) {
                    entries.add(method)
                }

                if ((method.access and Opcodes.ACC_ABSTRACT) > 0) {
                    entries.add(method)
                    classes.values.filter {
                        it.superName == cn.name || it.interfaces.contains(cn.name)
                    }.forEach {
                        it.methodList.find {
                            (it.name + it.desc) == (method.name + method.desc)
                        }?.let {
                            entries.add(it)
                        }
                    }
                }
            }
        }
        return entries
    }

    companion object {
        val OSRS_ENTRY_NAMES = listOf(
                "init", "paint", "update", "run", "start", "stop", "destroy", "focusGained", "focusLost",
                "windowActivated", "windowClosing", "windowIconified", "windowOpened", "windowDeactivated",
                "windowDeiconified", "keyPressed", "keyReleased", "keyTyped", "mouseWheelMoved", "mousePressed",
                "mouseReleased", "mouseEntered", "mouseExited", "mouseClicked", "mouseDragged", "mouseMoved",
                "equals", "compareTo", "toString", "hasNext", "next", "iterator", "remove", "compare", "hashCode",
                "finalize"
        )
    }
}