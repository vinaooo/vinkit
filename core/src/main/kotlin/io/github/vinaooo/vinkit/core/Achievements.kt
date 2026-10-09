package io.github.vinaooo.vinkit.core

import kotlinx.coroutines.flow.Flow

/**
 * The badges earned, by the game's own keys ("FIRST_WIN"), and what the ones that add up over games have gathered so
 * far ([collected]: "sizes_won" to the sizes won). Keys are stored as they are: never rename one after release.
 */
data class AchievementProgress(
    val unlocked: Set<String> = emptySet(),
    val collected: Map<String, Set<String>> = emptyMap(),
)

interface AchievementRepository {
    val progress: Flow<AchievementProgress>

    suspend fun update(transform: (AchievementProgress) -> AchievementProgress)
}

/** Applies the game's rules ([transform]) and returns the badges they just unlocked. */
suspend fun AchievementRepository.unlock(transform: (AchievementProgress) -> AchievementProgress): Set<String> {
    var earned = emptySet<String>()
    update { before -> transform(before).also { earned = it.unlocked - before.unlocked } }
    return earned
}
