package io.github.vinaooo.vinkit.scores.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** A won game. [extras] is the game's own values as a JSON object of strings. */
@Entity(tableName = "scores", indices = [Index("mode")])
internal data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val points: Int,
    val elapsedSeconds: Long,
    val playedAtMillis: Long,
    val extras: String,
)

/** One row per mode played. */
@Entity(tableName = "stats")
internal data class StatsEntity(
    @PrimaryKey val mode: String,
    val played: Int,
    val won: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    @ColumnInfo(defaultValue = "0") val drawn: Int,
)

@Dao
internal interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    /** `Ranking.HIGHEST_POINTS` in SQL. */
    @Query(
        "SELECT * FROM scores WHERE mode = :mode " +
            "ORDER BY points DESC, elapsedSeconds ASC, playedAtMillis ASC LIMIT :limit",
    )
    fun observeHighestPoints(mode: String, limit: Int): Flow<List<ScoreEntity>>

    /** `Ranking.LOWEST_POINTS` in SQL. */
    @Query(
        "SELECT * FROM scores WHERE mode = :mode " +
            "ORDER BY points ASC, elapsedSeconds ASC, playedAtMillis ASC LIMIT :limit",
    )
    fun observeLowestPoints(mode: String, limit: Int): Flow<List<ScoreEntity>>

    /** `Ranking.FASTEST` in SQL. */
    @Query(
        "SELECT * FROM scores WHERE mode = :mode " +
            "ORDER BY elapsedSeconds ASC, points DESC, playedAtMillis ASC LIMIT :limit",
    )
    fun observeFastest(mode: String, limit: Int): Flow<List<ScoreEntity>>
}

@Dao
internal interface StatsDao {
    @Query("SELECT * FROM stats WHERE mode = :mode")
    fun observe(mode: String): Flow<StatsEntity?>

    @Query("SELECT * FROM stats WHERE mode = :mode")
    suspend fun get(mode: String): StatsEntity?

    @Query("SELECT mode FROM stats WHERE played > 0")
    fun observePlayedModes(): Flow<List<String>>

    @Upsert
    suspend fun upsert(stats: StatsEntity)
}

/**
 * Every game's scores and stats, in the app's `vinkit_scores.db`. Every schema change gets an `AutoMigration` (or a
 * manual one) and a test migrating from each exported schema in `scores/schemas`; never a destructive fallback,
 * which would wipe the players' scores.
 */
@Database(
    entities = [ScoreEntity::class, StatsEntity::class],
    version = 2,
    exportSchema = true,
    // 2: stats.drawn.
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class ScoresDatabase : RoomDatabase() {
    internal abstract fun scoreDao(): ScoreDao

    internal abstract fun statsDao(): StatsDao

    companion object {
        const val NAME = "vinkit_scores.db"

        fun create(context: Context): ScoresDatabase =
            Room.databaseBuilder(context.applicationContext, ScoresDatabase::class.java, NAME).build()
    }
}
