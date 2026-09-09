package abkabk.azbarkon.domain.model.memorization

import kotlinx.serialization.Serializable

enum class SrsGrade {
    AGAIN,
    HARD,
    GOOD,
    EASY,
    UNSPECIFIED,
}

enum class MemorizationStatus {
    ACTIVE,
    COMPLETED,
}

@Serializable
data class SrsCard(
    val id: Long,
    val poemId: Int,
    val front: String,
    val back: String,
)

@Serializable
data class StoredPoem(
    val poemId: Int,
    val addedAtMillis: Long,
    val status: String,
    val interval: Int = 0,
    val dueDate: Long = 0,
    val consecutiveCorrect: Int = 0,
    val totalCards: Int,
)

@Serializable
data class StoredReviewLog(
    val id: Int,
    val poemId: Int,
    val reviewRound: Int,
    val minTotalScore: Double,
    val userTotalScore: Double,
    val cardIndex: Int,
    val sessionReviewed: Int,
    val sessionMistakes: Int,
    val sessionLearned: Int
)

data class MemorizationSummary(
    val activePoemCount: Int,
    val dueCardsToday: Int,
)

data class MemorizationPoem(
    val poemId: Int,
    val title: String,
    val poetName: String,
    val categoryName: String,
    val addedAtMillis: Long,
    val status: MemorizationStatus,
    val totalCards: Int,
    val reviewedCards: Int,
    val reviewSessionsCount: Int,
    val nextReviewDays: Int,
    val dueDate: Long
)

data class QuickStartTarget(
    val poetId: Int? = null,
    val catId: Int? = null,
    val catTitle: String? = null,
)

sealed interface MemorizationError : abkabk.azbarkon.core.domain.result.Error {
    data object MaxActivePoemsReached : MemorizationError

    data object PoemNotFound : MemorizationError

    data object CardNotFound : MemorizationError

    data object Unknown : MemorizationError
}
