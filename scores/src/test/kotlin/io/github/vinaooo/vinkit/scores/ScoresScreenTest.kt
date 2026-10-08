package io.github.vinaooo.vinkit.scores

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.designsystem.VinkitTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
class ScoresScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(
        state: ScoresUiState,
        ranked: Boolean = true,
        onSelect: (String) -> Unit = {
        },
    ) = compose.setContent {
        VinkitTheme(ThemeColor.BLUE) {
            ScoresScreen(
                uiState = state,
                onBack = {},
                modeName = { it.lowercase() },
                onSelectGroup = onSelect,
                details = { "${it.extras["mistakes"]} mistakes" },
                ranked = ranked,
                points = {
                    if (it.mode ==
                        "VEGAS"
                    ) {
                        ScorePoints("$${it.points}", "${it.points} dollars")
                    } else {
                        ScorePoints("${it.points}")
                    }
                },
            )
        }
    }

    @Test
    fun `tabs name the modes, and a row shows points, time and the game's details`() {
        var chosen: String? = null
        show(
            ScoresUiState(
                isLoading = false,
                groups = listOf("EASY", "HARD"),
                group = "EASY",
                sections = listOf(
                    ModeSection(
                        "EASY",
                        GameStats(played = 4, won = 3),
                        listOf(
                            ScoreRecord(
                                "EASY",
                                900,
                                65,
                                0,
                                mapOf(
                                    "mistakes" to "2",
                                ),
                            ),
                        ),
                    ),
                ),
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
        show(
            ScoresUiState(
                isLoading = false,
                groups = listOf("EASY"),
                group = "EASY",
                sections = listOf(ModeSection("EASY", GameStats(played = 1))),
            ),
        )
        compose.onNodeWithText("easy").assertExists()
        compose.onNodeWithText("Win a game to see your scores here.").assertExists()
    }

    @Test
    fun `a game without scores shows the stats alone, with draws and losses`() {
        show(
            ScoresUiState(
                isLoading = false,
                groups = listOf("3×3"),
                group = "3×3",
                sections = listOf(
                    ModeSection("EASY", GameStats(5, 2, 0, 1, 1)),
                    ModeSection("HARD", GameStats(played = 3)),
                ),
            ),
            ranked = false,
        )
        // A section per mode, each named: "easy" and "hard".
        compose.onNodeWithText("hard").assertExists()
        compose.onNodeWithContentDescription("Draws, 1").assertExists()
        compose.onNodeWithContentDescription("Losses, 2").assertExists()
        compose.onNodeWithText("Win a game to see your scores here.").assertDoesNotExist()
    }

    @Test
    fun `up to four groups are segmented buttons, more are scrollable tabs, and both select`() {
        var chosen: String? = null
        val five = listOf("A", "B", "C", "D", "E")
        show(ScoresUiState(isLoading = false, groups = five, group = "A", sections = listOf(ModeSection("A")))) {
            chosen = it
        }
        // Five scroll as tabs; the first ones are on screen.
        compose.onNodeWithText("b").performClick()
        chosen shouldBe "B"
    }

    @Test
    fun `a swipe to the left picks the next group, to the right the previous one`() {
        val picked = mutableListOf<String>()
        val state = ScoresUiState(
            isLoading = false,
            groups = listOf("A", "B", "C"),
            group = "B",
            sections = listOf(ModeSection("B", GameStats(played = 1))),
        )
        show(state) { picked += it }
        // Across the whole screen: its middle is the cards, below the selector.
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.onRoot().performTouchInput { swipeRight() }
        picked shouldBe listOf("C", "A")
    }

    private fun one(section: ModeSection) =
        show(ScoresUiState(isLoading = false, groups = listOf("ALL"), group = "ALL", sections = listOf(section)))

    @Test
    fun `the game writes the points`() {
        one(ModeSection("VEGAS", GameStats(played = 1, won = 1), listOf(ScoreRecord("VEGAS", 13, 300, 0))))
        compose.onNodeWithText("$13").assertExists()
        compose.onNodeWithContentDescription("13 dollars").assertExists()
    }

    @Test
    fun `a fastest ranking leads with the time and shows no points`() {
        val record = ScoreRecord("TIMED", 777, 125, 0)
        one(ModeSection("TIMED", GameStats(played = 1, won = 1), listOf(record), Ranking.FASTEST))
        compose.onNodeWithText("2:05").assertExists()
        compose.onNodeWithText("777").assertDoesNotExist()
    }

    @Test
    fun `an unranked mode shows its stats and doesn't ask for a win`() {
        one(ModeSection("BANK", GameStats(played = 2), ranking = null))
        compose.onNodeWithContentDescription("Played, 2").assertExists()
        compose.onNodeWithText("Win a game to see your scores here.").assertDoesNotExist()
    }

    @Test
    fun `before any game, the screen says what fills it`() {
        show(ScoresUiState(isLoading = false))
        compose.onNodeWithText("Win a game to see your scores here.").assertExists()
    }

    @Test
    fun `a game without scores asks for a game instead`() {
        show(ScoresUiState(isLoading = false), ranked = false)
        compose.onNodeWithText("Play a game to see your stats here.").assertExists()
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE) // Real text widths.
    fun `names too long for segments make scrollable tabs instead`() {
        var chosen: String? = null
        val groups = listOf("Standard", "Vegas", "Vegas cumulative", "Counter time")
        show(
            ScoresUiState(
                isLoading = false,
                groups = groups,
                group = "Standard",
                sections = listOf(ModeSection("Standard")),
            ),
        ) {
            chosen = it
        }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(4)
        compose.onNodeWithText("vegas cumulative").performClick()
        chosen shouldBe "Vegas cumulative"
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `short names stay segmented buttons`() {
        show(
            ScoresUiState(
                isLoading = false,
                groups = listOf("A", "B"),
                group = "A",
                sections = listOf(ModeSection("A")),
            ),
        )
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(0)
    }
}
