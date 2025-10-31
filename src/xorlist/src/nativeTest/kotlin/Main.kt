import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.iterator.shouldHaveNext
import io.kotest.matchers.iterator.shouldNotHaveNext
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

@Suppress("ForEachParameterNotUsed")
class NodeIterator {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should not iterate with 0 items`() {
        val iterator = list.iterator()

        iterator.shouldNotHaveNext()

        var i = 0

        iterator.forEach {
            i++
        }

        i shouldBe 0
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `should iterate with 1 item`() {
        list.add(1)

        val iterator = list.iterator()

        iterator.shouldHaveNext()

        var i = 0
        iterator.forEach {
            i++
        }

        i shouldBe 1
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `should iterate through all items`() {
        list.addAll(Array(11) { it })

        val iterator = list.iterator()

        var i = 0
        iterator.forEach {
            i++
        }

        i shouldBe 11
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `add then iterate then add then iterate`() {
        list.addAll(listOf(1, 2, 3))

        list.iterator().apply {
            shouldHaveNext()
            next() shouldBe 1
            shouldHaveNext()

            list.addAll(listOf(4, 5, 6))

            next() shouldBe 2
            next() shouldBe 3
            next() shouldBe 4
            next() shouldBe 5
            next() shouldBe 6
            shouldNotHaveNext()
        }
    }

    @Test
    fun `should throw exception when next in empty iterator`() {
        val iterator = list.iterator()

        shouldThrow<NoSuchElementException> {
            iterator.next()
        }
    }
}

class MutableNodeIterator {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should remove current item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator()

        iterator.next() shouldBe 1
        iterator.remove()
        iterator.next() shouldBe 2

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 2
        secondIterator.next() shouldBe 3
        secondIterator.shouldNotHaveNext()
    }

    @Test
    fun `should remove single item`() {
        list.add(1)

        val iterator = list.iterator()

        iterator.next()
        iterator.remove()

        val secondIterator = list.iterator()

        secondIterator.shouldNotHaveNext()
    }

    @Test
    fun `iterate then remove then iterate`() {
        list.addAll(Array(10 + 1) { it })

        list.iterator().apply {
            next() shouldBe 0
            next() shouldBe 1
            next() shouldBe 2
            next() shouldBe 3
            next() shouldBe 4
            remove()
            next() shouldBe 5
            remove()
            next() shouldBe 6
            remove()
            next() shouldBe 7
            remove()
            next() shouldBe 8
            remove()

            next() shouldBe 9
            next() shouldBe 10
        }

        list.shouldContainExactly(0, 1, 2, 3, 9, 10)
    }

    @Test
    fun `should remove item in the middle`() {
        list.addAll(listOf(1, 2, 3, 4, 5))

        val iterator = list.iterator()

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.next() shouldBe 3
        iterator.remove()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 1
        secondIterator.next() shouldBe 2
        secondIterator.next() shouldBe 4
    }

    @Test
    fun `remove first item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator()

        iterator.next() shouldBe 1
        iterator.remove()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 2
        secondIterator.next() shouldBe 3
        secondIterator.shouldNotHaveNext()
    }

    @Test
    fun `remove last item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator()

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.next() shouldBe 3
        iterator.remove()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 1
        secondIterator.next() shouldBe 2
        secondIterator.shouldNotHaveNext()
    }

    @Test
    fun `should throw exception when remove in empty iterator`() {
        val iterator = list.iterator()

        shouldThrow<IllegalStateException> {
            iterator.remove()
        }
    }

    @Test
    fun `should throw exception when remove twice`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator()

        iterator.next() shouldBe 1
        iterator.remove()

        shouldThrow<IllegalStateException> {
            iterator.remove()
        }
    }
}

class NodeList {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should add and iterate`() {
        list.addAll(listOf(1, 2, 3))

        val result = list.fold(0) { acc, node -> acc + node }

        result shouldBe 6
    }
}

class NodeCollection {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should contain`() {
        list.addAll(listOf(1, 2, 3))

        list.contains(1) shouldBe true
    }

    @Test
    fun `should not contain`() {
        list.addAll(listOf(1, 2, 3))

        list.contains(4) shouldBe false
    }

    @Test
    fun `should contain all`() {
        list.addAll(listOf(1, 2, 3))
        list.containsAll(listOf(1, 2, 3)) shouldBe true
    }

    @Test
    fun `should not contain all`() {
        list.addAll(listOf(1, 2, 3))
        list.containsAll(listOf(1, 2, 3, 4)) shouldBe false
    }

    @Test
    fun `should be empty`() {
        list.isEmpty() shouldBe true
    }

    @Test
    fun `should not be empty`() {
        list.addAll(listOf(1, 2, 3))

        list.isEmpty() shouldBe false
    }

    @Test
    fun `size should be correct`() {
        list.addAll(listOf(1, 2, 3))

        list.size shouldBe 3
    }

    @Test
    fun `size should be zero`() {
        list.size shouldBe 0
    }
}

class NodeMutableCollection {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should add`() {
        list.add(2) shouldBe true
    }

    @Test
    fun `should add all`() {
        list.addAll(listOf(1, 2, 3)) shouldBe true
    }

    @Test
    fun `should clear`() {
        list.addAll(listOf(1, 2, 3))

        list.clear()

        list.shouldBeEmpty()

        list.iterator().shouldNotHaveNext()
    }

