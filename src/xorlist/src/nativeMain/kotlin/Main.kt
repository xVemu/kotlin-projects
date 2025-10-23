@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*

class XorLinkedList : MutableIterable<Node> {

    private var last = 0UL
    private var first = 0UL

    override fun iterator(): MutableIterator<Node> {
        return XorLinkedListIterator(first)
    }

    fun add(i: Int) {
        val newItem = Node(i)

        if (last == 0UL) {
            last = newItem.toPointer()
            first = last

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

    fun addAll(vararg items: Int) = items.forEach(::add)

    inner class XorLinkedListIterator(firstPointer: ULong) : MutableIterator<Node> {

        private var currentPointer = firstPointer
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

        override fun remove() {
            if (last == 0UL) return

            // It means there is only one item in the list.
            if (first == last) {
                first.toRef().dispose()

                first = 0UL
                last = 0UL
                currentPointer = 0UL
                previousPointer = 0UL

                return
            }

            val oldHead = last
            // `Both` of old head points to 2nd last item.
            last = oldHead.toNode().both
            last.toNode().apply {
                both = bothXor(oldHead)
            }

            oldHead.toRef().dispose()
        }
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

private fun ULong.toRef() = toOpaquePointer().asStableRef<Node>()

private fun ULong.toNode() = toOpaquePointer().asStableRef<Node>().get()

fun ULong.toOpaquePointer(): COpaquePointer = this.toLong().toCPointer()!!

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val list = XorLinkedList()

    list.addAll(1, 2, 3)

    list.forEach {
        println("value = ${it.value}")
    }

    list.forEach {
        println("value = ${it.value}")
    }
}

