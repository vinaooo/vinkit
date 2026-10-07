package io.github.vinaooo.vinkit.core

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test

class GameCodecTest {

    @Serializable
    data class Board(val cells: List<Int>, val moves: Int)

    private val codec = GameCodec(Board.serializer())
    private val board = Board(cells = List(81) { it % 10 }, moves = 42)

    @Test
    fun `a state survives encode and decode`() {
        codec.decode(codec.encode(board)) shouldBe board
    }

    @Test
    fun `line breaks and spaces added by an email or issue are ignored`() {
        val wrapped = codec.encode(board).chunked(20).joinToString("\n", prefix = "  ", postfix = " \n")
        codec.decode(wrapped) shouldBe board
    }
}
