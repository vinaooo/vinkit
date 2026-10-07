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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

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
        actions.forEach { action -> HidingWhileMenuOpen(menuOpen, vertical) { ActionButton(action) } }
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

/** While the menu is open every button but its close button leaves, only along the toolbar. */
@Composable
private fun HidingWhileMenuOpen(menuOpen: Boolean, vertical: Boolean, content: @Composable () -> Unit) {
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
    AnimatedVisibility(!menuOpen, enter = enter, exit = exit) { content() }
}

@Composable
private fun ActionButton(action: ToolbarAction) {
    when (action) {
        is ToolbarAction.Button -> IconButton(onClick = action.onClick, enabled = action.enabled) {
            Crossfade(
                action.icon to action.label,
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "action",
            ) { (icon, label) ->
                if (action.keepDirection) LtrIcon { Icon(icon, label) } else Icon(icon, label)
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
