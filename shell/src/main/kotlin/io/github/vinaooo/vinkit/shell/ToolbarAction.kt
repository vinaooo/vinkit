package io.github.vinaooo.vinkit.shell

import androidx.compose.ui.graphics.vector.ImageVector

/** A toolbar button. One not [visible] (Solo's auto-complete, until it can play) leaves the toolbar, shrinking it. */
sealed interface ToolbarAction {
    val label: String
    val enabled: Boolean
    val visible: Boolean

    /**
     * A plain button. A new [icon] and [label] crossfade in (a hint that turns into "apply"). [keepDirection] draws
     * the icon left to right even in the left-handed (mirrored) toolbar, so undo and redo keep pointing their way.
     * A [tip] points the button out once.
     */
    data class Button(
        val icon: ImageVector,
        override val label: String,
        override val enabled: Boolean = true,
        val keepDirection: Boolean = false,
        override val visible: Boolean = true,
        val tip: ToolbarTip? = null,
        val onClick: () -> Unit,
    ) : ToolbarAction

    /** An on/off button (notes mode): [checkedIcon] while on. TalkBack reads it as a switch. */
    data class Toggle(
        val icon: ImageVector,
        val checkedIcon: ImageVector,
        override val label: String,
        val checked: Boolean,
        override val enabled: Boolean = true,
        override val visible: Boolean = true,
        val onCheckedChange: (Boolean) -> Unit,
    ) : ToolbarAction
}

/**
 * A bubble with [text] pointing at a button (from above, or from the side of a vertical toolbar), shown as soon as the
 * button is, until the player taps anywhere or a few seconds pass; then [onShown], so the game stops passing it.
 */
data class ToolbarTip(val text: String, val onShown: () -> Unit)
