package io.github.vinaooo.vinkit.bugreport

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder

/** Where an app's reports go: an email address (the contact in its privacy policy) and its GitHub repository. */
data class ReportTarget(val email: String, val githubRepo: String) {
    val issuesUrl: String get() = "https://github.com/$githubRepo/issues/new"
}

/**
 * What the game adds to a report: one line per fact ("Settings: …", "Game: …"), the exact state as short text
 * (`GameCodec`) for a GitHub issue, and files the email attaches, such as `game.json`.
 */
data class GameReport(
    val details: List<String> = emptyList(),
    val state: String? = null,
    val files: Map<String, String> = emptyMap(),
)

/** What a bug report says about the app and the device; nothing personal. */
data class ReportInfo(val appVersion: String, val android: String, val device: String, val screen: String)

/** The report's text: the player's [description], then the facts that help reproduce the bug. */
fun reportBody(info: ReportInfo, game: GameReport, description: String): String = buildString {
    appendLine(description.ifBlank { "(no description)" })
    appendLine()
    appendLine("---")
    appendLine("App: ${info.appVersion}")
    appendLine("Android: ${info.android}")
    appendLine("Device: ${info.device}")
    appendLine("Screen: ${info.screen}")
    game.details.forEach(::appendLine)
    game.state?.let {
        // The exact state, for the debug build to replay.
        appendLine()
        appendLine("State:")
        appendLine("```")
        appendLine(it)
        appendLine("```")
    }
}.trimEnd()

/**
 * A new GitHub issue with [title] and [body] filled in, for the player to review and submit. Only the text fits a
 * link: the screenshot and the files go by email.
 */
fun githubIssueUrl(target: ReportTarget, title: String, body: String): String =
    "${target.issuesUrl}?title=${URLEncoder.encode(title, UTF_8)}&body=${URLEncoder.encode(body, UTF_8)}"

/** Facts about this app and device. */
fun Context.reportInfo(): ReportInfo {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)

    @Suppress("DEPRECATION") // longVersionCode needs API 28; minSdk is 26.
    val build = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.P
    ) {
        packageInfo.longVersionCode
    } else {
        packageInfo.versionCode
    }
    val config = resources.configuration
    return ReportInfo(
        appVersion = "${packageInfo.versionName} ($build)",
        android = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        device = "${Build.MANUFACTURER} ${Build.MODEL}",
        screen = "${config.screenWidthDp}x${config.screenHeightDp}dp, ${config.densityDpi}dpi",
    )
}

/**
 * Sends the report to [email] through the player's email app, the [screenshot] and the game's [files] attached.
 */
internal fun Context.emailReport(
    email: String,
    subject: String,
    body: String,
    screenshot: Bitmap?,
    files: Map<String, String>,
) {
    val uris = attachments(screenshot, files).map { FileProvider.getUriForFile(this, "$packageName.reports", it) }
    val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        // A concrete type: Gmail takes several attachments of any type except "*/*".
        type = "multipart/mixed"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    // The read permission reaches the email app through the clip data.
    uris.toClipData()?.let { send.clipData = it }
    // Straight to the email apps (the ones that open mailto: links), the report addressed and attached: the only one
    // directly, or a choice among them. A mailto: selector can't do this: Android refuses it with several attachments.
    val direct = emailApps().map { Intent(send).setPackage(it) }
    when {
        direct.size == 1 -> startActivity(direct.single())
        direct.isNotEmpty() -> startActivity(
            Intent.createChooser(direct.first(), null)
                .putExtra(Intent.EXTRA_INITIAL_INTENTS, direct.drop(1).toTypedArray()),
        )
        // No email app set up: any app that can send the files.
        else -> startActivity(Intent.createChooser(send, null))
    }
}

/** The packages of the apps that open mailto: links. */
private fun Context.emailApps(): List<String> =
    packageManager.queryIntentActivities(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), 0)
        .map { it.activityInfo.packageName }
        .distinct()

/** The screenshot and the files, written to a fresh folder the file provider shares. */
private fun Context.attachments(screenshot: Bitmap?, files: Map<String, String>): List<File> {
    val dir = File(cacheDir, REPORTS_DIR).apply {
        deleteRecursively()
        mkdirs()
    }
    val shot = screenshot?.let { image ->
        File(dir, "screenshot.png").also { file ->
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        }
    }
    // Only the name: a file never lands outside the reports folder.
    val written = files.map { (name, text) -> File(dir, File(name).name).also { it.writeText(text) } }
    return listOfNotNull(shot) + written
}

private fun List<Uri>.toClipData(): ClipData? = firstOrNull()?.let { first ->
    ClipData.newRawUri(null, first).also { clip -> drop(1).forEach { clip.addItem(ClipData.Item(it)) } }
}

private const val REPORTS_DIR = "reports"
private const val UTF_8 = "UTF-8"

/** PNG is lossless; the quality is ignored but required. */
private const val PNG_QUALITY = 100
