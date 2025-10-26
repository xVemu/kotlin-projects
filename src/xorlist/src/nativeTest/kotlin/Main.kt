import io.kotest.matchers.iterator.shouldHaveNext
import io.kotest.matchers.iterator.shouldNotHaveNext
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@Suppress("ForEachParameterNotUsed")
class NodeIterator {

    @Test
    fun `should not iterate with 0 items`() {
        val iterator = XorLinkedList<Int>().iterator()

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
        val list = XorLinkedList<Int>()
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
        val list = XorLinkedList<Int>()

        list.addAll(*Array(11) { it })

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
        val list = XorLinkedList<Int>()
        list.addAll(1, 2, 3)

        list.iterator().apply {
            shouldHaveNext()
            next() shouldBe 1
            shouldHaveNext()

            list.addAll(4, 5, 6)

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
    @Test
    fun `should remove last item`() {
        val list = XorLinkedList<Int>()

        list.addAll(1, 2, 3)

        val iterator = list.iterator()

        iterator.remove()

        iterator.next() shouldBe 1
        iterator.next() shouldBe 2
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `should remove first item`() {
        val list = XorLinkedList<Int>()

        list.add(1)

        val iterator = list.iterator()

        iterator.remove()
        iterator.remove()
        iterator.remove()

        iterator.shouldNotHaveNext()
    }

    @Test
    fun `iterate then remove then iterate`() {
        val list = XorLinkedList<Int>()

        list.addAll(*Array(10 + 1) { it })

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
}

class NodeList {
    @Test
    fun `should add and iterate`() {
        val list = XorLinkedList<Int>()

        list.addAll(1, 2, 3)

        val result = list.fold(0) { acc, node -> acc + node }

        result shouldBe 6
    }
}
