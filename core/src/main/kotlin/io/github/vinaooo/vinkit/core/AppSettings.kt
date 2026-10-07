package io.github.vinaooo.vinkit.core

import kotlinx.coroutines.flow.Flow

/** The hand the controls sit under. */
enum class Handedness { LEFT, RIGHT }

/** Where the board sits in the height it has, in portrait. */
enum class BoardAlignment { TOP, BOTTOM }

/** Where phone view's board sits across the room it has on a tablet. */
enum class PhoneViewSide { LEFT, CENTER, RIGHT }

/** The settings every vinkit game has. A game keeps its own (mode, difficulty, …) apart from these. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    /** The game's brand color by default; see the game's own default. */
    val themeColor: ThemeColor = ThemeColor.BLUE,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val handedness: Handedness = Handedness.RIGHT,
    val boardAlignment: BoardAlignment = BoardAlignment.TOP,
    /** On a tablet, the board at the size a phone shows it, instead of filling the screen. */
    val phoneView: Boolean = false,
    val phoneViewSide: PhoneViewSide = PhoneViewSide.CENTER,
)

interface AppSettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun update(transform: (AppSettings) -> AppSettings)
}
