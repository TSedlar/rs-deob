package me.sedlar.deobfuscator

import me.sedlar.asm.methodList
import me.sedlar.deobfuscator.transform.ClassTransform
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import java.math.BigInteger

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */
class EuclideanTransform: ClassTransform() {

    private val SHIFT_32 = BigInteger.ONE.shiftLeft(32)
    private val SHIFT_64 = BigInteger.ONE.shiftLeft(64)

    private var removed = 0

    override fun output(): String {
        return "Removed $removed euclidean pairs"
    }

    override fun transform(classes: Map<String, ClassNode>) {
        val intMultipliers: MutableSet<Int> = HashSet()
        val longMultipliers: MutableSet<Long> = HashSet()
        val matchedIntMultipliers: MutableSet<Int> = HashSet()
        val matchedLongMultipliers: MutableSet<Long> = HashSet()

        val dead: MutableList<LdcInsnNode> = ArrayList()

        classes.values.forEach {
            it.methodList.forEach {
                it.instructions.toArray().forEach {
                    if (it is LdcInsnNode) {
                        val cst = it.cst
                        if (cst is Int && cst.toInt() % 2 != 0) {
                            intMultipliers.add(cst.toInt())
                        } else if (cst is Long && cst.toLong() % 2 != 0L) {
                            longMultipliers.add(cst.toLong())
                        }
                    }
                }
            }
        }

        intMultipliers.forEach {
            val bigInt = it.toBigInteger()
            if (bigInt.bitLength() > 0) {
                val inverse = bigInt.modInverse(SHIFT_32).toInt()
                if (inverse in intMultipliers) {
                    matchedIntMultipliers.add(it)
                    matchedIntMultipliers.add(inverse)
                }
            }
        }

        longMultipliers.forEach {
            val bigInt = it.toBigInteger()
            if (bigInt.bitLength() > 0) {
                val inverse = bigInt.modInverse(SHIFT_64).toLong()
                if (inverse in longMultipliers) {
                    matchedLongMultipliers.add(it)
                    matchedLongMultipliers.add(inverse)
                }
            }
        }

        classes.values.forEach {
            it.methodList.forEach {
                it.instructions.toArray().forEach {
                    if (it is LdcInsnNode) {
                        val cst = it.cst
                        if (cst is Int && cst.toInt() in matchedIntMultipliers) {
                            it.cst = 1
                            dead.add(it)
                        } else if (cst is Long && cst.toLong() in matchedLongMultipliers) {
                            it.cst = 1L
                            dead.add(it)
                        }
                    }
                }
            }
        }

        removed += dead.size
    }
}