package io.github.vinaooo.vinkit.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.ScoreRepository
import io.github.vinaooo.vinkit.core.StatsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class ScoresUiState(
    val scores: List<ScoreRecord> = emptyList(),
    val stats: GameStats = GameStats(),
    val isLoading: Boolean = true,
    /** The modes played at least once, won or not, one tab each. */
    val modes: List<String> = emptyList(),
    /** The mode whose ranking shows; null before anything was played. */
    val mode: String? = null,
)

/**
 * The Scores screen's state. [modes] lists every mode the game knows, in tab order; a stored mode it doesn't list
 * (from a newer version) is skipped. [rankingFor] says how each mode ranks. Open, so a Hilt app can subclass it
 * with an `@HiltViewModel @Inject constructor`.
 */
open class ScoresViewModel(
    scores: ScoreRepository,
    stats: StatsRepository,
    modes: List<String>,
    rankingFor: (String) -> Ranking = { Ranking.HIGHEST_POINTS },
) : ViewModel() {
    private val chosen = MutableStateFlow<String?>(null)

    // The chosen tab, or the first mode played until one is chosen.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScoresUiState> =
        combine(stats.observePlayedModes(), chosen) { played, choice ->
            val tabs = modes.filter { it in played }
            tabs to (choice?.takeIf { it in tabs } ?: tabs.firstOrNull())
        }.flatMapLatest { (tabs, mode) ->
            if (mode == null) {
                flowOf(ScoresUiState(isLoading = false))
            } else {
                combine(scores.observeTopScores(mode, rankingFor(mode)), stats.observe(mode)) { top, modeStats ->
                    ScoresUiState(top, modeStats, isLoading = false, modes = tabs, mode = mode)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ScoresUiState())

    fun selectMode(mode: String) {
        chosen.value = mode
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
