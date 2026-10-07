package io.github.vinaooo.vinkit.bugreport

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * "Report a bug": the player describes it, then sends it by email to [target] (with the [screenshot] and the game's
 * files attached) or opens a prefilled GitHub issue (text only). [gameReport] is read when the report is sent.
 */
@Composable
fun BugReportDialog(target: ReportTarget, screenshot: ImageBitmap?, onDone: () -> Unit, gameReport: () -> GameReport) {
    val context = LocalContext.current
    var description by rememberSaveable { mutableStateOf("") }
    val subject =
        stringResource(R.string.vinkit_report_subject, context.applicationInfo.loadLabel(context.packageManager))
    fun body(game: GameReport) = reportBody(context.reportInfo(), game, description)
    AlertDialog(
        onDismissRequest = onDone,
        icon = { Icon(Icons.Rounded.BugReport, contentDescription = null) },
        title = { Text(stringResource(R.string.vinkit_report_bug)) },
        text = {
            Column {
                Text(stringResource(R.string.vinkit_report_bug_body))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.vinkit_report_bug_hint)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val game = gameReport()
                context.emailReport(target.email, subject, body(game), screenshot?.asAndroidBitmap(), game.files)
                onDone()
            }) { Text(stringResource(R.string.vinkit_report_by_email)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDone) { Text(stringResource(R.string.vinkit_report_cancel)) }
                TextButton(onClick = {
                    val title =
                        description.lineSequence().firstOrNull()?.take(TITLE_LENGTH)?.ifBlank { null } ?: subject
                    val url = githubIssueUrl(target, title, body(gameReport()))
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    onDone()
                }) { Text(stringResource(R.string.vinkit_report_on_github)) }
            }
        },
    )
}

private const val TITLE_LENGTH = 70
