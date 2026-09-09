package abkabk.azbarkon.domain.usecase

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.model.profile.BadgeCatalog
import abkabk.azbarkon.domain.model.profile.BadgeUi
import abkabk.azbarkon.domain.model.profile.GameLevelCatalog
import abkabk.azbarkon.domain.model.profile.GameProfileStats
import abkabk.azbarkon.domain.model.profile.LevelListItemUi
import abkabk.azbarkon.domain.model.profile.MemorizationProfileStats
import abkabk.azbarkon.domain.model.profile.ProfileLevelProgress
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.features.poets.GHAZAL_CATEGORY

class BuildProfileStatsUseCase(
    private val memorizationRepository: MemorizationRepository,
) {
    data class ProfileStatsResult(
        val levelProgress: ProfileLevelProgress,
        val memorizationStats: MemorizationProfileStats,
        val reviewedVersesCount: Int,
        val hasCompletedGhazal: Boolean,
        val previewBadges: List<BadgeUi>,
        val allBadges: List<BadgeUi>,
        val allLevels: List<LevelListItemUi>,
    )

    suspend operator fun invoke(
        gameStats: GameProfileStats,
        activePoemCount: Int = 0,
    ): ProfileStatsResult {

        val levelProgress = GameLevelCatalog.progressFromCoinBalance(gameStats.coinBalance)
        val reviewedVerses = memorizationRepository.countReviewedVerses()

        val completedPoems = when (val result = memorizationRepository.getPoemsByStatus(MemorizationStatus.COMPLETED)) {
            is Result.Success -> result.data
            is Result.Error -> emptyList()
        }

        val hasCompletedGhazal = completedPoems.any { it.categoryName == GHAZAL_CATEGORY }

        val badges = BadgeCatalog.badges.map { badge ->
            BadgeCatalog.toBadgeUi(
                badge = badge,
                hasCompletedGhazal = hasCompletedGhazal,
                reviewedVersesCount = reviewedVerses,
                gameVisitStreak = gameStats.visitStreak,
                completedPoemCount = completedPoems.size,
                perfectGameSessions = gameStats.perfectGameSessions,
            )
        }

        val levels = GameLevelCatalog.levels.map { level ->
            LevelListItemUi(
                level = GameLevelCatalog.toGameLevel(level),
                state = GameLevelCatalog.levelRowState(
                    levelId = level.id,
                    currentLevelId = levelProgress.levelId,
                ),
            )
        }

        return ProfileStatsResult(
            levelProgress = levelProgress,
            memorizationStats = MemorizationProfileStats(
                activePoemCount = activePoemCount,
                completedPoemCount = completedPoems.size,
            ),
            reviewedVersesCount = reviewedVerses,
            hasCompletedGhazal = hasCompletedGhazal,
            previewBadges = badges,
            allBadges = badges,
            allLevels = levels,
        )
    }
}
