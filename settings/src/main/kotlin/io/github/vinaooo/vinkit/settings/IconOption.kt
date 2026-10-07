package io.github.vinaooo.vinkit.settings

import androidx.compose.ui.graphics.vector.ImageVector

/** One option of an [IconChoice]: its icon, its name (also what TalkBack reads) and what it means. */
class IconOption<T>(val value: T, val icon: ImageVector, val label: String, val description: String)
