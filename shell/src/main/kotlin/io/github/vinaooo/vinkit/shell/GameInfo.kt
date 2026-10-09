package io.github.vinaooo.vinkit.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import io.github.vinaooo.vinkit.core.formatElapsed
import io.github.vinaooo.vinkit.designsystem.spokenElapsed

/**
 * The mode's [name] and the clock, read by TalkBack as one item; [large] in landscape's side column, which has the
 * room.
 */
@Composable
fun ModeAndTime(name: String, elapsedSeconds: Long, modifier: Modifier = Modifier, large: Boolean = false) {
    val time = stringResource(R.string.vinkit_time)
    val spoken = spokenElapsed(elapsedSeconds)
    val typography = MaterialTheme.typography
    Column(modifier.clearAndSetSemantics { contentDescription = "$name, $time, $spoken" }) {
        Text(
            name,
            style = if (large) typography.titleSmall else typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            formatElapsed(elapsedSeconds),
            style = if (large) typography.headlineMedium else typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** A game's own screen button beside Scores and Settings (BattleGrid's badges). */
data class NavigationAction(val icon: ImageVector, val label: String, val onClick: () -> Unit)

/** The game's own [navigation] buttons, then Scores and Settings, each shown only when its screen exists. */
@Composable
fun NavigationButtons(
    onOpenScores: (() -> Unit)?,
    onOpenSettings: (() -> Unit)?,
    navigation: List<NavigationAction> = emptyList(),
) {
    navigation.forEach { IconButton(onClick = it.onClick) { Icon(it.icon, it.label) } }
    onOpenScores?.let {
        IconButton(onClick = it) { Icon(Icons.Rounded.EmojiEvents, stringResource(R.string.vinkit_open_scores)) }
    }
    onOpenSettings?.let {
        IconButton(onClick = it) { Icon(Icons.Rounded.Settings, stringResource(R.string.vinkit_open_settings)) }
    }
}
