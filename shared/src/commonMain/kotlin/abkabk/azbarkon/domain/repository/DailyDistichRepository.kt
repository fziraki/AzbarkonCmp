package abkabk.azbarkon.domain.repository

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.RandomDistich

interface DailyDistichRepository {
    suspend fun getRandomDistich(
        seed: Long,
        poetId: Int = 0,
    ): Result<RandomDistich, DataError.Local>

    suspend fun getTodayDistich(): Result<RandomDistich, DataError.Local>
}
