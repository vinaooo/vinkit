package io.github.vinaooo.vinkit.core

import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow

/**
 * A won game in its mode's ranking. [mode] is the game's own key for a mode ("KILLER_HARD"); [extras] holds what
 * else the game shows on a score row ("mistakes" to "2"), stored as is.
 */
data class ScoreRecord(
    val mode: String,
    val points: Int,
    val elapsedSeconds: Long,
    val playedAtMillis: Long,
    val extras: Map<String, String> = emptyMap(),
) {
    companion object {
        const val TOP_LIMIT = 10
    }
}

/** How a mode's scores rank. Ties always go to the earlier game. */
enum class Ranking(val comparator: Comparator<ScoreRecord>) {
    /** The most points first, then the fastest. */
    HIGHEST_POINTS(
        compareByDescending<ScoreRecord> {
            it.points
        }.thenBy { it.elapsedSeconds }.thenBy { it.playedAtMillis },
    ),

    /** The fastest first, then the most points. */
    FASTEST(compareBy<ScoreRecord> { it.elapsedSeconds }.thenByDescending { it.points }.thenBy { it.playedAtMillis }),
}

/** Statistics of one mode. */
data class GameStats(val played: Int = 0, val won: Int = 0, val currentStreak: Int = 0, val bestStreak: Int = 0) {
    val winRatePercent: Int
        get() = if (played == 0) 0 else (won * PERCENT / played.toDouble()).roundToInt()

    fun afterWin(): GameStats {
        val streak = currentStreak + 1
        return copy(played = played + 1, won = won + 1, currentStreak = streak, bestStreak = maxOf(bestStreak, streak))
    }

    fun afterLoss(): GameStats = copy(played = played + 1, currentStreak = 0)

    private companion object {
        const val PERCENT = 100
    }
}

interface ScoreRepository {
    /** The best [limit] scores of [mode], ranked by [ranking]. */
    fun observeTopScores(mode: String, ranking: Ranking, limit: Int = ScoreRecord.TOP_LIMIT): Flow<List<ScoreRecord>>

    suspend fun add(record: ScoreRecord)
}

/** Statistics are kept per mode. */
interface StatsRepository {
    fun observe(mode: String): Flow<GameStats>

    /** The modes played at least once, won or not: the Scores screen's tabs. */
    fun observePlayedModes(): Flow<Set<String>>

    suspend fun update(mode: String, transform: (GameStats) -> GameStats)
}
