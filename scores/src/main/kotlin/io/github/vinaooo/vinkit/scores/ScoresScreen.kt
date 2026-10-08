package io.github.vinaooo.vinkit.scores

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.formatElapsed
import io.github.vinaooo.vinkit.designsystem.R as DesignR
import io.github.vinaooo.vinkit.designsystem.spokenElapsed
import java.text.DateFormat
import java.util.Date

/**
 * A tab per group played (a title instead for a single one), and in it a section per mode: its name (when the tab
 * holds several), its stats and its top 10. [groupName] names a tab, [modeName] a mode; [details] is what the game
 * shows under a score ("No mistakes · 2 hints"); the date follows it. [points] writes a score's points (Solo's Vegas:
 * dollars). A mode ranked [Ranking.FASTEST] shows its time in their place. A game without scores (only wins, losses
 * and draws) passes `ranked = false`: the stats alone, with draws. A single unranked mode is one whose ranking is null.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    uiState: ScoresUiState,
    onBack: () -> Unit,
    modeName: @Composable (String) -> String,
    modifier: Modifier = Modifier,
    groupName: @Composable (String) -> String = modeName,
    onSelectGroup: (String) -> Unit = {},
    details: @Composable (ScoreRecord) -> String? = { null },
    ranked: Boolean = true,
    points: @Composable (ScoreRecord) -> ScorePoints = { ScorePoints(it.points.toString()) },
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            // The selector stays put; the group's cards slide under it.
            GroupSelector(uiState, groupName, onSelectGroup, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp))
            SlidingGroups(uiState, onSelectGroup, Modifier.fillMaxSize()) { shown ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) { sections(shown, ScoreText(modeName, details, points), ranked) }
            }
        }
    }
}

/**
 * [content] for the chosen group, sliding in from the side of the group it comes from (right for a later one), with
 * the motion scheme's spring. A horizontal swipe picks the next or the previous group. Each page keeps its own state
 * while it slides out, so the old cards don't show the new group's numbers.
 */
@Composable
private fun SlidingGroups(
    uiState: ScoresUiState,
    onSelectGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ScoresUiState) -> Unit,
) {
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val groups = uiState.groups
    AnimatedContent(
        targetState = uiState,
        contentKey = { it.group },
        transitionSpec = {
            val forward = groups.indexOf(targetState.group) > groups.indexOf(initialState.group)
            val direction = if (forward) 1 else -1
            (slideInHorizontally(spatial) { it * direction } + fadeIn(fade)) togetherWith
                (slideOutHorizontally(spatial) { -it * direction } + fadeOut(fade))
        },
        label = "scores group",
        modifier = modifier.swipeBetween(groups, uiState.group, onSelectGroup),
    ) { shown -> content(shown) }
}

/** A horizontal swipe past [SWIPE_DP] picks the next group (to the left) or the previous one (to the right). */
private fun Modifier.swipeBetween(groups: List<String>, group: String?, onSelectGroup: (String) -> Unit): Modifier =
    pointerInput(groups, group) {
        val threshold = SWIPE_DP.dp.toPx()
        var dragged = 0f
        detectHorizontalDragGestures(
            onDragStart = { dragged = 0f },
            onHorizontalDrag = { _, amount -> dragged += amount },
            onDragEnd = {
                val index = groups.indexOf(group)
                val next = when {
                    dragged < -threshold -> index + 1
                    dragged > threshold -> index - 1
                    else -> index
                }
                groups.getOrNull(next)?.takeIf { it != group }?.let(onSelectGroup)
            },
        )
    }

private const val SWIPE_DP = 56

@Composable
private fun GroupSelector(
    uiState: ScoresUiState,
    groupName: @Composable (String) -> String,
    onSelectGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = uiState.groups
    when {
        groups.size in 2..MAX_SEGMENTS -> {
            // The same segmented buttons as Settings' choices (board position, preferred hand).
            SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
                groups.forEachIndexed { index, group ->
                    SegmentedButton(
                        selected = group == uiState.group,
                        onClick = { onSelectGroup(group) },
                        shape = SegmentedButtonDefaults.itemShape(index, groups.size),
                        // No check mark: the filled segment shows the choice (user's request).
                        icon = {},
                    ) { Text(groupName(group)) }
                }
            }
        }
        groups.size > MAX_SEGMENTS -> {
            // More groups than fit a phone's width as segments: scrollable tabs.
            PrimaryScrollableTabRow(
                selectedTabIndex = groups.indexOf(uiState.group).coerceAtLeast(0),
                modifier = modifier,
                edgePadding = 0.dp,
            ) {
                groups.forEach { group ->
                    Tab(selected = group == uiState.group, onClick = {
                        onSelectGroup(group)
                    }, text = { Text(groupName(group)) })
                }
            }
        }
        else -> uiState.group?.let {
            Text(groupName(it), style = MaterialTheme.typography.titleMedium, modifier = modifier)
        }
    }
}

/** Up to this many groups show as segmented buttons; more, as scrollable tabs. */
private const val MAX_SEGMENTS = 4

/** How a score reads: [text] on screen and, when it differs (`-$12`), [spoken] by TalkBack ("minus 12 dollars"). */
data class ScorePoints(val text: String, val spoken: String? = null)

/** The game's words for a section and its scores. */
private class ScoreText(
    val modeName: @Composable (String) -> String,
    val details: @Composable (ScoreRecord) -> String?,
    val points: @Composable (ScoreRecord) -> ScorePoints,
)

/** Each mode of the tab: its name when the tab holds several, its stats, and its scores when it's ranked. */
private fun LazyListScope.sections(uiState: ScoresUiState, text: ScoreText, ranked: Boolean) {
    val titled = uiState.sections.size > 1
    uiState.sections.forEach { section ->
        if (titled) {
            item(key = "title ${section.mode}") {
                Text(
                    text.modeName(section.mode),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                )
            }
        }
        item(key = "stats ${section.mode}") { StatsCard(section.stats, withDraws = !ranked) }
        val ranking = section.ranking?.takeIf { ranked } ?: return@forEach
        if (!uiState.isLoading && section.scores.isEmpty()) {
            item(key = "empty ${section.mode}") {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.vinkit_no_scores), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        itemsIndexed(section.scores) { index, record ->
            ScoreRow(
                index + 1,
                record,
                text.details(record),
                if (ranking ==
                    Ranking.FASTEST
                ) {
                    null
                } else {
                    text.points(record)
                },
            )
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

/** The rank, the [points] and the time (only the time when [points] is null), and the details and date below. */
@Composable
private fun ScoreRow(rank: Int, record: ScoreRecord, details: String?, points: ScorePoints?) {
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
        trailingContent = points?.let {
            {
                Text(
                    formatElapsed(record.elapsedSeconds),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { contentDescription = spokenTime },
                )
            }
        },
    ) {
        val headline = points ?: ScorePoints(formatElapsed(record.elapsedSeconds), spokenTime)
        Text(
            headline.text,
            fontWeight = FontWeight.Bold,
            modifier = headline.spoken?.let { Modifier.semantics { contentDescription = it } } ?: Modifier,
        )
    }
}
