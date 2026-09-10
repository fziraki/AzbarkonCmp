package abkabk.azbarkon.domain.model.profile

import abkabk.azbarkon.domain.model.GameLevel

data class MemorizationProfileStats(
    val activePoemCount: Int = 0,
    val completedPoemCount: Int = 0,
)

data class GameProfileStats(
    val visitStreak: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalWrongAnswers: Int = 0,
    val coinBalance: Int = 0,
    val completedSessions: Int = 0,
    val perfectGameSessions: Int = 0,
)

enum class ProfileSheet {
    Settings,
    Badges,
    Levels,
}

enum class LevelRowState {
    Completed,
    Current,
    Locked,
}

data class LevelListItemUi(
    val level: GameLevel,
    val state: LevelRowState,
)

data class BadgeUi(
    val id: Int,
    val name: String,
    val description: String,
    val isEarned: Boolean,
)

data class ProfileLevelProgress(
    val levelId: Int,
    val levelName: String,
    val currentXp: Int,
    val targetXp: Int,
)
