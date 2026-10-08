package io.github.vinaooo.vinkit.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.core.BoardAlignment
import io.github.vinaooo.vinkit.core.Handedness
import io.github.vinaooo.vinkit.core.PhoneViewSide

/**
 * The game screen's layout, the same in every vinkit game, from the player's [settings] (hand, board position, phone
 * view). Portrait: [info] (mode and clock, or a hint in their place) with the Scores and Settings buttons in a fixed
 * top region, the [board] as the largest [boardAspectRatio] box at the top or bottom of its room, then [controls]
 * (a number pad; optional) and the [toolbar]. Landscape: [info] and the buttons on one side, the board centered at
 * full height, the controls and a vertical toolbar on the preferred hand's side. On a tablet, phone view keeps
 * board, controls and toolbar in one phone-wide column on the chosen side. A null [boardAspectRatio] gives the board
 * all of its room (Solo: its layout sizes the cards from it and places itself).
 */
@Composable
fun GameFrame(
    settings: AppSettings,
    info: @Composable (FrameInfo) -> Unit,
    board: @Composable () -> Unit,
    toolbar: @Composable (FrameInfo) -> Unit,
    modifier: Modifier = Modifier,
    controls: (@Composable (FrameInfo) -> Unit)? = null,
    onOpenScores: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    boardAspectRatio: Float? = 1f,
) {
    val phoneView = settings.phoneView && LocalConfiguration.current.smallestScreenWidthDp >= TABLET_WIDTH_DP
    val slots =
        Slots(info, board, toolbar, controls, { NavigationButtons(onOpenScores, onOpenSettings) }, boardAspectRatio)
    BoxWithConstraints(modifier.fillMaxSize().safeDrawingPadding()) {
        val frame = FrameInfo(
            // Phone view keeps its column in landscape too.
            landscape = maxWidth > maxHeight && !phoneView,
            large = maxWidth >= LARGE_LANDSCAPE,
            mirrored = settings.handedness == Handedness.LEFT,
        )
        CompositionLocalProvider(LocalFrameInfo provides frame) {
            when {
                frame.landscape -> LandscapeFrame(slots, frame)
                phoneView -> PhoneViewFrame(slots, frame, settings)
                else -> PortraitFrame(slots, frame, settings.boardAlignment)
            }
        }
    }
}

private class Slots(
    val info: @Composable (FrameInfo) -> Unit,
    val board: @Composable () -> Unit,
    val toolbar: @Composable (FrameInfo) -> Unit,
    val controls: (@Composable (FrameInfo) -> Unit)?,
    val navigation: @Composable () -> Unit,
    val aspectRatio: Float?,
)

@Composable
private fun TopRegion(slots: Slots, frame: FrameInfo) {
    // The info may change (a hint in place of the clock); the region's height doesn't, so the board never moves.
    Row(
        Modifier.fillMaxWidth().height(TOP_REGION).padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).padding(start = 16.dp)) { slots.info(frame) }
        slots.navigation()
    }
}

@Composable
private fun PortraitFrame(slots: Slots, frame: FrameInfo, alignment: BoardAlignment) {
    Column(Modifier.fillMaxSize()) {
        TopRegion(slots, frame)
        BoardBox(
            slots,
            BiasAlignment(0f, if (alignment == BoardAlignment.BOTTOM) 1f else -1f),
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
        )
        // On a tablet the controls would stretch across the screen: they keep a phone's proportions.
        Column(
            Modifier.widthIn(max = PORTRAIT_CONTROLS_MAX_WIDTH).align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            slots.controls?.invoke(frame)
            Box(Modifier.padding(vertical = 12.dp)) { slots.toolbar(frame) }
        }
    }
}

@Composable
private fun PhoneViewFrame(slots: Slots, frame: FrameInfo, settings: AppSettings) {
    val horizontal = when (settings.phoneViewSide) {
        PhoneViewSide.LEFT -> -1f
        PhoneViewSide.CENTER -> 0f
        PhoneViewSide.RIGHT -> 1f
    }
    val vertical = if (settings.boardAlignment == BoardAlignment.BOTTOM) 1f else -1f
    Column(Modifier.fillMaxSize()) {
        TopRegion(slots, frame)
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = BiasAlignment(horizontal, vertical)) {
            Column(Modifier.width(PHONE_WIDTH), horizontalAlignment = Alignment.CenterHorizontally) {
                // Measured after the controls and toolbar: a short screen shrinks the board.
                val ratio = slots.aspectRatio
                Box(
                    if (ratio == null) {
                        Modifier.fillMaxWidth().weight(1f)
                    } else {
                        Modifier.weight(1f, fill = false).aspectRatio(ratio, matchHeightConstraintsFirst = true)
                    }.padding(horizontal = 8.dp, vertical = 4.dp),
                ) { slots.board() }
                slots.controls?.invoke(frame)
                Box(Modifier.padding(vertical = 12.dp)) { slots.toolbar(frame) }
            }
        }
    }
}

@Composable
private fun LandscapeFrame(slots: Slots, frame: FrameInfo) {
    val side: @Composable () -> Unit = {
        Column(
            Modifier.width(SIDE_WIDTH).fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            slots.info(frame)
            Spacer(Modifier.weight(1f))
            Row { slots.navigation() }
        }
    }
    val controls: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val toolbar: @Composable () -> Unit = { Box(Modifier.padding(horizontal = 8.dp)) { slots.toolbar(frame) } }
            if (frame.mirrored) toolbar()
            slots.controls?.invoke(frame)
            if (!frame.mirrored) toolbar()
        }
    }
    CenteredRow(
        modifier = Modifier.fillMaxSize(),
        start = if (frame.mirrored) controls else side,
        center = { BoardBox(slots, Alignment.Center, Modifier.fillMaxSize().padding(8.dp)) },
        end = if (frame.mirrored) side else controls,
    )
}

/** The board as the largest box of its aspect ratio that fits, placed by [alignment] in its room; or all of it. */
@Composable
private fun BoardBox(slots: Slots, alignment: Alignment, modifier: Modifier) {
    val ratio = slots.aspectRatio ?: return Box(modifier) { slots.board() }
    BoxWithConstraints(modifier, contentAlignment = alignment) {
        val (width, height) = fit(maxWidth, maxHeight, ratio)
        Box(Modifier.size(width, height)) { slots.board() }
    }
}

/** The largest width and height of [ratio] (width over height) inside [maxWidth] by [maxHeight]. */
internal fun fit(maxWidth: Dp, maxHeight: Dp, ratio: Float): Pair<Dp, Dp> =
    if (maxWidth / maxHeight > ratio) maxHeight * ratio to maxHeight else maxWidth to maxWidth / ratio

/** Holds the info or, in its place, a hint: the board below never moves. */
private val TOP_REGION = 88.dp
private val SIDE_WIDTH = 200.dp

/** On a tablet in landscape, controls have room to grow. */
private val LARGE_LANDSCAPE = 1000.dp

/** A phone's width for the controls: on a tablet in portrait they keep it, centered. */
private val PORTRAIT_CONTROLS_MAX_WIDTH = 480.dp

/** Phone view's width: a typical modern phone's (412dp). */
private val PHONE_WIDTH = 412.dp

/** From this short side (Material's medium window), the screen is a tablet's and phone view applies. */
private const val TABLET_WIDTH_DP = 600
