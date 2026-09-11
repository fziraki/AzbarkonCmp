package abkabk.azbarkon.domain.model.profile

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class BadgeCatalogTest {
    @Test
    fun `first ghazal badge earned only when a ghazal is fully reviewed`() {
        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 1,
                hasCompletedGhazal = true,
                reviewedVersesCount = 0,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            ),
        ).isTrue()

        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 1,
                hasCompletedGhazal = false,
                reviewedVersesCount = 200,
                gameVisitStreak = 10,
                completedPoemCount = 10,
                perfectGameSessions = 10,
            ),
        ).isFalse()
    }

    @Test
    fun `hundred verses badge earned at 200 reviewed verses`() {
        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 2,
                hasCompletedGhazal = false,
                reviewedVersesCount = 199,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            ),
        ).isFalse()

        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 2,
                hasCompletedGhazal = false,
                reviewedVersesCount = 200,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            ),
        ).isTrue()
    }

    @Test
    fun `seven night streak badge uses game visit streak`() {
        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 3,
                hasCompletedGhazal = false,
                reviewedVersesCount = 0,
                gameVisitStreak = 6,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            ),
        ).isFalse()

        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 3,
                hasCompletedGhazal = false,
                reviewedVersesCount = 0,
                gameVisitStreak = 7,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            ),
        ).isTrue()
    }

    @Test
    fun `poetry lover badge earned after five completed poems`() {
        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 4,
                hasCompletedGhazal = true,
                reviewedVersesCount = 500,
                gameVisitStreak = 30,
                completedPoemCount = 4,
                perfectGameSessions = 20,
            ),
        ).isFalse()

        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 4,
                hasCompletedGhazal = true,
                reviewedVersesCount = 500,
                gameVisitStreak = 30,
                completedPoemCount = 5,
                perfectGameSessions = 20,
            ),
        ).isTrue()
    }

    @Test
    fun `hafez star badge earned after five perfect game sessions`() {
        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 5,
                hasCompletedGhazal = false,
                reviewedVersesCount = 0,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 4,
            ),
        ).isFalse()

        assertThat(
            BadgeCatalog.resolveEarned(
                badgeId = 5,
                hasCompletedGhazal = false,
                reviewedVersesCount = 0,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 5,
            ),
        ).isTrue()
    }

    @Test
    fun `toBadgeUi maps catalog fields and earn state`() {
        val badge = BadgeCatalog.badges.first { it.id == 2 }

        val ui =
            BadgeCatalog.toBadgeUi(
                badge = badge,
                hasCompletedGhazal = false,
                reviewedVersesCount = 200,
                gameVisitStreak = 0,
                completedPoemCount = 0,
                perfectGameSessions = 0,
            )

        assertThat(ui.id).isEqualTo(2)
        assertThat(ui.name).isEqualTo("صد بیت")
        assertThat(ui.description).isEqualTo("۱۰۰ بیت حفظ کنید")
        assertThat(ui.isEarned).isTrue()
    }

    @Test
    fun `catalog contains five mock badges in order`() {
        assertThat(BadgeCatalog.badges.map { it.id }).isEqualTo(listOf(1, 2, 3, 4, 5))
    }
}
