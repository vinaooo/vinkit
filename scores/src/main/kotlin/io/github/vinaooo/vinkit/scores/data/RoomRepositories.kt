package io.github.vinaooo.vinkit.scores.data

import androidx.room.withTransaction
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.core.ScoreRecord
import io.github.vinaooo.vinkit.core.ScoreRepository
import io.github.vinaooo.vinkit.core.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RoomScoreRepository(private val db: ScoresDatabase) : ScoreRepository {
    override fun observeTopScores(mode: String, ranking: Ranking, limit: Int): Flow<List<ScoreRecord>> {
        val rows = when (ranking) {
            Ranking.HIGHEST_POINTS -> db.scoreDao().observeHighestPoints(mode, limit)
            Ranking.FASTEST -> db.scoreDao().observeFastest(mode, limit)
        }
        return rows.map { list -> list.map { it.toRecord() } }
    }

    override suspend fun add(record: ScoreRecord) = db.scoreDao().insert(record.toEntity())
}

class RoomStatsRepository(private val db: ScoresDatabase) : StatsRepository {
    override fun observe(mode: String): Flow<GameStats> = db.statsDao().observe(mode).map { it.toStats() }

    override fun observePlayedModes(): Flow<Set<String>> = db.statsDao().observePlayedModes().map { it.toSet() }

    override suspend fun update(mode: String, transform: (GameStats) -> GameStats) = db.withTransaction {
        val current = db.statsDao().get(mode).toStats()
        db.statsDao().upsert(transform(current).toEntity(mode))
    }
}

private val json = Json { ignoreUnknownKeys = true }

private fun ScoreRecord.toEntity() = ScoreEntity(
    mode = mode,
    points = points,
    elapsedSeconds = elapsedSeconds,
    playedAtMillis = playedAtMillis,
    extras = json.encodeToString(extras),
)

private fun ScoreEntity.toRecord() = ScoreRecord(
    mode = mode,
    points = points,
    elapsedSeconds = elapsedSeconds,
    playedAtMillis = playedAtMillis,
    // A row a newer version wrote in another shape still shows, without its extras.
    extras = runCatching { json.decodeFromString<Map<String, String>>(extras) }.getOrDefault(emptyMap()),
)

private fun GameStats.toEntity(mode: String) = StatsEntity(mode, played, won, currentStreak, bestStreak, drawn)

private fun StatsEntity?.toStats() =
    this?.let { GameStats(played, won, currentStreak, bestStreak, drawn) } ?: GameStats()
