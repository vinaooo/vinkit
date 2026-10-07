package io.github.vinaooo.vinkit.scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.formatElapsed
import io.github.vinaooo.vinkit.designsystem.R as DesignR
import io.github.vinaooo.vinkit.designsystem.spokenElapsed
import java.text.DateFormat
import java.util.Date

/**
 * A tab per mode played (a title instead for a single one), its stats and its top 10. [modeName] names a mode;
 * [details] is what the game shows under a score ("No mistakes · 2 hints"); the date follows it. A game without
 * scores (only wins, losses and draws) passes `ranked = false`: the stats alone, with draws.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    uiState: ScoresUiState,
    onBack: () -> Unit,
    modeName: @Composable (String) -> String,
    modifier: Modifier = Modifier,
    onSelectMode: (String) -> Unit = {},
    details: @Composable (ScoreRecord) -> String? = { null },
    ranked: Boolean = true,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vinkit_scores_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(DesignR.string.vinkit_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.modes.size > 1) {
                item {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = uiState.modes.indexOf(uiState.mode).coerceAtLeast(0),
                        edgePadding = 0.dp,
                    ) {
                        uiState.modes.forEach { mode ->
                            Tab(
                                selected = mode == uiState.mode,
                                onClick = { onSelectMode(mode) },
                                text = { Text(modeName(mode)) },
                            )
                        }
                    }
                }
            } else {
                uiState.mode?.let { item { Text(modeName(it), style = MaterialTheme.typography.titleMedium) } }
            }
            if (uiState.mode != null) item { StatsCard(uiState.stats, withDraws = !ranked) }
            if (ranked && !uiState.isLoading && uiState.scores.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.vinkit_no_scores), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            itemsIndexed(uiState.scores) { index, record -> ScoreRow(index + 1, record, details(record)) }
        }
    }
}

/** Played, won, win rate and streaks; [withDraws], draws and losses too, on two rows so a phone fits them. */
@Composable
private fun StatsCard(stats: GameStats, withDraws: Boolean) {
    val played = StatItemData(stringResource(R.string.vinkit_stat_played), stats.played.toString())
    val won = StatItemData(stringResource(R.string.vinkit_stat_won), stats.won.toString())
    val rate = StatItemData(stringResource(R.string.vinkit_stat_win_rate), "${stats.winRatePercent}%")
    val streak = StatItemData(stringResource(R.string.vinkit_stat_streak), stats.currentStreak.toString())
    val best = StatItemData(stringResource(R.string.vinkit_stat_best_streak), stats.bestStreak.toString())
    val rows = if (withDraws) {
        listOf(
            listOf(
                played,
                won,
                StatItemData(stringResource(R.string.vinkit_stat_drawn), stats.drawn.toString()),
                StatItemData(stringResource(R.string.vinkit_stat_lost), stats.lost.toString()),
            ),
            listOf(rate, streak, best),
        )
    } else {
        listOf(listOf(played, won, rate, streak, best))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { StatItem(it.label, it.value) }
                }
            }
        }
    }
}

private class StatItemData(val label: String, val value: String)

/** A value over its label, read by TalkBack as one item: "Won, 3". */
@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clearAndSetSemantics { contentDescription = "$label, $value" },
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The rank, the points, the time, and the game's details and the date below. */
@Composable
private fun ScoreRow(rank: Int, record: ScoreRecord, details: String?) {
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(record.playedAtMillis))
    val rankDescription = stringResource(R.string.vinkit_rank_description, rank)
    val spokenTime = spokenElapsed(record.elapsedSeconds)
    ListItem(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        leadingContent = {
            Text(
                "#$rank",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { contentDescription = rankDescription },
            )
        },
        supportingContent = { Text(details?.let { stringResource(R.string.vinkit_score_details, it, date) } ?: date) },
        trailingContent = {
            Text(
                formatElapsed(record.elapsedSeconds),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { contentDescription = spokenTime },
            )
        },
    ) { Text(record.points.toString(), fontWeight = FontWeight.Bold) }
}
