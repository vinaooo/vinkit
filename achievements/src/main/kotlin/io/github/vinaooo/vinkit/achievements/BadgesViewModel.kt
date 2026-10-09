package io.github.vinaooo.vinkit.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinaooo.vinkit.core.AchievementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The badges earned so far. Open, so a Hilt app can subclass it with an `@Inject` constructor. */
open class BadgesViewModel(achievements: AchievementRepository) : ViewModel() {
    val unlocked: StateFlow<Set<String>> = achievements.progress.map { it.unlocked }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptySet())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
