import io.kotest.matchers.iterator.shouldHaveNext
import io.kotest.matchers.iterator.shouldNotHaveNext
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@Suppress("ForEachParameterNotUsed")
class NodeIterator {

    @Test
    fun `should not iterate with 0 items`() {
        val iterator = XorLinkedListIterator()

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
        val iterator = XorLinkedListIterator()
        iterator.add(1)

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
        val iterator = XorLinkedListIterator()

        (0..10).forEach {
            iterator.add(it)
        }

        var i = 0
        iterator.forEach {
            i++
        }

        i shouldBe 11
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `add then iterate then add then iterate`() {
        XorLinkedListIterator().apply {
            add(1)
            add(2)
            add(3)

            shouldHaveNext()
            next().value shouldBe 1
            shouldHaveNext()

            add(4)
            add(5)
            add(6)

            next().value shouldBe 2
            next().value shouldBe 3
            next().value shouldBe 4
            next().value shouldBe 5
            next().value shouldBe 6
            shouldNotHaveNext()
        }
    }
}

class MutableNodeIterator {
    @Test
    fun `should remove last item`() {
        val iterator = XorLinkedListIterator()

        iterator.add(1)
        iterator.add(2)
        iterator.add(3)

        iterator.remove()

        iterator.next().value shouldBe 1
        iterator.next().value shouldBe 2
        iterator.shouldNotHaveNext()
    }

    @Test
    fun `should remove first item`() {
        val iterator = XorLinkedListIterator()

        iterator.add(1)

        iterator.remove()
        iterator.remove()
        iterator.remove()

        iterator.shouldNotHaveNext()
    }

    @Test
    fun `iterate then remove then iterate`() {
        val iterator = XorLinkedListIterator()

        (0..10).forEach {
            iterator.add(it)
        }

        iterator.next().value shouldBe 0
        iterator.next().value shouldBe 1
        iterator.next().value shouldBe 2
        iterator.next().value shouldBe 3
        iterator.next().value shouldBe 4
        iterator.remove()
        iterator.remove()
        iterator.remove()
        iterator.remove()
        iterator.remove()
        iterator.next().value shouldBe 5

        iterator.shouldNotHaveNext()
    }
}
