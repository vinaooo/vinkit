package io.github.vinaooo.vinkit.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.roundToIntRect
import io.github.vinaooo.vinkit.bugreport.BugReportDialog
import io.github.vinaooo.vinkit.bugreport.GameReport
import io.github.vinaooo.vinkit.bugreport.ReportTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The game screen's background. It records what it draws, so "Report a bug" can attach a screenshot of the game as it
 * was once the menu has closed; it tells the new game menu where the game lies ([LocalGameArea]); and it holds the
 * TalkBack [Announcer]. [color] is its background (Solo: the table's). [content] gets the report action to pass to
 * [GameToolbar]; null without a [reportTarget].
 */
@Composable
fun GameSurface(
    announcement: String?,
    announcementSequence: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    reportTarget: ReportTarget? = null,
    gameReport: () -> GameReport = { GameReport() },
    content: @Composable (reportBug: (() -> Unit)?) -> Unit,
) {
    val frame = rememberGraphicsLayer()
    var screenshot by remember { mutableStateOf<Screenshot?>(null) }
    val scope = rememberCoroutineScope()
    val reportBug: (() -> Unit)? = reportTarget?.let {
        {
            // After the menu and its scrim have gone, so the screenshot shows the game as it was.
            scope.launch {
                delay(MENU_CLOSED_MILLIS)
                screenshot = Screenshot(frame.toImageBitmap())
            }
        }
    }
    var area by remember { mutableStateOf(IntRect.Zero) }
    Surface(
        modifier = modifier.fillMaxSize()
            .onGloballyPositioned { area = it.boundsInWindow().roundToIntRect() }
            .drawWithContent {
                frame.record { this@drawWithContent.drawContent() }
                drawLayer(frame)
            },
        color = color,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        // Surface stretches each direct child to its full size, so the tiny announcer sits in a Box of its own.
        Box {
            CompositionLocalProvider(LocalGameArea provides area) { content(reportBug) }
            Announcer(announcement, announcementSequence)
        }
    }
    val shot = screenshot
    if (reportTarget != null && shot != null) {
        BugReportDialog(reportTarget, shot.image, onDone = { screenshot = null }, gameReport = gameReport)
    }
}

/** Wrapped, so taking two screenshots in a row still shows the dialog again. */
private class Screenshot(val image: ImageBitmap)

private const val MENU_CLOSED_MILLIS = 400L
