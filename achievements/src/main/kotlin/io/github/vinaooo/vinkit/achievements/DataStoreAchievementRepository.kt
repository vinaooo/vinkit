package io.github.vinaooo.vinkit.achievements

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.vinaooo.vinkit.core.AchievementProgress
import io.github.vinaooo.vinkit.core.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [AchievementProgress] in a Preferences DataStore the game may share: `achievements_unlocked`, and each collected
 * set under `achievements_<name>` (so a game can't name one "unlocked"). Keys a newer version wrote are kept as they
 * are; the game skips the ones it doesn't know.
 */
class DataStoreAchievementRepository(private val dataStore: DataStore<Preferences>) : AchievementRepository {
    override val progress: Flow<AchievementProgress> = dataStore.data.map { it.toProgress() }

    override suspend fun update(transform: (AchievementProgress) -> AchievementProgress) {
        dataStore.edit { prefs ->
            val progress = transform(prefs.toProgress())
            prefs[UNLOCKED] = progress.unlocked
            progress.collected.forEach { (name, keys) -> prefs[stringSetPreferencesKey(PREFIX + name)] = keys }
        }
    }

    private fun Preferences.toProgress(): AchievementProgress {
        val collected = asMap().mapNotNull { (key, value) ->
            val name = key.name.removePrefix(PREFIX)
            if (name == key.name || key == UNLOCKED || value !is Set<*>) return@mapNotNull null
            name to value.filterIsInstance<String>().toSet()
        }
        return AchievementProgress(this[UNLOCKED].orEmpty(), collected.toMap())
    }

    private companion object {
        const val PREFIX = "achievements_"
        val UNLOCKED = stringSetPreferencesKey("achievements_unlocked")
    }
}
