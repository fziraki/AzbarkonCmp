package abkabk.azbarkon.data.local

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.dbQuery
import abkabk.azbarkon.data.mapper.toRandomDistich
import abkabk.azbarkon.domain.datasource.DailyDistichLocalDataSource
import abkabk.azbarkon.domain.model.RandomDistich
import com.sarv.db.VerseQueries

class SqlDelightDailyDistichLocalDataSource(
    private val verseQueries: VerseQueries,
) : DailyDistichLocalDataSource {
    override suspend fun getRandomDistich(
        seed: Long,
        poetId: Int,
    ): Result<RandomDistich, DataError.Local> =
        dbQuery {
            val filterPoet = if (poetId == 0) 0L else 1L
            val poetIdParam = poetId.toLong()
            val count =
                verseQueries
                    .selectRandomDistichCountByPoet(
                        filter_poet = filterPoet,
                        poet_id = poetIdParam,
                    ).executeAsOne()
            if (count == 0L) {
                return Result.Error(DataError.Local.NOT_FOUND)
            }
            val offset = (seed and Long.MAX_VALUE) % count
            verseQueries
                .selectRandomDistichAtOffsetByPoet(
                    filter_poet = filterPoet,
                    poet_id = poetIdParam,
                    offset = offset,
                ).executeAsOneOrNull()
                ?.toRandomDistich()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
        }
}
