package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.data.local.SqlDelightGamesLocalDataSource
import abkabk.azbarkon.domain.model.games.GameType
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import assertk.assertThat
import assertk.assertions.isGreaterThan
import com.sarv.db.SarvDatabase
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class OfflineFirstGamesRepositoryIntegrationTest {
    @Test
    fun `generateQuizBatch succeeds against bundled ganjoor database`() =
        runBlocking {
            val dbFile = resolveBundledDatabaseFile() ?: run {
                assumeTrue(false, "Bundled ganjoor.s3db not found — skipping integration test")
                return@runBlocking
            }
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            val database = SarvDatabase(driver)
            val repository =
                OfflineFirstGamesRepository(
                    localDataSource =
                        SqlDelightGamesLocalDataSource(
                            verseQueries = database.verseQueries,
                            poemQueries = database.poemQueries,
                            poetQueries = database.poetQueries,
                        ),
                )

            GameType.entries.forEach { gameType ->
                when (val result = repository.generateQuizBatch(gameType, seed = 42L, count = 10)) {
                    is Result.Success -> assertThat(result.data.size).isGreaterThan(0)
                    is Result.Error -> throw AssertionError("generateQuizBatch failed for $gameType")
                }
            }
        }

    private fun resolveBundledDatabaseFile(): File? {
        val candidates =
            listOf(
                File("sqlite/ganjoor.s3db"),
                File("../shared/sqlite/ganjoor.s3db"),
                File("shared/sqlite/ganjoor.s3db"),
            )
        return candidates.firstOrNull { it.exists() }
    }
}
