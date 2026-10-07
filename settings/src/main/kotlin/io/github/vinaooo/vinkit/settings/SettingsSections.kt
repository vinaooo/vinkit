package io.github.vinaooo.vinkit.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.core.BoardAlignment
import io.github.vinaooo.vinkit.core.Handedness
import io.github.vinaooo.vinkit.core.PhoneViewSide
import io.github.vinaooo.vinkit.core.ThemeMode
import io.github.vinaooo.vinkit.designsystem.ColorChoice

private const val TABLET_WIDTH_DP = 600

@Composable
internal fun AppearanceSection(settings: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    Choice(
        title = stringResource(R.string.vinkit_theme),
        options = listOf(
            ThemeMode.SYSTEM to stringResource(R.string.vinkit_theme_system),
            ThemeMode.LIGHT to stringResource(R.string.vinkit_theme_light),
            ThemeMode.DARK to stringResource(R.string.vinkit_theme_dark),
        ),
        selected = settings.themeMode,
        onSelect = { value -> onChange { it.copy(themeMode = value) } },
    )
    val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    if (dynamicColorAvailable) {
        ToggleRow(
            title = stringResource(R.string.vinkit_dynamic_color),
            supporting = stringResource(R.string.vinkit_dynamic_color_note),
            checked = settings.dynamicColor,
            onToggle = { value -> onChange { it.copy(dynamicColor = value) } },
        )
    }
    // Revealed from behind the switch when it's turned off; always there without dynamic color.
    Revealed(visible = !dynamicColorAvailable || !settings.dynamicColor) {
        ColorChoice(settings.themeColor, settings.themeMode, onSelect = { value ->
            onChange { it.copy(themeColor = value) }
        })
    }
    Choice(
        title = stringResource(R.string.vinkit_handedness),
        options = listOf(
            Handedness.LEFT to stringResource(R.string.vinkit_hand_left),
            Handedness.RIGHT to stringResource(R.string.vinkit_hand_right),
        ),
        selected = settings.handedness,
        onSelect = { value -> onChange { it.copy(handedness = value) } },
    )
    Choice(
        title = stringResource(R.string.vinkit_board_alignment),
        options = listOf(
            BoardAlignment.TOP to stringResource(R.string.vinkit_board_top),
            BoardAlignment.BOTTOM to stringResource(R.string.vinkit_board_bottom),
        ),
        selected = settings.boardAlignment,
        onSelect = { value -> onChange { it.copy(boardAlignment = value) } },
    )
    PhoneViewRows(settings, onChange)
}

@Composable
private fun PhoneViewRows(settings: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    // Only a tablet (Material's medium window and up) has room to spare; a phone already shows a phone's board.
    if (LocalConfiguration.current.smallestScreenWidthDp < TABLET_WIDTH_DP) return
    ToggleRow(
        title = stringResource(R.string.vinkit_phone_view),
        supporting = stringResource(R.string.vinkit_phone_view_note),
        checked = settings.phoneView,
        onToggle = { value -> onChange { it.copy(phoneView = value) } },
    )
    // Revealed from behind the switch above it, as if it had been tucked under it.
    Revealed(visible = settings.phoneView) {
        Choice(
            title = stringResource(R.string.vinkit_phone_view_side),
            options = listOf(
                PhoneViewSide.LEFT to stringResource(R.string.vinkit_side_left),
                PhoneViewSide.CENTER to stringResource(R.string.vinkit_side_center),
                PhoneViewSide.RIGHT to stringResource(R.string.vinkit_side_right),
            ),
            selected = settings.phoneViewSide,
            onSelect = { value -> onChange { it.copy(phoneViewSide = value) } },
        )
    }
}

@Composable
private fun Revealed(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec(), Alignment.Top) +
            fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec(), Alignment.Top) +
            fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
    ) { content() }
}

@Composable
internal fun FeedbackSection(settings: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    ToggleRow(stringResource(R.string.vinkit_sound), settings.soundEnabled, { value ->
        onChange { it.copy(soundEnabled = value) }
    })
    ToggleRow(stringResource(R.string.vinkit_haptics), settings.hapticsEnabled, { value ->
        onChange { it.copy(hapticsEnabled = value) }
    })
}

/**
 * The privacy policy link, which Google Play requires inside the app, and the consent form, which is offered only
 * where the law requires a way to change ad consent (GDPR, some US states).
 */
@Composable
internal fun PrivacySection(
    privacyOptionsRequired: Boolean,
    onOpenPrivacyOptions: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    LinkRow(stringResource(R.string.vinkit_privacy_policy), onClick = onOpenPrivacyPolicy)
    if (privacyOptionsRequired) {
        LinkRow(
            stringResource(R.string.vinkit_privacy_options),
            supporting = stringResource(R.string.vinkit_privacy_options_note),
            onClick = onOpenPrivacyOptions,
        )
    }
}
