package io.github.vinaooo.vinkit.shell

import androidx.compose.ui.graphics.vector.ImageVector

/** A toolbar button. */
sealed interface ToolbarAction {
    val label: String
    val enabled: Boolean

    /**
     * A plain button. A new [icon] and [label] crossfade in (a hint that turns into "apply"). [keepDirection] draws
     * the icon left to right even in the left-handed (mirrored) toolbar, so undo and redo keep pointing their way.
     */
    data class Button(
        val icon: ImageVector,
        override val label: String,
        override val enabled: Boolean = true,
        val keepDirection: Boolean = false,
        val onClick: () -> Unit,
    ) : ToolbarAction

    /** An on/off button (notes mode): [checkedIcon] while on. TalkBack reads it as a switch. */
    data class Toggle(
        val icon: ImageVector,
        val checkedIcon: ImageVector,
        override val label: String,
        val checked: Boolean,
        override val enabled: Boolean = true,
        val onCheckedChange: (Boolean) -> Unit,
    ) : ToolbarAction
}
