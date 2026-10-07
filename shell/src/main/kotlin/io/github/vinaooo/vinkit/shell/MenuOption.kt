package io.github.vinaooo.vinkit.shell

import androidx.compose.ui.graphics.vector.ImageVector

/** One option of the new game menu ("New game", "Restart this board"). */
data class MenuOption(val icon: ImageVector, val label: String, val onClick: () -> Unit)
