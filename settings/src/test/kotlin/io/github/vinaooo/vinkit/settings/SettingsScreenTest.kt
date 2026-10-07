package io.github.vinaooo.vinkit.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.core.BoardAlignment
import io.github.vinaooo.vinkit.core.Handedness
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.core.ThemeMode
import io.github.vinaooo.vinkit.designsystem.VinkitTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-port")
class SettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private var current = AppSettings()
    private var gameValue = "A"
    private var rival = 1

    private fun show(
        settings: AppSettings = AppSettings(),
        privacyOptions: Boolean = false,
        onPolicy: () -> Unit = {},
    ) {
        current = settings
        compose.setContent {
            VinkitTheme(ThemeColor.BLUE) {
                SettingsScreen(
                    settings = settings,
                    onChange = { current = it(current) },
                    onBack = {},
                    onOpenPrivacyPolicy = onPolicy,
                    privacyOptionsRequired = privacyOptions,
                    gameSections = listOf(
                        SettingsSection("Game") {
                            Choice("Level", listOf("A" to "Easy", "B" to "Hard"), gameValue, { gameValue = it })
                            IconChoice(
                                "Rival",
                                listOf(
                                    IconOption(1, Icons.Rounded.Star, "Cat", "Plays at random"),
                                    IconOption(2, Icons.Rounded.Favorite, "Owl", "Never loses"),
                                ),
                                rival,
                                { rival = it },
                            )
                        },
                    ),
                )
            }
        }
    }

    @Test
    fun `the game's own section comes with the screen and sends its own changes`() {
        show()
        compose.onNodeWithText("Game").assertExists()
        compose.onNodeWithText("Hard").performClick()
        gameValue shouldBe "B"
    }

    @Test
    fun `an icon choice names its options for TalkBack and explains the chosen one`() {
        show()
        compose.onNodeWithText("Plays at random").assertExists()
        compose.onNodeWithContentDescription("Owl").performClick()
        rival shouldBe 2
    }

    @Test
    fun `appearance and feedback rows change the settings`() {
        show(AppSettings(dynamicColor = false))
        compose.onNodeWithText("Dark").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Orange").performScrollTo().performClick()
        compose.onNodeWithText("Left").performScrollTo().performClick()
        compose.onNodeWithText("Bottom").performScrollTo().performClick()
        compose.onNodeWithText("Vibration").performScrollTo().performClick()
        current shouldBe AppSettings(
            themeMode = ThemeMode.DARK,
            dynamicColor = false,
            themeColor = ThemeColor.ORANGE,
            handedness = Handedness.LEFT,
            boardAlignment = BoardAlignment.BOTTOM,
            hapticsEnabled = false,
        )
    }

    @Test
    fun `the color row hides while dynamic color is on`() {
        show(AppSettings(dynamicColor = true))
        compose.onNodeWithContentDescription("Orange").assertDoesNotExist()
    }

    @Test
    fun `the privacy policy is always there, privacy options only when required`() {
        var opened = 0
        show(onPolicy = { opened++ })
        compose.onNodeWithText("Privacy options").assertDoesNotExist()
        compose.onNodeWithText("Privacy policy").performScrollTo().performClick()
        opened shouldBe 1
    }

    @Test
    @Config(qualifiers = "sw600dp-w800dp-h1280dp-port")
    fun `a tablet offers phone view and, once on, its side`() {
        show(AppSettings(phoneView = true))
        compose.onNodeWithText("Phone view").assertExists()
        compose.onNodeWithText("Board side").assertExists()
    }
}
