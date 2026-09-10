package abkabk.azbarkon.data.mapper

import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import com.azbarkon.memorization.Review_log
import com.azbarkon.memorization.Srs_poem_card
import kotlin.Int

fun Srs_poem_card.toSrsCard(): SrsCard =
    SrsCard(
        id = id,
        poemId = poem_id.toInt(),
        front = front,
        back = back,
    )

fun Review_log.toStoredReviewLog(): StoredReviewLog =
    StoredReviewLog(
        id = id.toInt(),
        poemId = poem_id.toInt(),
        reviewRound = review_round.toInt(),
        minTotalScore = min_total_score,
        userTotalScore = user_total_score,
        cardIndex = card_index.toInt(),
        sessionReviewed = session_reviewed.toInt(),
        sessionMistakes = session_mistakes.toInt(),
        sessionLearned = session_learned.toInt()
    )
