package io.github.vinaooo.vinkit.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The game's [actions] and then the new game menu ([menuOptions], plus "Report a bug" when [onReportBug] is set), in
 * a horizontal floating toolbar or, in landscape, a [vertical] one. [mirrored] (left hand) lays it out right to left,
 * so the menu sits at the thumb's end and its pills grow the other way. The toolbar owns whether the menu is open,
 * because it dims the game behind itself meanwhile.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameToolbar(
    actions: List<ToolbarAction>,
    menuOptions: List<MenuOption>,
    modifier: Modifier = Modifier,
    onReportBug: (() -> Unit)? = null,
    vertical: Boolean = false,
    mirrored: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val dim by animateFloatAsState(
        if (menuOpen) MENU_SCRIM_ALPHA else 0f,
        MaterialTheme.motionScheme.defaultEffectsSpec(),
    )
    val toolbarModifier = modifier.scrimBehind(MaterialTheme.colorScheme.scrim) { dim }
        .keepingEnd(vertical = vertical, hold = rememberMenuHold(menuOpen))
    val content: @Composable () -> Unit = {
        actions.forEach { action ->
            Hiding(menuOpen || !action.visible, vertical) { ActionButton(action, vertical) }
        }
        NewGameMenu(menuOpen, { menuOpen = it }, menuOptions, vertical, onReportBug)
    }
    CompositionLocalProvider(LocalLayoutDirection provides if (mirrored) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        if (vertical) {
            VerticalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                modifier = toolbarModifier,
            ) { content() }
        } else {
            HorizontalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                modifier = toolbarModifier,
            ) { content() }
        }
    }
}

/**
 * A button that grows out of its slot, widening the toolbar, and scales into place, or leaves the same way when
 * [hidden] (while the menu is open every button but its close button leaves). Only along the toolbar: growing across
 * it too leaves the toolbar's balanced padding, and so its thickness, wrong.
 */
@Composable
private fun Hiding(hidden: Boolean, vertical: Boolean, content: @Composable () -> Unit) {
    val size = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val scale = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val enter = if (vertical) {
        expandVertically(size, Alignment.CenterVertically)
    } else {
        expandHorizontally(size, Alignment.CenterHorizontally)
    } + scaleIn(scale) + fadeIn(fade)
    val exit = if (vertical) {
        shrinkVertically(size, Alignment.CenterVertically)
    } else {
        shrinkHorizontally(size, Alignment.CenterHorizontally)
    } + scaleOut(scale) + fadeOut(fade)
    AnimatedVisibility(!hidden, enter = enter, exit = exit) { content() }
}

@Composable
private fun ActionButton(action: ToolbarAction, vertical: Boolean) {
    when (action) {
        is ToolbarAction.Button -> TipBox(action.tip, action.icon, vertical) {
            IconButton(onClick = action.onClick, enabled = action.enabled) {
                Crossfade(
                    action.icon to action.label,
                    animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                    label = "action",
                ) { (icon, label) ->
                    if (action.keepDirection) LtrIcon { Icon(icon, label) } else Icon(icon, label)
                }
            }
        }
        is ToolbarAction.Toggle -> IconToggleButton(
            checked = action.checked,
            onCheckedChange = action.onCheckedChange,
            enabled = action.enabled,
            colors = IconButtonDefaults.iconToggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Icon(if (action.checked) action.checkedIcon else action.icon, action.label)
        }
    }
}

/** Draws [icon] left to right whatever the toolbar's direction. */
@Composable
private fun LtrIcon(icon: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = icon)
}

/**
 * [content], with [tip]'s bubble (and the button's [icon]) pointing at it when there is one. The tooltip box stays
 * around the button with or without a tip: dropping it when the tip goes (on the very press that closes the bubble)
 * would rebuild the button under the finger and cancel that press.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TipBox(tip: ToolbarTip?, icon: ImageVector, vertical: Boolean, content: @Composable () -> Unit) {
    val state = rememberTooltipState(isPersistent = true)
    // The last text, so the bubble keeps it while it fades out after the tip is gone.
    var text by remember { mutableStateOf("") }
    val onShown by rememberUpdatedState(tip?.onShown)
    LaunchedEffect(tip?.text) {
        text = tip?.text ?: return@LaunchedEffect
        try {
            withTimeoutOrNull(TIP_MILLIS) { state.show() }
        } finally {
            state.dismiss()
            onShown?.invoke()
        }
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            if (vertical) TooltipAnchorPosition.Left else TooltipAnchorPosition.Above,
            // Clears the toolbar's own padding around the button, so the bubble floats just off the toolbar.
            spacingBetweenTooltipAndAnchor = 16.dp,
        ),
        tooltip = {
            // Expressive: a pill in the accent (tertiary) color, with the button's icon and emphasized text.
            PlainTooltip(
                caretShape = TooltipDefaults.caretShape(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shadowElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text, style = MaterialTheme.typography.labelLargeEmphasized)
                }
            }
        },
        state = state,
        // Not focusable: the tap that lands on the button (or anywhere) closes the bubble and still does its job,
        // instead of being spent on closing it.
        focusable = false,
        // Only a tip shows the bubble, never a long press.
        enableUserInput = false,
        content = content,
    )
}

private const val TIP_MILLIS = 5_000L
