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
}

class MutableNodeIterator {

    private var list = XorLinkedList<Int>()

    @BeforeTest
    fun setUp() {
        list = XorLinkedList()
    }

    @Test
    fun `should remove last item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator()

        iterator.remove()

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `should remove first item`() {
        list.add(1)

        val iterator = list.iterator()

        iterator.remove()
        iterator.remove()
        iterator.remove()

        iterator.shouldNotHaveNext()
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
            remove()
            remove()
            remove()
            remove()

            next() shouldBe 5

            shouldNotHaveNext()
        }
    }

    @Test
    fun `should remove item in the middle`() {
        list.addAll(listOf(1, 2, 3, 4, 5))

        val iterator = list.iterator() as XorLinkedList.XorLinkedListIterator

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.next() shouldBe 3
        iterator.removePrevious()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 1
        secondIterator.next() shouldBe 2
        secondIterator.next() shouldBe 4
    }

    @Test
    fun `should remove single item`() {
        list.add(1)

        val iterator = list.iterator() as XorLinkedList.XorLinkedListIterator

        iterator.next() shouldBe 1
        iterator.removePrevious()

        iterator.shouldNotHaveNext()

        list.shouldBeEmpty()

        list.iterator().shouldNotHaveNext()
    }

    @Test
    fun `remove first item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator() as XorLinkedList.XorLinkedListIterator

        iterator.next() shouldBe 1
        iterator.removePrevious()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 2
        secondIterator.next() shouldBe 3
        secondIterator.shouldNotHaveNext()
    }

    @Test
    fun `remove last item`() {
        list.addAll(listOf(1, 2, 3))

        val iterator = list.iterator() as XorLinkedList.XorLinkedListIterator

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.next() shouldBe 3
        iterator.removePrevious()

        val secondIterator = list.iterator()

        secondIterator.next() shouldBe 1
        secondIterator.next() shouldBe 2
        secondIterator.shouldNotHaveNext()
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
