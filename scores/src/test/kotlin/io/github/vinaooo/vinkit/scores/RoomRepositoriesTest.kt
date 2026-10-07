package io.github.vinaooo.vinkit.scores

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.scores.data.RoomScoreRepository
import io.github.vinaooo.vinkit.scores.data.RoomStatsRepository
import io.github.vinaooo.vinkit.scores.data.ScoresDatabase
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomRepositoriesTest {
    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        ScoresDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val scores = RoomScoreRepository(db)
    private val stats = RoomStatsRepository(db)

    private fun record(mode: String, points: Int, seconds: Long = 100, at: Long = 0) =
        ScoreRecord(mode, points, seconds, at, extras = mapOf("mistakes" to "1"))

    @After
    fun tearDown() = db.close()

    @Test
    fun `top scores are per mode, in the mode's ranking, limited, with their extras`() = runTest {
        val all = listOf(
            record("CLASSIC", 500, at = 3),
            record("CLASSIC", 800),
            record("CLASSIC", 500, seconds = 60, at = 5),
            record("CLASSIC", 500, seconds = 60, at = 1),
            record("KILLER", 9000),
        )
        all.forEach { scores.add(it) }
        val classic = all.filter { it.mode == "CLASSIC" }

        scores.observeTopScores("CLASSIC", Ranking.HIGHEST_POINTS, limit = 3).first() shouldBe
            classic.sortedWith(Ranking.HIGHEST_POINTS.comparator).take(3)
        scores.observeTopScores("CLASSIC", Ranking.FASTEST).first() shouldBe
            classic.sortedWith(Ranking.FASTEST.comparator)
        scores.observeTopScores("KILLER", Ranking.HIGHEST_POINTS).first() shouldBe listOf(record("KILLER", 9000))
    }

    @Test
    fun `no stats yet read as zeros`() = runTest {
        stats.observe("CLASSIC").first() shouldBe GameStats()
    }

    @Test
    fun `stats updates build on what is stored, per mode`() = runTest {
        stats.update("CLASSIC") { it.afterWin() }
        stats.update("CLASSIC") { it.afterWin() }
        stats.update("CLASSIC") { it.afterLoss() }
        stats.update("KILLER") { it.afterWin() }

        stats.observe("CLASSIC").first() shouldBe GameStats(played = 3, won = 2, currentStreak = 0, bestStreak = 2)
        stats.observe("KILLER").first() shouldBe GameStats(played = 1, won = 1, currentStreak = 1, bestStreak = 1)
    }

    @Test
    fun `played modes are the ones with a game played, won or not`() = runTest {
        stats.observePlayedModes().test {
            awaitItem() shouldBe emptySet()
            stats.update("KILLER") { it.afterLoss() }
            awaitItem() shouldBe setOf("KILLER")
            cancelAndIgnoreRemainingEvents()
        }
        stats.update("CLASSIC") { it }
        stats.observePlayedModes().first() shouldBe setOf("KILLER")
    }
}
