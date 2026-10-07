package io.github.vinaooo.vinkit.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.designsystem.R as DesignR

/**
 * The Settings screen: the game's [gameSections] first, then Appearance, Feedback and Privacy. [onChange] receives a
 * transform of the current [settings] (pass it to `AppSettingsRepository.update`). From 600dp wide, two columns: the
 * game's sections, Feedback and Privacy on the left, Appearance on the right, at most 1040dp wide.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onChange: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    gameSections: List<SettingsSection> = emptyList(),
    privacyOptionsRequired: Boolean = false,
    onOpenPrivacyOptions: () -> Unit = {},
) {
    val game = @Composable { gameSections.forEach { SectionCard(it.title) { it.content() } } }
    val appearance = @Composable {
        SectionCard(stringResource(R.string.vinkit_section_appearance)) { AppearanceSection(settings, onChange) }
    }
    val feedback = @Composable {
        SectionCard(stringResource(R.string.vinkit_section_feedback)) { FeedbackSection(settings, onChange) }
    }
    val privacy = @Composable {
        SectionCard(stringResource(R.string.vinkit_section_privacy)) {
            PrivacySection(privacyOptionsRequired, onOpenPrivacyOptions, onOpenPrivacyPolicy)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = { SettingsTopBar(onBack) },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val scroll = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            val spaced = Arrangement.spacedBy(16.dp)
            if (maxWidth >= TWO_COLUMNS_WIDTH.dp) {
                Box(scroll, contentAlignment = Alignment.TopCenter) {
                    Row(
                        modifier = Modifier.widthIn(max = MAX_WIDTH.dp)
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = spaced,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = spaced) {
                            game()
                            feedback()
                            privacy()
                        }
                        Column(Modifier.weight(1f)) { appearance() }
                    }
                }
            } else {
                Column(scroll.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = spaced) {
                    game()
                    appearance()
                    feedback()
                    privacy()
                }
            }
        }
    }
}

/**
 * For a game setting that changes the mode of a game in progress. [text] is "This will start a new game. The current
 * game counts as a loss." unless the game says otherwise (a game that isn't recorded).
 */
@Composable
fun NewGameConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String = stringResource(R.string.vinkit_new_game_confirm_text),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.vinkit_new_game_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.vinkit_new_game_cancel)) }
        },
    )
}

private const val TWO_COLUMNS_WIDTH = 600
private const val MAX_WIDTH = 1040

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            SectionTitle(title)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.vinkit_settings_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(DesignR.string.vinkit_back))
            }
        },
    )
}
