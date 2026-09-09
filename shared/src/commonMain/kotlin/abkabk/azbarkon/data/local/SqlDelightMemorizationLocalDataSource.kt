package abkabk.azbarkon.data.local

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.dbQuery
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.core.util.dayKeyFromMillis
import abkabk.azbarkon.data.mapper.toSrsCard
import abkabk.azbarkon.data.mapper.toStoredReviewLog
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.StoredPoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import com.azbarkon.memorization.MemorizationDatabase
import com.azbarkon.memorization.ReviewLogQueries
import com.azbarkon.memorization.SrsPoemCardQueries
import com.azbarkon.memorization.SrsPoemQueries
import com.sarv.db.CatQueries
import com.sarv.db.PoetQueries

class SqlDelightMemorizationLocalDataSource(
    private val poemQueries: SrsPoemQueries,
    private val cardQueries: SrsPoemCardQueries,
    private val reviewLogQueries: ReviewLogQueries,
    private val poetQueries: PoetQueries,
    private val catQueries: CatQueries,
    private val database: MemorizationDatabase,
) : MemorizationLocalDataSource {
    override suspend fun countActivePoems(): Int =
        poemQueries.countActive().executeAsOne().toInt()

    override suspend fun isPoemActive(poemId: Int): Boolean =
        poemQueries.isPoemActive(poem_id = poemId.toLong()).executeAsOne()

    override suspend fun insertPoem(
        poemId: Int,
        addedAtMillis: Long,
        status: String,
        interval: Int,
        dueDateMillis: Long,
        consecutiveCorrect: Int,
        totalCard: Int
    ) {
        poemQueries.insertPoem(
            poem_id = poemId.toLong(),
            added_at = addedAtMillis,
            status = status,
            interval = interval.toLong(),
            due_date = dueDateMillis,
            consecutive_correct = consecutiveCorrect.toLong(),
            total_cards = totalCard.toLong()
        )
    }

    override suspend fun deletePoem(poemId: Int) {
        database.transaction {
            reviewLogQueries.deleteReviewLogsByPoemId(poem_id = poemId.toLong())
            cardQueries.deleteCardsByPoemId(poem_id = poemId.toLong())
            poemQueries.deletePoem(poem_id = poemId.toLong())
        }
    }


    override suspend fun getPoemAddedAt(poemId: Int): Long? =
        poemQueries
            .selectPoem(poem_id = poemId.toLong())
            .executeAsOneOrNull()
            ?.added_at

    override suspend fun getMemorizationPoem(poemId: Int): StoredPoem? =
        poemQueries
            .selectPoem(poem_id = poemId.toLong())
            .executeAsOneOrNull()
            ?.let { row ->
                StoredPoem(
                    poemId = row.poem_id.toInt(),
                    addedAtMillis = row.added_at,
                    status = row.status,
                    interval = row.interval.toInt(),
                    dueDate = row.due_date,
                    consecutiveCorrect = row.consecutive_correct.toInt(),
                    totalCards = row.total_cards.toInt(),
                )
            }

    override suspend fun insertCards(cards: List<SrsCard>) {
        database.transaction {
            cards.forEach { card ->
                cardQueries.insertCard(
                    poem_id = card.poemId.toLong(),
                    front = card.front,
                    back = card.back,
                )
            }
        }
    }

    override suspend fun getCardById(cardId: Long): SrsCard? =
        cardQueries.selectCardById(id = cardId).executeAsOneOrNull()?.toSrsCard()

    override suspend fun getDueCards(
        poemId: Int,
    ): List<SrsCard> =
        cardQueries
            .selectDueCardsByPoemId(
                poem_id = poemId.toLong(),
                due_date = currentTimeMillis()
            ).executeAsList()
            .map { it.toSrsCard() }

    override suspend fun getCardsByPoemId(poemId: Int): List<SrsCard> =
        cardQueries
            .selectCardsByPoemId(poem_id = poemId.toLong())
            .executeAsList()
            .map { it.toSrsCard() }

    override suspend fun countDueCards(
        poemId: Int,
    ): Int =
        cardQueries
            .countDueCardsByPoemId(
                poem_id = poemId.toLong(),
                due_date = currentTimeMillis(),
            ).executeAsOne()
            .toInt()

    override suspend fun updatePoemSchedule(
        poemId: Int,
        status: String,
        interval: Int,
        dueDateMillis: Long,
        consecutiveCorrect: Int,
    ) {
        poemQueries.updatePoemSchedule(
            poem_id = poemId.toLong(),
            status = status,
            interval = interval.toLong(),
            due_date = dueDateMillis,
            consecutive_correct = consecutiveCorrect.toLong(),
        )
    }

    override suspend fun countCardsByPoemId(poemId: Int): Int =
        cardQueries.countCardsByPoemId(poem_id = poemId.toLong()).executeAsOne().toInt()


    override suspend fun getPoemIdsByStatus(status: String): List<Int> =
        poemQueries
            .selectPoemIdsByStatus(status = status)
            .executeAsList()
            .map { it.toInt() }

    override suspend fun insertReviewLog(
        poemId: Int,
        reviewRound: Int,
        minTotalScore: Double,
        userTotalScore: Double,
        cardIndex: Int,
        sessionReviewed: Int,
        sessionMistakes: Int,
        sessionLearned: Int
    ) {
        reviewLogQueries.insertReviewLog(
            poemId.toLong(),
            reviewRound.toLong(),
            minTotalScore,
            userTotalScore,
            cardIndex.toLong(),
            sessionReviewed.toLong(),
            sessionMistakes.toLong(),
            sessionLearned.toLong()
        )
    }

    override suspend fun getLastReviewLogByPoemId(poemId: Int): StoredReviewLog =
        reviewLogQueries
                .lastReviewLogByPoemId(poem_id = poemId.toLong())
                .executeAsOneOrNull()?.toStoredReviewLog() ?:run {
                StoredReviewLog(
                    id = -1,
                    poemId = poemId,
                    reviewRound = 0,
                    minTotalScore = 0.0,
                    userTotalScore = 0.0,
                    cardIndex = 0,
                    sessionReviewed = 0,
                    sessionMistakes = 0,
                    sessionLearned = 0
                )
            }

    override suspend fun getReviewDayKeys(): List<Int> =
        reviewLogQueries
            .selectReviewDayKeys()
            .executeAsList()
            .map { dayKeyFromMillis(it) }

    override suspend fun countReviewedVerses(): Int =
        reviewLogQueries.countReviewedVerses().executeAsOne().toInt()

    override suspend fun dumpActivePoems(): List<StoredPoem> =
        poemQueries.selectAll().executeAsList().map { row ->
            StoredPoem(
                poemId = row.poem_id.toInt(),
                addedAtMillis = row.added_at,
                status = row.status,
                interval = row.interval.toInt(),
                dueDate = row.due_date,
                consecutiveCorrect = row.consecutive_correct.toInt(),
                totalCards = row.total_cards.toInt(),
            )
        }

    override suspend fun dumpCards(): List<SrsCard> =
        cardQueries.selectAllCards().executeAsList().map { it.toSrsCard() }

    override suspend fun dumpReviewLogs(): List<StoredReviewLog> =
        reviewLogQueries.selectAllReviewLogs().executeAsList().map { row ->
            StoredReviewLog(
                id = row.id.toInt(),
                poemId = row.poem_id.toInt(),
                reviewRound = row.review_round.toInt(),
                minTotalScore = row.min_total_score,
                userTotalScore = row.user_total_score,
                cardIndex = row.card_index.toInt(),
                sessionReviewed = row.session_reviewed.toInt(),
                sessionMistakes = row.session_mistakes.toInt(),
                sessionLearned = row.session_learned.toInt()
            )
        }

    override suspend fun replaceAll(
        activePoems: List<StoredPoem>,
        cards: List<SrsCard>,
        reviewLogs: List<StoredReviewLog>,
    ) {
        database.transaction {
            poemQueries.deleteAll()
            cardQueries.deleteAllCards()
            reviewLogQueries.deleteAllReviewLogs()

            activePoems.forEach { poem ->
                poemQueries.insertPoem(
                    poem_id = poem.poemId.toLong(),
                    added_at = poem.addedAtMillis,
                    status = poem.status,
                    interval = poem.interval.toLong(),
                    due_date = poem.dueDate,
                    consecutive_correct = poem.consecutiveCorrect.toLong(),
                    total_cards = poem.totalCards.toLong()
                )
            }
            cards.forEach { card ->
                cardQueries.insertCardWithId(
                    id = card.id,
                    poem_id = card.poemId.toLong(),
                    front = card.front,
                    back = card.back,
                )
            }
            reviewLogs.forEach { log ->
                reviewLogQueries.insertReviewLogWithId(
                    id = log.id.toLong(), poem_id = log.poemId.toLong(), review_round = log.reviewRound.toLong(),
                    min_total_score = log.minTotalScore, user_total_score = log.userTotalScore,
                    card_index = log.cardIndex.toLong(), session_reviewed = log.sessionReviewed.toLong(),
                    session_mistakes = log.sessionMistakes.toLong(), session_learned = log.sessionLearned.toLong()
                )
            }
        }
    }

    override suspend fun findPoetIdByName(nameFragment: String): Result<Int, DataError.Local> =
        dbQuery {
            poetQueries
                .selectIdByNameLike(nameFragment)
                .executeAsOneOrNull()
                ?.toInt()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
        }

    override suspend fun findCategoryByPoetAndText(
        poetId: Int,
        textFragment: String,
    ): Result<Pair<Int, String>, DataError.Local> =
        dbQuery {
            val row =
                catQueries
                    .selectIdByPoetIdAndText(
                        poet_id = poetId.toLong(),
                        textFragment,
                    ).executeAsOneOrNull()
                    ?: return Result.Error(DataError.Local.NOT_FOUND)
            row.id.toInt() to row.text
        }
}
