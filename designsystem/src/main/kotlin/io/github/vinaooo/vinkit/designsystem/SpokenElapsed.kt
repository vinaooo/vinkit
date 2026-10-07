package io.github.vinaooo.vinkit.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource

/**
 * A game duration the way TalkBack should say it, such as "1 minute 5 seconds", because "1:05" is read as a clock
 * time or as two numbers. Parts that are zero are left out, except for a zero duration.
 */
@Composable
fun spokenElapsed(seconds: Long): String {
    val hours = (seconds / SECONDS_PER_HOUR).toInt()
    val minutes = (seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE).toInt()
    val secs = (seconds % SECONDS_PER_MINUTE).toInt()
    val parts = buildList {
        if (hours > 0) add(pluralStringResource(R.plurals.vinkit_elapsed_hours, hours, hours))
        if (minutes > 0) add(pluralStringResource(R.plurals.vinkit_elapsed_minutes, minutes, minutes))
        if (secs > 0 || hours == 0 && minutes == 0) {
            add(pluralStringResource(R.plurals.vinkit_elapsed_seconds, secs, secs))
        }
    }
    return parts.joinToString(" ")
}

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3_600
