package io.github.vinaooo.vinkit.settings

import androidx.compose.runtime.Composable

/** A card of the game's own settings, shown before vinkit's; build its rows with [Choice], [ToggleRow], [LinkRow]. */
class SettingsSection(val title: String, val content: @Composable () -> Unit)
