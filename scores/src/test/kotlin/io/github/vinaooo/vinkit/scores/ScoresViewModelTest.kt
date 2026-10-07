package io.github.vinaooo.vinkit.scores

import app.cash.turbine.test
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.ScoreRepository
import io.github.vinaooo.vinkit.core.StatsRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScoresViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val records = listOf(
        ScoreRecord("EASY", 700, 120, 1),
        ScoreRecord("EASY", 900, 100, 2),
        ScoreRecord("EASY", 950, 300, 3),
        ScoreRecord("HARD", 5000, 600, 4),
    )
    private val played = mapOf(
        "HARD" to GameStats(played = 2, won = 1),
        "EASY" to GameStats(played = 3, won = 3, currentStreak = 3, bestStreak = 3),
        "EXPERT" to GameStats(played = 1),
        "SAMURAI" to GameStats(played = 1),
    )
    private val scores = object : ScoreRepository {
        override fun observeTopScores(mode: String, ranking: Ranking, limit: Int): Flow<List<ScoreRecord>> =
            flowOf(records.filter { it.mode == mode }.sortedWith(ranking.comparator).take(limit))

        override suspend fun add(record: ScoreRecord) = Unit
    }
    private val stats = object : StatsRepository {
        override fun observe(mode: String) = flowOf(played[mode] ?: GameStats())

        override fun observePlayedModes() = flowOf(played.keys)

        override suspend fun update(mode: String, transform: (GameStats) -> GameStats) = Unit
    }

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ScoresViewModel(scores, stats, listOf("EASY", "HARD", "EXPERT")) {
        if (it == "EASY") Ranking.FASTEST else Ranking.HIGHEST_POINTS
    }

    @Test
    fun `a tab per known mode played, in the game's order, the first shown in its own ranking`() = runTest(dispatcher) {
        viewModel().uiState.test {
            awaitItem().isLoading shouldBe true
            val state = awaitItem()
            state.modes shouldBe listOf("EASY", "HARD", "EXPERT")
            state.mode shouldBe "EASY"
            state.scores.map { it.points } shouldBe listOf(900, 700, 950)
            state.stats.won shouldBe 3
        }
    }

    @Test
    fun `choosing a tab shows that mode`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.test {
            skipItems(2)
            viewModel.selectMode("EXPERT")
            val state = awaitItem()
            state.mode shouldBe "EXPERT"
            state.scores shouldBe emptyList()
            state.stats.played shouldBe 1
        }
    }
}
