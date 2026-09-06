package abkabk.azbarkon.domain.datasource

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.RandomDistich

interface DailyDistichLocalDataSource {
    suspend fun getRandomDistich(
        seed: Long,
        poetId: Int = 0,
    ): Result<RandomDistich, DataError.Local>
}
