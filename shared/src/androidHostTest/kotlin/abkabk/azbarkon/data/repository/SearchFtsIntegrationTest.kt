package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.data.local.SqlDelightSearchLocalDataSource
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import assertk.assertThat
import assertk.assertions.isGreaterThan
import com.sarv.db.SarvDatabase
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SearchFtsIntegrationTest {
    @Test
    fun `search عشق filtered to Hafez returns hits`() =
        runBlocking {
            val dbFile = resolveBundledDatabaseFile()
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            val database = SarvDatabase(driver)
            val dataSource = SqlDelightSearchLocalDataSource(database.searchQueries, database.catQueries)

            val result = dataSource.searchVersesPage(query = "عشق", poetId = 2, categoryIds = null, offset = 0, limit = 20)

            when (result) {
                is Result.Success -> {
                    assertThat(result.data.size).isGreaterThan(0)
                    println("hits: ${result.data.size}, first: ${result.data.first().verseText}")
                }
                is Result.Error -> throw AssertionError("search failed: ${result.error}")
            }
        }

    @Test
    fun `search global عشق returns hits`() =
        runBlocking {
            val dbFile = resolveBundledDatabaseFile()
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            val database = SarvDatabase(driver)
            val dataSource = SqlDelightSearchLocalDataSource(database.searchQueries, database.catQueries)

            val result = dataSource.searchVersesPage(query = "عشق", poetId = null, categoryIds = null, offset = 0, limit = 20)

            when (result) {
                is Result.Success -> assertThat(result.data.size).isGreaterThan(0)
                is Result.Error -> throw AssertionError("search failed: ${result.error}")
            }
        }

    private fun resolveBundledDatabaseFile(): File {
        val candidates =
            listOf(
                File("sqlite/ganjoor.s3db"),
                File("../shared/sqlite/ganjoor.s3db"),
                File("shared/sqlite/ganjoor.s3db"),
            )
        return candidates.firstOrNull { it.exists() }
            ?: error("Bundled ganjoor.s3db not found")
    }
}