    @Test
    fun `should remove`() {
        list.addAll(listOf(1, 2, 3))

        list.remove(2) shouldBe true
        list.remove(4) shouldBe false
        list.shouldHaveSize(2)

        list.shouldContainExactly(1, 3)
    }

    @Test
    fun `should remove all`() {
        list.addAll(listOf(1, 2, 3))

        list.removeAll(listOf(2, 3))

        list.single() shouldBe 1
    }

    @Test
    fun `should retain all`() {
        list.addAll(listOf(1, 2, 3))

        list.retainAll(listOf(1, 2))

        list.shouldContainExactly(1, 2)
    }
}

class NodeListIterator {
    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should has previous`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator() as ListIterator<*>
        iterator.next()
        iterator.next()

        iterator.hasPrevious() shouldBe true
    }

    @Test
    fun `should not iterate with 0 items`() {
        val iterator = list.iterator() as ListIterator<*>

        iterator.hasPrevious() shouldBe false

        var i = 0

        while (iterator.hasPrevious()) {
            i++
        }

        i shouldBe 0
        iterator.hasPrevious() shouldBe false
    }

    @Test
    fun `should throw exception when previous in empty iterator`() {
        val iterator = list.iterator() as ListIterator<*>

        shouldThrow<NoSuchElementException> {
            iterator.previous()
        }
    }

    @Test
    fun `should return previous item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator() as ListIterator<*>

        iterator.next()
        iterator.next()

        iterator.previous() shouldBe 2
        iterator.previous() shouldBe 1
    }

    @Test
    fun `should return previous item on last item`() {
        list.addAll(listOf(1, 2))

        val iterator = list.iterator() as ListIterator<*>

        iterator.next()
        iterator.next()

        iterator.previous() shouldBe 2
        iterator.previous() shouldBe 1
    }

    @Test
    fun `nextIndex should be zero at start`() {
        list.add(2)
        val iterator = list.iterator() as ListIterator<*>

        iterator.nextIndex() shouldBe 0
    }

    @Test
    fun `previousIndex should be -1 at start`() {
        list.add(2)
        val iterator = list.iterator() as ListIterator<*>

        iterator.previousIndex() shouldBe -1
    }

    @Test
    fun `nextIndex should be one after next`() {
        list.add(1)

        val iterator = list.iterator() as ListIterator<*>
        iterator.next()

        iterator.nextIndex() shouldBe 1
    }

    @Test
    fun `previousIndex should be zero before next`() {
        list.add(1)

        val iterator = list.iterator() as ListIterator<*>
        iterator.next()

        iterator.previousIndex() shouldBe 0
    }

    @Test
    fun `nextIndex should decrease on remove`() {
        list.addAll(listOf(1, 2, 3))
        val iterator = list.iterator() as ListIterator<*>
        iterator.next()
        iterator.next()
        iterator.nextIndex() shouldBe 2
        (iterator as MutableIterator<*>).remove()
        iterator.nextIndex() shouldBe 1
    }

    @Test
    fun `nextIndex should decrease on remove first`() {
        list.addAll(listOf(1, 2, 3))
        val iterator = list.iterator() as ListIterator<*>
        iterator.next()
        iterator.nextIndex() shouldBe 1
        (iterator as MutableIterator<*>).remove()
        iterator.nextIndex() shouldBe 0
    }

    @Test
    fun `previousIndex should decrease on remove first`() {
        list.addAll(listOf(1, 2, 3))
        val iterator = list.iterator() as ListIterator<*>
        iterator.next()
        iterator.previousIndex() shouldBe 0
        (iterator as MutableIterator<*>).remove()
        iterator.previousIndex() shouldBe -1
    }
}

class NodeMutableListIterator {
    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should set previous`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.next()

        iterator.previous()
        iterator.set(4)

        list.shouldContainExactly(1, 4, 3)
    }

    @Test
    fun `should throw exception when set after remove`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.next()
        iterator.remove()

        shouldThrow<IllegalStateException> {
            iterator.set(4)
        }
    }

    @Test
    fun `should set next`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.next()

        iterator.set(4)

        list.shouldContainExactly(1, 4, 3)
    }

    @Test
    fun `add first element`() {
        val iterator = list.listIterator()
        iterator.nextIndex() shouldBe 0
        iterator.previousIndex() shouldBe -1

        iterator.add(1)

        list.shouldContainExactly(1)
        iterator.nextIndex() shouldBe 1
        iterator.previousIndex() shouldBe 0

        iterator.add(2)

        list.shouldContainExactly(1, 2)
    }

    @Test
    fun `add mid element`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.next()

        iterator.nextIndex() shouldBe 2
        iterator.previousIndex() shouldBe 1

        iterator.add(4)
        iterator.nextIndex() shouldBe 3
        iterator.previousIndex() shouldBe 2
        iterator.next() shouldBe 3

        list.shouldContainExactly(1, 2, 4, 3)
    }

    @Test
    fun `previous should return new element`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()

        iterator.next()
        iterator.next()
        iterator.add(4)

        iterator.previous() shouldBe 4
    }

    @Test
    fun `add last element`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()

        iterator.next()
        iterator.next()
        iterator.next()
        iterator.add(4)

        list.size shouldBe 4
        list.shouldContainExactly(1, 2, 3, 4)
    }

    @Test
    fun `should remove first previous item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.previous()
        iterator.remove()

        list.shouldContainExactly(2, 3)
    }

    @Test
    fun `should remove previous item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.listIterator()
        iterator.next()
        iterator.next()
        iterator.previous()
        iterator.remove()

        list.shouldContainExactly(1, 3)
    }
}
