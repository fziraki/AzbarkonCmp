package abkabk.azbarkon.data.local

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.dbQuery
import abkabk.azbarkon.data.mapper.toSrsCard
import abkabk.azbarkon.data.mapper.toStorageValue
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredActivePoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import abkabk.azbarkon.core.util.consecutiveDayStreak
import abkabk.azbarkon.core.util.dayKeyFromMillis
import com.sarv.db.CatQueries
import com.sarv.db.PoetQueries
import com.azbarkon.memorization.ActiveSrsPoemQueries
import com.azbarkon.memorization.MemorizationDatabase
import com.azbarkon.memorization.ReviewLogQueries
import com.azbarkon.memorization.SrsPoemCardQueries

class SqlDelightMemorizationLocalDataSource(
    private val activePoemQueries: ActiveSrsPoemQueries,
    private val cardQueries: SrsPoemCardQueries,
    private val reviewLogQueries: ReviewLogQueries,
    private val poetQueries: PoetQueries,
    private val catQueries: CatQueries,
    private val database: MemorizationDatabase,
) : MemorizationLocalDataSource {
    override suspend fun countActivePoems(): Int =
        activePoemQueries.countActive().executeAsOne().toInt()

    override suspend fun isPoemActive(poemId: Int): Boolean =
        activePoemQueries.isPoemActive(poem_id = poemId.toLong()).executeAsOne()

    override suspend fun insertActivePoem(
        poemId: Int,
        addedAtMillis: Long,
        status: String,
    ) {
        activePoemQueries.insertActivePoem(
            poem_id = poemId.toLong(),
            added_at = addedAtMillis,
            status = status,
        )
    }

    override suspend fun deleteActivePoem(poemId: Int) {
        database.transaction {
            cardQueries.deleteCardsByPoemId(poem_id = poemId.toLong())
            activePoemQueries.deleteActivePoem(poem_id = poemId.toLong())
        }
    }

    override suspend fun getActivePoemIds(): List<Int> =
        activePoemQueries
            .selectActivePoemIds()
            .executeAsList()
            .map { it.toInt() }

    override suspend fun getActivePoemAddedAt(poemId: Int): Long? =
        activePoemQueries
            .selectActivePoem(poem_id = poemId.toLong())
            .executeAsOneOrNull()
            ?.added_at

    override suspend fun insertCards(cards: List<SrsCard>) {
        database.transaction {
            cards.forEach { card ->
                cardQueries.insertCard(
                    poem_id = card.poemId.toLong(),
                    card_index = card.cardIndex.toLong(),
                    front = card.front,
                    back = card.back,
                    interval = card.interval.toLong(),
                    due_date = card.dueDateMillis,
                    consecutive_correct = card.consecutiveCorrect.toLong(),
                    score = card.score,
                )
            }
        }
    }

    override suspend fun getCardById(cardId: Long): SrsCard? =
        cardQueries.selectCardById(id = cardId).executeAsOneOrNull()?.toSrsCard()

    override suspend fun getDueCards(
        nowMillis: Long,
        poemId: Int?,
    ): List<SrsCard> =
        if (poemId != null) {
            cardQueries
                .selectDueCardsByPoemId(
                    poem_id = poemId.toLong(),
                    due_date = nowMillis,
                ).executeAsList()
        } else {
            cardQueries
                .selectDueCards(due_date = nowMillis)
                .executeAsList()
        }.map { it.toSrsCard() }

    override suspend fun getCardsByPoemId(poemId: Int): List<SrsCard> =
        cardQueries
            .selectCardsByPoemId(poem_id = poemId.toLong())
            .executeAsList()
            .map { it.toSrsCard() }

    override suspend fun countDueCards(
        nowMillis: Long,
        poemId: Int?,
    ): Int =
        if (poemId != null) {
            cardQueries
                .countDueCardsByPoemId(
                    poem_id = poemId.toLong(),
                    due_date = nowMillis,
                ).executeAsOne()
        } else {
            cardQueries.countDueCards(due_date = nowMillis).executeAsOne()
        }.toInt()

    override suspend fun updateCard(card: SrsCard) {
        cardQueries.updateCard(
            interval = card.interval.toLong(),
            due_date = card.dueDateMillis,
            consecutive_correct = card.consecutiveCorrect.toLong(),
            score = card.score,
            id = card.id,
        )
    }

    override suspend fun updateCardsByPoemIdSchedule(
        poemId: Int,
        interval: Int,
        dueDateMillis: Long,
        score: Double,
        consecutiveEasy: Int,
    ) {
        cardQueries.updateCardsByPoemIdSchedule(
            interval = interval.toLong(),
            due_date = dueDateMillis,
            score = score,
            consecutive_correct = consecutiveEasy.toLong(),
            poem_id = poemId.toLong(),
        )
    }

    override suspend fun countCardsByPoemId(poemId: Int): Int =
        cardQueries.countCardsByPoemId(poem_id = poemId.toLong()).executeAsOne().toInt()

    override suspend fun countReviewedCardsByPoemId(poemId: Int): Int =
        cardQueries
            .countReviewedCardsByPoemId(poem_id = poemId.toLong())
            .executeAsOne()
            .toInt()

    override suspend fun getMaxIntervalByPoemId(poemId: Int): Int =
        cardQueries
            .getMaxIntervalByPoemId(poem_id = poemId.toLong()) { max -> max?.toInt() ?: 0 }
            .executeAsOne()

    override suspend fun getReviewCountByPoemId(poemId: Int): Int =
        reviewLogQueries
            .countReviewSessionsByPoemId(poem_id = poemId.toLong())
            .executeAsOne()
            .toInt()

    override suspend fun getActivePoemIdsByStatus(status: String): List<Int> =
        activePoemQueries
            .selectPoemIdsByStatus(status = status)
            .executeAsList()
            .map { it.toInt() }

    override suspend fun updatePoemStatus(
        poemId: Int,
        status: String,
    ) {
        activePoemQueries.updatePoemStatus(
            status = status,
            poem_id = poemId.toLong(),
        )
    }

    override suspend fun insertReviewLog(
        cardId: Long,
        grade: SrsGrade,
        previousInterval: Int,
        newInterval: Int,
        reviewTimeMillis: Long,
    ) {
        reviewLogQueries.insertReviewLog(
            card_id = cardId,
            grade = grade.toStorageValue(),
            previous_interval = previousInterval.toLong(),
            new_interval = newInterval.toLong(),
            review_time = reviewTimeMillis,
        )
    }

    override suspend fun getReviewDayKeys(): List<Int> =
        reviewLogQueries
            .selectReviewDayKeys()
            .executeAsList()
            .map { dayKeyFromMillis(it) }

    override suspend fun countReviewedVerses(): Int =
        reviewLogQueries.countReviewedVerses().executeAsOne().toInt()

    override suspend fun dumpActivePoems(): List<StoredActivePoem> =
        activePoemQueries.selectAll().executeAsList().map { row ->
            StoredActivePoem(
                poemId = row.poem_id.toInt(),
                addedAtMillis = row.added_at,
                status = row.status,
            )
        }

    override suspend fun dumpCards(): List<SrsCard> =
        cardQueries.selectAllCards().executeAsList().map { it.toSrsCard() }

    override suspend fun dumpReviewLogs(): List<StoredReviewLog> =
        reviewLogQueries.selectAllReviewLogs().executeAsList().map { row ->
            StoredReviewLog(
                id = row.id,
                cardId = row.card_id,
                grade = row.grade,
                previousInterval = row.previous_interval.toInt(),
                newInterval = row.new_interval.toInt(),
                reviewTimeMillis = row.review_time,
            )
        }

    override suspend fun replaceAll(
        activePoems: List<StoredActivePoem>,
        cards: List<SrsCard>,
        reviewLogs: List<StoredReviewLog>,
    ) {
        database.transaction {
            activePoemQueries.deleteAll()
            cardQueries.deleteAllCards()
            reviewLogQueries.deleteAllReviewLogs()

            activePoems.forEach { poem ->
                activePoemQueries.insertActivePoem(
                    poem_id = poem.poemId.toLong(),
                    added_at = poem.addedAtMillis,
                    status = poem.status,
                )
            }
            cards.forEach { card ->
                cardQueries.insertCardWithId(
                    id = card.id,
                    poem_id = card.poemId.toLong(),
                    card_index = card.cardIndex.toLong(),
                    front = card.front,
                    back = card.back,
                    interval = card.interval.toLong(),
                    due_date = card.dueDateMillis,
                    consecutive_correct = card.consecutiveCorrect.toLong(),
                    score = card.score,
                )
            }
            reviewLogs.forEach { log ->
                reviewLogQueries.insertReviewLogWithId(
                    id = log.id,
                    card_id = log.cardId,
                    grade = log.grade,
                    previous_interval = log.previousInterval.toLong(),
                    new_interval = log.newInterval.toLong(),
                    review_time = log.reviewTimeMillis,
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
