package io.github.vinaooo.vinkit.scores

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.vinaooo.vinkit.core.GameStats
import io.github.vinaooo.vinkit.core.Ranking
import io.github.vinaooo.vinkit.scores.data.RoomScoreRepository
import io.github.vinaooo.vinkit.scores.data.RoomStatsRepository
import io.github.vinaooo.vinkit.scores.data.ScoresDatabase
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * A player's scores and stats survive every schema change: a database built from each exported schema, with data
 * in it, is opened by the current kit, which runs the real migrations. Add a case for each new schema version.
 */
@RunWith(RobolectricTestRunner::class)
class ScoresDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `a version 1 database opens with its scores and stats, and no draws`() = runTest {
        create(version = 1, context.getDatabasePath(NAME).also { it.parentFile?.mkdirs() })

        val db = Room.databaseBuilder(context, ScoresDatabase::class.java, NAME).allowMainThreadQueries().build()
        try {
            RoomScoreRepository(db).observeTopScores("EASY", Ranking.HIGHEST_POINTS).first().single().points shouldBe
                900
            RoomStatsRepository(db).observe("EASY").first() shouldBe GameStats(5, 3, 1, 2, drawn = 0)
        } finally {
            db.close()
        }
    }

    /** The tables and identity of an exported schema, with a score and a stats row in them. */
    private fun create(version: Int, file: File) {
        val schema = Json.parseToJsonElement(File("$SCHEMAS/$version.json").readText()).jsonObject
            .getValue("database").jsonObject
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            schema.getValue("entities").jsonArray.forEach { entity ->
                val table = entity.jsonObject.getValue("tableName").jsonPrimitive.content
                db.execSQL(
                    entity.jsonObject.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table),
                )
                entity.jsonObject["indices"]?.jsonArray?.forEach { index ->
                    db.execSQL(
                        index.jsonObject.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table),
                    )
                }
            }
            schema.getValue("setupQueries").jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
            db.execSQL(
                "INSERT INTO scores (mode, points, elapsedSeconds, playedAtMillis, extras) " +
                    "VALUES ('EASY', 900, 60, 7, '{}')",
            )
            db.execSQL("INSERT INTO stats (mode, played, won, currentStreak, bestStreak) VALUES ('EASY', 5, 3, 1, 2)")
            db.version = version
        }
    }

    private companion object {
        const val NAME = "migration-test.db"
        const val SCHEMAS = "schemas/io.github.vinaooo.vinkit.scores.data.ScoresDatabase"
    }
}
