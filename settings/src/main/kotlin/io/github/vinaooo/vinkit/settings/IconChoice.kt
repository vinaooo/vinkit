package io.github.vinaooo.vinkit.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A setting with four or more options, as an Expressive connected button group: one icon per option, the chosen one a
 * filled pill that bounces when chosen, and under the group the chosen option's name and description, bouncing in.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> IconChoice(
    title: String,
    options: List<IconOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            modifier = Modifier.fillMaxWidth().selectableGroup(),
        ) {
            options.forEachIndexed { index, option ->
                val bounce = rememberBounce(option.value == selected)
                ToggleButton(
                    checked = option.value == selected,
                    onCheckedChange = { onSelect(option.value) },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(BUTTON_HEIGHT)
                        .graphicsLayer {
                            scaleX = bounce.value
                            scaleY = bounce.value
                        }
                        .semantics { role = Role.RadioButton },
                ) { Icon(option.icon, option.label) }
            }
        }
        // The new option's name and meaning bounce in from below as the old ones fade.
        AnimatedContent(
            targetState = options.first { it.value == selected },
            transitionSpec = { (slideInVertically(bouncy()) { it / 2 } + fadeIn()) togetherWith fadeOut() },
            label = "choice description",
        ) { option ->
            Column {
                Text(
                    option.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A button's scale: when it becomes [checked] it squashes, then springs back past its size. Only a change bounces. */
@Composable
private fun rememberBounce(checked: Boolean): Animatable<Float, AnimationVector1D> {
    val scale = remember { Animatable(1f) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(checked) {
        if (checked && shown) {
            scale.snapTo(SQUASH)
            scale.animateTo(1f, bouncy())
        }
        shown = true
    }
    return scale
}

private fun <T> bouncy() = spring<T>(dampingRatio = BOUNCE_DAMPING, stiffness = Spring.StiffnessMediumLow)

private const val SQUASH = 0.85f
private const val BOUNCE_DAMPING = 0.4f
private val BUTTON_HEIGHT = 56.dp
