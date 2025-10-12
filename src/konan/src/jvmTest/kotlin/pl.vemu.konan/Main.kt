package pl.vemu.konan

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class Main {
    @Test
    fun `test add`() {
        val res = add(1, 2)
        res shouldBe 3
    }
}
