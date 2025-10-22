@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*

class XorLinkedListIterator : Iterator<Node> {

    private var last = 0UL

    private var first = 0UL

    private var currentPointer = 0UL
    private var previousPointer = 0UL

    override fun next(): Node {
        val toReturn = currentPointer.toNode()

        // Can't use .toPointer() instead, because then it creates a different pointer.
        val temp = currentPointer

        currentPointer = toReturn bothXor previousPointer
        previousPointer = temp

        return toReturn
    }

    override fun hasNext() = currentPointer != 0UL

    fun add(i: Int) {
        val newItem = Node(i)

        if (last == 0UL) {
            last = newItem.toPointer()
            first = last
            currentPointer = first

            return
        }

        val previousItem = last.toNode()

        // `both` of new item points to previous item address.
        newItem.both = last

        // Head points to new item.
        last = newItem.toPointer()

        // Currently `both` of previous item points to second previous item.
        // Now it sets `both` of previous item to xored new item and second previous item.
        previousItem.both = previousItem bothXor last
    }
}

/** @property both XORed address of next and previous nodes. */
data class Node(val value: Int, var both: ULong = 0UL) {

    infix fun bothXor(pointer: ULong): ULong = both xor pointer

    /** Should be called only once !!! */
    fun toPointer() =
        StableRef.create(this).asCPointer().toLong().toULong()

    override fun toString() = "Node(value=$value, both=0x${both.toString(16)})"
}

private fun ULong.toNode() = toOpaquePointer().asStableRef<Node>().get()

fun ULong.toOpaquePointer(): COpaquePointer = this.toLong().toCPointer()!!

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val iterator = XorLinkedListIterator()
    iterator.add(1)
    iterator.add(2)
    iterator.add(3)

    iterator.forEach {
        println("value = ${it.value}")
    }
}

