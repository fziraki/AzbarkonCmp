package abkabk.azbarkon.domain.model.memorization

import kotlinx.serialization.Serializable

enum class SrsGrade {
    AGAIN,
    HARD,
    GOOD,
    EASY,
}

enum class ActiveMemorizationStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
}

@Serializable
data class SrsCard(
    val id: Long,
    val poemId: Int,
    val cardIndex: Int,
    val front: String,
    val back: String,
    val interval: Int,
    val dueDateMillis: Long,
    val consecutiveCorrect: Int,
    val score: Double = 0.0,
)

@Serializable
data class StoredActivePoem(
    val poemId: Int,
    val addedAtMillis: Long,
    val status: String,
)

@Serializable
data class StoredReviewLog(
    val id: Long,
    val cardId: Long,
    val grade: String,
    val previousInterval: Int,
    val newInterval: Int,
    val reviewTimeMillis: Long,
)

data class MemorizationSummary(
    val activePoemCount: Int,
    val dueCardsToday: Int,
)

data class ActiveMemorizationPoem(
    val poemId: Int,
    val title: String,
    val poetName: String,
    val categoryName: String,
    val addedAtMillis: Long,
    val status: ActiveMemorizationStatus,
    val totalCards: Int,
    val reviewedCards: Int,
    val dueCards: Int,
    val reviewCount: Int,
    val nextReviewDays: Int,
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
