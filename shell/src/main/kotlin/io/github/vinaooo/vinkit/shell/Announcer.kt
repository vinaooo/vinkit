package io.github.vinaooo.vinkit.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Speaks [text] through TalkBack, from an invisible live region: a live region is read whenever its text changes.
 * (`View.announceForAccessibility` is deprecated.) [sequence] numbers the announcements, so saying the same thing
 * twice in a row (two undos) is still spoken. Keep it out of a `Surface`'s direct children: the Surface would stretch
 * it over the whole screen ([GameSurface] already holds one).
 */
@Composable
fun Announcer(text: String?, sequence: Int, modifier: Modifier = Modifier) {
    // Every other announcement ends with a character that isn't read aloud, so the text always changes.
    val spoken = if (text != null && sequence % 2 == 1) text + ZERO_WIDTH_SPACE else text.orEmpty()
    Box(
        modifier = modifier
            .size(1.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = spoken
            },
    )
}

private const val ZERO_WIDTH_SPACE = "​"
