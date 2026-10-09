package io.github.vinaooo.vinkit.achievements

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.vinaooo.vinkit.core.AchievementProgress
import io.github.vinaooo.vinkit.core.unlock
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreAchievementRepositoryTest {
    @TempDir
    lateinit var dir: File

    private val scope = TestScope(StandardTestDispatcher())

    private val store by lazy {
        PreferenceDataStoreFactory.create(scope = scope.backgroundScope) { File(dir, "settings.preferences_pb") }
    }

    private fun repository() = DataStoreAchievementRepository(store)

    @Test
    fun `nothing is earned on first launch`() = scope.runTest {
        repository().progress.first() shouldBe AchievementProgress()
    }

    @Test
    fun `badges and collected sets are persisted`() = scope.runTest {
        val progress = AchievementProgress(setOf("FIRST_WIN"), mapOf("sizes_won" to setOf("TEN", "EIGHT")))
        repository().update { progress }

        repository().progress.first() shouldBe progress
    }

    @Test
    fun `unlock returns only the badges just earned`() = scope.runTest {
        val repository = repository()
        repository.update { AchievementProgress(setOf("FIRST_WIN")) }

        repository.unlock { it.copy(unlocked = it.unlocked + "FIRST_WIN" + "BEAT_HARD") } shouldBe setOf("BEAT_HARD")
    }

    @Test
    fun `the game's own keys are left alone`() = scope.runTest {
        val own = stringPreferencesKey("achievements_note")
        store.edit { it[own] = "kept" }
        repository().update { AchievementProgress(setOf("FIRST_WIN")) }

        repository().progress.first() shouldBe AchievementProgress(setOf("FIRST_WIN"))
        store.data.first()[own] shouldBe "kept"
    }
}
