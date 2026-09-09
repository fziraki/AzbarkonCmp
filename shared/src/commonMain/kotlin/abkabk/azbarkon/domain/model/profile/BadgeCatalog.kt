package abkabk.azbarkon.domain.model.profile

data class CatalogBadge(
    val id: Int,
    val name: String,
    val description: String,
)

object BadgeCatalog {
    private const val FIRST_GHAZAL_BADGE_ID = 1
    private const val HUNDRED_VERSES_BADGE_ID = 2
    private const val WEEKLY_STREAK_BADGE_ID = 3
    private const val POETRY_LOVER_BADGE_ID = 4
    private const val GAMES_STAR_BADGE_ID = 5


    private const val TWO_HUNDRED_VERSES_THRESHOLD = 200
    private const val WEEKLY_STREAK_THRESHOLD = 7
    private const val POETRY_LOVER_THRESHOLD = 5
    private const val GAMES_STAR_THRESHOLD = 5

    val badges: List<CatalogBadge> =
        listOf(
            CatalogBadge(
                id = 1,
                name = "اولین غزل",
                description = "یک غزل را کامل حفظ کنید",
            ),
            CatalogBadge(
                id = 2,
                name = "صد بیت",
                description = "۱۰۰ بیت حفظ کنید",
            ),
            CatalogBadge(
                id = 3,
                name = "هفت شب پیاپی",
                description = "۷ روز پیاپی بازی کنید",
            ),
            CatalogBadge(
                id = 4,
                name = "علاقه‌مند شعر",
                description = "۵ شعر کامل حفظ کنید",
            ),
            CatalogBadge(
                id = 5,
                name = "ستاره‌ی میدان",
                description = "۵ بازی را با امتیاز کامل ببرید",
            ),
        )

    fun resolveEarned(
        badgeId: Int,
        hasCompletedGhazal: Boolean,
        reviewedVersesCount: Int,
        gameVisitStreak: Int,
        completedPoemCount: Int,
        perfectGameSessions: Int,
    ): Boolean =
        when (badgeId) {
            FIRST_GHAZAL_BADGE_ID -> hasCompletedGhazal
            HUNDRED_VERSES_BADGE_ID -> reviewedVersesCount >= TWO_HUNDRED_VERSES_THRESHOLD
            WEEKLY_STREAK_BADGE_ID -> gameVisitStreak >= WEEKLY_STREAK_THRESHOLD
            POETRY_LOVER_BADGE_ID -> completedPoemCount >= POETRY_LOVER_THRESHOLD
            GAMES_STAR_BADGE_ID -> perfectGameSessions >= GAMES_STAR_THRESHOLD
            else -> false
        }

    fun toBadgeUi(
        badge: CatalogBadge,
        hasCompletedGhazal: Boolean,
        reviewedVersesCount: Int,
        gameVisitStreak: Int,
        completedPoemCount: Int,
        perfectGameSessions: Int,
    ): BadgeUi =
        BadgeUi(
            id = badge.id,
            name = badge.name,
            description = badge.description,
            isEarned =
                resolveEarned(
                    badgeId = badge.id,
                    hasCompletedGhazal = hasCompletedGhazal,
                    reviewedVersesCount = reviewedVersesCount,
                    gameVisitStreak = gameVisitStreak,
                    completedPoemCount = completedPoemCount,
                    perfectGameSessions = perfectGameSessions,
                ),
        )
}
