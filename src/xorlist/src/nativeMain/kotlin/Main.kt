@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*

class XorLinkedList<E> : MutableCollection<E> {

    private var last = 0UL
    private var first = 0UL
    private var count = 0

    override fun iterator(): MutableIterator<E> {
        return XorLinkedListIterator(first)
    }

    inner class XorLinkedListIterator(firstPointer: ULong) : MutableListIterator<E> {

        private var nextPointer = firstPointer
        private var nextIndex = 0
        private var previousPointer = 0UL
        private var returned = 0UL

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException()
            nextIndex++

            returned = nextPointer
            val toReturn = nextPointer.toNode()

            // Can't use .toPointer() instead, because then it creates a different pointer.
            val temp = nextPointer

            nextPointer = toReturn bothXor previousPointer
            previousPointer = temp

            return toReturn.value
        }

        override fun hasNext() = nextPointer != 0UL

        // Removes current item.
        override fun remove() {
            if (returned == 0UL) throw IllegalStateException()

            count--
            nextIndex--

            val basePointer = returned
            val baseNode = basePointer.toNode()

            // Base cancels previous or next from xor.
            val sibling1Ptr = previousPointer xor nextPointer xor basePointer
            // Null if it's the first or last item
            val sibling1Node = sibling1Ptr.takeUnless { it == 0UL }?.toNode()

            val sibling2Ptr = baseNode.bothXor(sibling1Ptr)
            // Null if it's the first or last item
            val sibling2Node = sibling2Ptr.takeUnless { it == 0UL }?.toNode()

            // Removes basePointer and adds oppositeSibling to xorred address
            sibling2Node?.both = sibling2Node.both xor sibling1Ptr xor basePointer
            sibling1Node?.both = sibling1Node.both xor sibling2Ptr xor basePointer

            basePointer.toRef().dispose()

            // Set new first item
            if (first == basePointer)
                first = sibling1Ptr xor sibling2Ptr
            // Set new last item
            if (last == basePointer)
                last = sibling1Ptr xor sibling2Ptr

            if (returned == previousPointer)
                previousPointer = sibling2Ptr
            if (returned == nextPointer)
                nextPointer = sibling2Ptr

            returned = 0UL
        }

        override fun hasPrevious(): Boolean = previousPointer != 0UL

        override fun previous(): E {
            if (!hasPrevious()) throw NoSuchElementException()
            nextIndex--

            returned = previousPointer
            val toReturn = previousPointer.toNode()

            // Can't use .toPointer() instead, because then it creates a different pointer.
            val temp = previousPointer

            previousPointer = toReturn bothXor nextPointer
            nextPointer = temp

            return toReturn.value
        }

        override fun nextIndex(): Int = nextIndex

        override fun previousIndex(): Int = nextIndex - 1

        override fun add(element: E) {
            val newItem = Node(element)
            count++
            nextIndex++

            if (last == 0UL) {
                last = newItem.toPointer()
                first = last
                previousPointer = last

                return
            }

            val newPointer = newItem.toPointer()
            newItem.both = previousPointer xor nextPointer

            previousPointer.takeUnless { it == 0UL }?.toNode()?.let { previousNode ->
                previousNode.both = previousNode.both xor nextPointer xor newPointer
            }

            nextPointer.takeUnless { it == 0UL }?.toNode()?.let { currentNode ->
                currentNode.both = previousPointer xor newPointer xor currentNode.both
            }

            previousPointer = newPointer
        }

        override fun set(element: E) {
            if (returned == 0UL) throw IllegalStateException()

            returned.toNode().value = element
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

        val previousItem = last.toNode()

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
        iterator.forEach { _ ->
            iterator.remove()
        }
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

    fun listIterator() = XorLinkedListIterator(first)

    private fun ULong.toRef() = toOpaquePointer().asStableRef<Node<E>>()

    private fun ULong.toNode() = toOpaquePointer().asStableRef<Node<E>>().get()
}

/** @property both XORed address of next and previous nodes. */
data class Node<E>(var value: E, var both: ULong = 0UL) {

    infix fun bothXor(pointer: ULong): ULong = both xor pointer

    /** Should be called only once !!! */
    fun toPointer() =
        StableRef.create(this).asCPointer().toLong().toULong()

    override fun toString() = "Node(value=$value, both=0x${both.toString(16)})"
}

private fun ULong.toOpaquePointer(): COpaquePointer = this.toLong().toCPointer()!!

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

