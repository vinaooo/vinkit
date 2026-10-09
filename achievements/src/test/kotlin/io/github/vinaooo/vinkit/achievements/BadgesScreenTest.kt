package io.github.vinaooo.vinkit.achievements

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.designsystem.VinkitTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BadgesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val badges = listOf(
        Badge("FIRST_WIN", "First win", "Win a game."),
        Badge("BEAT_HARD", "Admiral", "Beat the hard AI."),
    )

    @Test
    fun `each badge is read as one item, earned or locked`() {
        var back = false
        compose.setContent {
            VinkitTheme(ThemeColor.BLUE) { BadgesScreen(badges, setOf("FIRST_WIN"), onBack = { back = true }) }
        }

        compose.onNodeWithContentDescription("First win, earned: Win a game.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Admiral, locked: Beat the hard AI.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        back shouldBe true
    }
}
