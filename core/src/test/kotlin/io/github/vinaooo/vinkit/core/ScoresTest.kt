package io.github.vinaooo.vinkit.core

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ScoresTest {
    private fun record(points: Int, seconds: Long, at: Long) = ScoreRecord("M", points, seconds, at)

    @Test
    fun `highest points ranks by points, then time, then the earliest`() {
        val records = listOf(record(500, 90, 3), record(800, 200, 4), record(500, 60, 5), record(500, 60, 1))
        records.sortedWith(Ranking.HIGHEST_POINTS.comparator) shouldBe
            listOf(record(800, 200, 4), record(500, 60, 1), record(500, 60, 5), record(500, 90, 3))
    }

    @Test
    fun `fastest ranks by time, then points, then the earliest`() {
        val records = listOf(record(100, 90, 3), record(800, 200, 4), record(500, 90, 5), record(500, 90, 1))
        records.sortedWith(Ranking.FASTEST.comparator) shouldBe
            listOf(record(500, 90, 1), record(500, 90, 5), record(100, 90, 3), record(800, 200, 4))
    }

    @Test
    fun `streaks grow with wins and reset on a loss`() {
        val stats = GameStats().afterWin().afterWin().afterLoss().afterWin()
        stats shouldBe GameStats(played = 4, won = 3, currentStreak = 1, bestStreak = 2)
        stats.winRatePercent shouldBe 75
        GameStats().winRatePercent shouldBe 0
    }
}
