package io.github.vinaooo.vinkit.shell

import androidx.compose.runtime.compositionLocalOf

/** How the frame is laid out; what the slots learn about it. */
data class FrameInfo(
    /** Landscape: the info and the controls sit beside the board. */
    val landscape: Boolean,
    /** A large landscape screen (1000dp+), with room for bigger controls. */
    val large: Boolean,
    /** Left hand: controls and toolbar mirror, the toolbar's menu at the thumb's end. */
    val mirrored: Boolean,
)

/** The frame's layout, for a board that changes with it (Solo lays its cards sideways in landscape). */
val LocalFrameInfo = compositionLocalOf { FrameInfo(landscape = false, large = false, mirrored = false) }
