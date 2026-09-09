package abkabk.azbarkon.data.backup

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.StoredPoem
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class UserBackupManagerTest {

    @Test
    fun `encode then decode round-trips all prefs and memorization data`() {
        val file =
            UserBackupFile(
                exportedAtMillis = 123456789,
                prefs =
                    UserBackupPrefs(
                        themeMode = 2,
                        coinInitialized = true,
                        gameCoinBalance = 150,
                        gameVisitStreak = 4,
                        gameLastPlayDay = 20260101,
                        gameTotalCorrect = 77,
                        gameTotalWrong = 13,
                        gameCompletedSessions = 9,
                        gamePerfectSessions = 3,
                        likedPoemIds = setOf(1, 2),
                        bookmarkedPoemIds = setOf(3),
                        dailyDistichNotificationsEnabled = true,
                        memorizationReminderEnabled = false,
                    ),
                memorization =
                    UserBackupMemorization(
                        activePoems =
                            listOf(
                                StoredPoem(poemId = 5, addedAtMillis = 1000, status = "ACTIVE"),
                            ),
                    ),
            )

        val decoded = decodeBackup(encodeBackup(file))

        assertThat(decoded).isInstanceOf(Result.Success::class)
        assertThat((decoded as Result.Success).data.prefs).isEqualTo(file.prefs)
        assertThat(decoded.data.memorization.activePoems).isEqualTo(file.memorization.activePoems)
    }

    @Test
    fun `unknown keys in backup are ignored`() {
        val json =
            """
            {"format":1,"unknown_key":"x","prefs":{"gameCoinBalance":42}}
            """.trimIndent()

        val decoded = decodeBackup(json)

        assertThat(decoded).isInstanceOf(Result.Success::class)
        assertThat((decoded as Result.Success).data.prefs.gameCoinBalance).isEqualTo(42)
    }

    @Test
    fun `invalid json returns InvalidData`() {
        assertThat(decodeBackup("not json")).isInstanceOf(Result.Error::class)
    }

    @Test
    fun `unsupported format returns UnsupportedFormat`() {
        val decoded = decodeBackup("""{"format":99}""")

        assertThat(decoded).isInstanceOf(Result.Error::class)
        assertThat((decoded as Result.Error).error).isEqualTo(BackupError.UnsupportedFormat)
    }
}
