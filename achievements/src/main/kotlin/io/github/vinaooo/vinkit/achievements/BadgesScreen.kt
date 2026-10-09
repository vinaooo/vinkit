package io.github.vinaooo.vinkit.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.designsystem.R as DesignR

/** Every one of the game's [badges], earned or locked, on a screen of its own. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(badges: List<Badge>, unlocked: Set<String>, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vinkit_badges)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(DesignR.string.vinkit_back))
                    }
                },
            )
        },
    ) { padding ->
        BadgesList(badges, unlocked, Modifier.fillMaxSize().padding(padding), PaddingValues(BADGES_PADDING))
    }
}

/** Earned badges in the theme's primary color, locked ones dimmed with a lock; each read as one item. */
@Composable
fun BadgesList(
    badges: List<Badge>,
    unlocked: Set<String>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(badges, key = { it.key }) { badge ->
            val earned = badge.key in unlocked
            val spoken = stringResource(
                if (earned) R.string.vinkit_badge_unlocked else R.string.vinkit_badge_locked,
                badge.name,
                badge.note,
            )
            val color = if (earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                Modifier.clearAndSetSemantics { contentDescription = spoken },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (earned) Icons.Rounded.MilitaryTech else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = color,
                )
                Column {
                    Text(badge.name, style = MaterialTheme.typography.titleMedium, color = color)
                    Text(badge.note, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private val BADGES_PADDING = 16.dp
