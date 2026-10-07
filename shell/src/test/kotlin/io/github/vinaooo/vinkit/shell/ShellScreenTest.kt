package io.github.vinaooo.vinkit.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.designsystem.VinkitTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ShellScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<String>()

    @Composable
    private fun toolbar(frame: FrameInfo, notes: Boolean = false) = GameToolbar(
        actions = listOf(
            ToolbarAction.Button(Icons.AutoMirrored.Rounded.Undo, "Undo", keepDirection = true) { events += "undo" },
            ToolbarAction.Button(Icons.AutoMirrored.Rounded.Undo, "Redo", enabled = false) { events += "redo" },
            ToolbarAction.Toggle(Icons.Outlined.Edit, Icons.Rounded.Edit, "Notes", notes) { events += "notes $it" },
        ),
        menuOptions = listOf(
            MenuOption(Icons.Rounded.Add, "New game") { events += "new" },
            MenuOption(Icons.Rounded.Refresh, "Restart this board") { events += "restart" },
        ),
        onReportBug = { events += "report" },
        vertical = frame.landscape,
        mirrored = frame.mirrored,
    )

    private fun show(settings: AppSettings = AppSettings()) = compose.setContent {
        VinkitTheme(ThemeColor.BLUE) {
            GameSurface(announcement = null, announcementSequence = 0) {
                GameFrame(
                    settings = settings,
                    info = { ModeAndTime("Easy", 65, large = it.landscape) },
                    board = { Box(Modifier.fillMaxSize().testTag("board")) { Text("board") } },
                    toolbar = { toolbar(it) },
                    onOpenScores = { events += "scores" },
                    onOpenSettings = { events += "settings" },
                )
            }
        }
    }

    @Test
    fun `the frame shows the info, the board, the navigation and the game's toolbar`() {
        show()
        compose.onNodeWithContentDescription("Easy, Time, 1 minute 5 seconds").assertExists()
        compose.onNodeWithTag("board").assertExists()
        compose.onNodeWithContentDescription("Scores").performClick()
        compose.onNodeWithContentDescription("Undo").performClick()
        compose.onNodeWithContentDescription("Redo").performClick()
        compose.onNode(isToggleable()).performClick()
        events shouldBe listOf("scores", "undo", "notes true")
    }

    @Test
    @Config(qualifiers = "w891dp-h411dp-land")
    fun `landscape keeps everything reachable`() {
        show()
        compose.onNodeWithTag("board").assertExists()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithContentDescription("Undo").performClick()
        events shouldBe listOf("settings", "undo")
    }

    @Test
    fun `the menu offers the game's options`() {
        show()
        compose.onNodeWithContentDescription("New game").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Restart this board").performClick()
        events shouldBe listOf("restart")
    }

    @Test
    fun `the end dialog lists the game's results`() {
        compose.setContent {
            VinkitTheme(ThemeColor.BLUE) {
                WinDialog(lines = listOf("Score: 900", "Time: 1:05"), onNewGame = { events += "new" }, kind = null)
            }
        }
        compose.onNodeWithText("You won!").assertExists()
        compose.onNodeWithText("Score: 900").assertExists()
        compose.onNodeWithText("New game").performClick()
        events shouldBe listOf("new")
    }
}
