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
        private var calledNext = false

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException()
            calledNext = true

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
            // Required to align with spec. I'm not happy with this solution.
            if (!calledNext) throw IllegalStateException()
            calledNext = false

            count--

            // It means there is only one item in the list.
            // Code below automatically sets first, last, currentPointer and previousPointer to 0.
            if (first == last) currentPointer = 0UL

            val previous2Pointer = previousPointer.toNode<E>().bothXor(currentPointer)
            // Null if previousPointer is first item
            val previous2Node = previous2Pointer.takeUnless { it == 0UL }?.toNode<E>()
            val nextPointer = currentPointer
            // Null if previousPointer is last item
            val nextNode = nextPointer.takeUnless { it == 0UL }?.toNode<E>()

            // Removes previousPointer and adds nextPointer to xorred address
            previous2Node?.both = previous2Node.both xor previousPointer xor nextPointer
            // Removes previousPointer and adds previous2Pointer to xorred address
            nextNode?.both = previous2Pointer xor previousPointer xor nextNode.both

            // Sets first
            if (previous2Node == null) first = nextPointer
            if (nextNode == null) last = previous2Pointer

            previousPointer.toRef<E>().dispose()

            previousPointer = previous2Pointer
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

