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

/** One mode in the chosen tab: its stats and its top scores, in its [ranking] (null: the mode isn't ranked). */
data class ModeSection(
    val mode: String,
    val stats: GameStats = GameStats(),
    val scores: List<ScoreRecord> = emptyList(),
    val ranking: Ranking? = Ranking.HIGHEST_POINTS,
)

data class ScoresUiState(
    val isLoading: Boolean = true,
    /** The tabs: groups with at least one mode played, won or not. */
    val groups: List<String> = emptyList(),
    /** The chosen tab; null before anything was played. */
    val group: String? = null,
    /** Every mode of the chosen tab, in the game's order; one not played yet shows zeros. */
    val sections: List<ModeSection> = emptyList(),
)

/**
 * The Scores screen's state. [modes] lists every mode the game knows, in order; a stored mode it doesn't list (from
 * a newer version) is skipped. [groupOf] puts modes in tabs (each mode its own tab by default; OX Play: one tab per
 * board, a section per opponent). [rankingFor] says how each mode ranks, or null for a mode with stats only (Solo's
cumulative Vegas). [extraGroups] are the game's own tabs after the modes' (BattleGrid's achievements), always
 * shown and without sections: `ScoresScreen(extra = …)` draws them. Open, so a Hilt app can subclass it with an
 * `@HiltViewModel @Inject constructor`.
 */
open class ScoresViewModel(
    scores: ScoreRepository,
    stats: StatsRepository,
    modes: List<String>,
    extraGroups: List<String> = emptyList(),
    // Before rankingFor, so a caller's trailing lambda stays the ranking.
    groupOf: (String) -> String = { it },
    rankingFor: (String) -> Ranking? = { Ranking.HIGHEST_POINTS },
) : ViewModel() {
    private val chosen = MutableStateFlow<String?>(null)

    // The chosen tab, or the first one until one is chosen.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScoresUiState> =
        combine(stats.observePlayedModes(), chosen) { played, choice ->
            val tabs = modes.filter { it in played }.map(groupOf).distinct() + extraGroups
            tabs to (choice?.takeIf { it in tabs } ?: tabs.firstOrNull())
        }.flatMapLatest { (tabs, group) ->
            if (group == null || group in extraGroups) {
                flowOf(ScoresUiState(isLoading = false, groups = tabs, group = group))
            } else {
                val sections = modes.filter { groupOf(it) == group }.map { mode ->
                    val ranking = rankingFor(mode)
                    val top = ranking?.let { scores.observeTopScores(mode, it) } ?: flowOf(emptyList())
                    combine(top, stats.observe(mode)) { records, modeStats ->
                        ModeSection(mode, modeStats, records, ranking)
                    }
                }
                combine(sections) {
                    ScoresUiState(isLoading = false, groups = tabs, group = group, sections = it.toList())
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ScoresUiState())

    fun selectGroup(group: String) {
        chosen.value = group
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
