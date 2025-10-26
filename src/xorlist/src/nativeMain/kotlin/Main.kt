@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*

class XorLinkedList<E> : MutableCollection<E> {

    private var last = 0UL
    private var first = 0UL
    private var count = 0

    override fun iterator(): MutableIterator<E> {
        return XorLinkedListIterator(first)
    }

    inner class XorLinkedListIterator(firstPointer: ULong) : MutableIterator<E> {

        private var currentPointer = firstPointer
        private var previousPointer = 0UL

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException()

            val toReturn = currentPointer.toNode<E>()

            // Can't use .toPointer() instead, because then it creates a different pointer.
            val temp = currentPointer

            currentPointer = toReturn bothXor previousPointer
            previousPointer = temp

            return toReturn.value
        }

        override fun hasNext() = currentPointer != 0UL

        // Removes current item.
        override fun remove() {
            // throw IllegalStateException()
            if (last == 0UL) return
            count--

            // It means there is only one item in the list.
            if (first == last) return removeWhenSingle()

            if (previousPointer == last) {
                last = removeEnding(last)
                return
            }

            if (previousPointer == first) {
                first = removeEnding(first)
                return
            }

            val previous2Pointer = previousPointer.toNode<E>().bothXor(currentPointer)
            val previous2Node = previous2Pointer.toNode<E>()
            val nextPointer = currentPointer
            val nextNode = nextPointer.toNode<E>()

            // Removes previousPointer and adds nextPointer to xorred address
            previous2Node.both = previous2Node.both xor previousPointer xor nextPointer
            // Removes previousPointer and adds previous2Pointer to xorred address
            nextNode.both = previous2Pointer xor previousPointer xor nextNode.both

            previousPointer.toRef<E>().dispose()

            previousPointer = previous2Pointer
        }

        /** @return address to new ending */
        private fun removeEnding(old: ULong): ULong {
            // `Both` of old points to next/previous item.
            val new = old.toNode<E>().both

            new.toNode<E>().apply {
                // Removes old pointer from xorred address
                both = bothXor(old)
            }

            old.toRef<E>().dispose()
            previousPointer = new

            return new
        }

        private fun removeWhenSingle() {
            first.toRef<E>().dispose()

            first = 0UL
            last = 0UL
            currentPointer = 0UL
            previousPointer = 0UL
        }
    }

    override fun contains(element: E): Boolean {
        iterator().forEach {
            if (it == element) return true
        }

        return false
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        elements.forEach {
            if (!contains(it)) return false
        }

        return true
    }

    override fun isEmpty() = size == 0

    override val size
        get() = count

    override fun add(element: E): Boolean {
        val newItem = Node(element)
        count++

        if (last == 0UL) {
            last = newItem.toPointer()
            first = last

            return true
        }

        val previousItem = last.toNode<E>()

        // `both` of new item points to previous item address.
        newItem.both = last

        // Head points to new item.
        last = newItem.toPointer()

        // Currently `both` of previous item points to second previous item.
        // Now it sets `both` of previous item to xored new item and second previous item.
        previousItem.both = previousItem bothXor last

        return true
    }

    override fun addAll(elements: Collection<E>): Boolean = elements.all(::add)

    override fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) iterator.remove()
    }

    override fun remove(element: E): Boolean {
        val iterator = iterator()

        iterator.forEach {
            if (it != element)
                return@forEach

            iterator.remove()
            return true
        }

        return false
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val iterator = iterator()
        var modified = false

        iterator.forEach {
            if (!elements.contains(it))
                return@forEach

            iterator.remove()
            modified = true
        }

        return modified
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        val iterator = iterator()
        var modified = false

        iterator.forEach {
            if (elements.contains(it))
                return@forEach

            iterator.remove()
            modified = true
        }

        return modified
    }
}

// TODO move to class to omit <E>

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

    list.addAll(listOf(1, 2, 3))

    list.forEach {
        println("value = $it")
    }

    list.forEach {
        println("value = $it")
    }
}

