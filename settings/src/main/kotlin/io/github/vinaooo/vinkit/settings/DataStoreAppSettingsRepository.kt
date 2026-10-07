package io.github.vinaooo.vinkit.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.vinaooo.vinkit.core.AppSettings
import io.github.vinaooo.vinkit.core.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [AppSettings] in a Preferences DataStore, which the game may share for its own keys (vinkit only touches its own).
 * A value this version doesn't know (from a newer one) reads as the default. [defaults] carries the game's brand
 * color.
 */
class DataStoreAppSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val defaults: AppSettings = AppSettings(),
) : AppSettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { it.toSettings() }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private fun Preferences.toSettings() = AppSettings(
        themeMode = enumOrDefault(this[Keys.THEME_MODE], defaults.themeMode),
        dynamicColor = this[Keys.DYNAMIC_COLOR] ?: defaults.dynamicColor,
        themeColor = enumOrDefault(this[Keys.THEME_COLOR], defaults.themeColor),
        soundEnabled = this[Keys.SOUND] ?: defaults.soundEnabled,
        hapticsEnabled = this[Keys.HAPTICS] ?: defaults.hapticsEnabled,
        handedness = enumOrDefault(this[Keys.HANDEDNESS], defaults.handedness),
        boardAlignment = enumOrDefault(this[Keys.BOARD_ALIGNMENT], defaults.boardAlignment),
        phoneView = this[Keys.PHONE_VIEW] ?: defaults.phoneView,
        phoneViewSide = enumOrDefault(this[Keys.PHONE_VIEW_SIDE], defaults.phoneViewSide),
    )

    private fun MutablePreferences.write(settings: AppSettings) {
        this[Keys.THEME_MODE] = settings.themeMode.name
        this[Keys.DYNAMIC_COLOR] = settings.dynamicColor
        this[Keys.THEME_COLOR] = settings.themeColor.name
        this[Keys.SOUND] = settings.soundEnabled
        this[Keys.HAPTICS] = settings.hapticsEnabled
        this[Keys.HANDEDNESS] = settings.handedness.name
        this[Keys.BOARD_ALIGNMENT] = settings.boardAlignment.name
        this[Keys.PHONE_VIEW] = settings.phoneView
        this[Keys.PHONE_VIEW_SIDE] = settings.phoneViewSide.name
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    // The same keys Solo and Sudoku Trio used, so their saved settings carry over.
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val HANDEDNESS = stringPreferencesKey("handedness")
        val BOARD_ALIGNMENT = stringPreferencesKey("board_alignment")
        val PHONE_VIEW = booleanPreferencesKey("phone_view")
        val PHONE_VIEW_SIDE = stringPreferencesKey("phone_view_side")
    }
}
