package io.github.vinaooo.vinkit.scores

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.designsystem.VinkitTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScoresScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(state: ScoresUiState, onSelect: (String) -> Unit = {}) = compose.setContent {
        VinkitTheme(ThemeColor.BLUE) {
            ScoresScreen(
                uiState = state,
                onBack = {},
                modeName = { it.lowercase() },
                onSelectMode = onSelect,
                details = { "${it.extras["mistakes"]} mistakes" },
            )
        }
    }

    @Test
    fun `tabs name the modes, and a row shows points, time and the game's details`() {
        var chosen: String? = null
        show(
            ScoresUiState(
                scores = listOf(ScoreRecord("EASY", 900, 65, 0, mapOf("mistakes" to "2"))),
                stats = GameStats(played = 4, won = 3),
                isLoading = false,
                modes = listOf("EASY", "HARD"),
                mode = "EASY",
            ),
        ) { chosen = it }
        compose.onNodeWithText("900").assertExists()
        compose.onNodeWithText("1:05").assertExists()
        compose.onNodeWithText("2 mistakes", substring = true).assertExists()
        compose.onNodeWithContentDescription("Win rate, 75%").assertExists()
        compose.onNodeWithText("hard").performClick()
        chosen shouldBe "HARD"
    }

    @Test
    fun `a mode played but never won says how to get scores`() {
        show(ScoresUiState(isLoading = false, modes = listOf("EASY"), mode = "EASY", stats = GameStats(played = 1)))
        compose.onNodeWithText("easy").assertExists()
        compose.onNodeWithText("Win a game to see your scores here.").assertExists()
    }
}
