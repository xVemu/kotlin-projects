@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*

class XorLinkedList<E> : MutableIterable<E> {

    private var last = 0UL
    private var first = 0UL

    override fun iterator(): MutableIterator<E> {
        return XorLinkedListIterator(first)
    }

    fun add(i: E) {
        val newItem = Node(i)

        if (last == 0UL) {
            last = newItem.toPointer()
            first = last

            return
        }

        val previousItem = last.toNode<E>()

        // `both` of new item points to previous item address.
        newItem.both = last

        // Head points to new item.
        last = newItem.toPointer()

        // Currently `both` of previous item points to second previous item.
        // Now it sets `both` of previous item to xored new item and second previous item.
        previousItem.both = previousItem bothXor last
    }

    fun addAll(vararg items: E) = items.forEach(::add)

    inner class XorLinkedListIterator(firstPointer: ULong) : MutableIterator<E> {

        private var currentPointer = firstPointer
        private var previousPointer = 0UL

        override fun next(): E {
            val toReturn = currentPointer.toNode<E>()

            // Can't use .toPointer() instead, because then it creates a different pointer.
            val temp = currentPointer

            currentPointer = toReturn bothXor previousPointer
            previousPointer = temp

            return toReturn.value
        }

        override fun hasNext() = currentPointer != 0UL

        override fun remove() {
            if (last == 0UL) return

            // It means there is only one item in the list.
            if (first == last) {
                first.toRef<E>().dispose()

                first = 0UL
                last = 0UL
                currentPointer = 0UL
                previousPointer = 0UL

                return
            }

            val oldHead = last
            // `Both` of old head points to 2nd last item.
            last = oldHead.toNode<E>().both
            last.toNode<E>().apply {
                both = bothXor(oldHead)
            }

            oldHead.toRef<E>().dispose()
        }
    }
}

/** @property both XORed address of next and previous nodes. */
data class Node<E>(val value: E, var both: ULong = 0UL) {

    infix fun bothXor(pointer: ULong): ULong = both xor pointer

    /** Should be called only once !!! */
    fun toPointer() =
        StableRef.create(this).asCPointer().toLong().toULong()

    override fun toString() = "Node(value=$value, both=0x${both.toString(16)})"
}

private fun <E> ULong.toRef() = toOpaquePointer().asStableRef<Node<E>>()

private fun <E> ULong.toNode() = toOpaquePointer().asStableRef<Node<E>>().get()

fun ULong.toOpaquePointer(): COpaquePointer = this.toLong().toCPointer()!!

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val list = XorLinkedList<Int>()

    list.addAll(1, 2, 3)

    list.forEach {
        println("value = $it")
    }

    list.forEach {
        println("value = $it")
    }
}

