package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.util.currentLocalDateSeed
import abkabk.azbarkon.domain.datasource.DailyDistichLocalDataSource
import abkabk.azbarkon.domain.model.RandomDistich
import abkabk.azbarkon.domain.repository.DailyDistichRepository

class OfflineFirstDailyDistichRepository(
    private val localDataSource: DailyDistichLocalDataSource,
) : DailyDistichRepository {
    private var cachedSeed: Long? = null
    private var cachedResult: Result<RandomDistich, DataError.Local>? = null

    override suspend fun getRandomDistich(
        seed: Long,
        poetId: Int,
    ): Result<RandomDistich, DataError.Local> =
        localDataSource.getRandomDistich(seed, poetId)

    override suspend fun getTodayDistich(): Result<RandomDistich, DataError.Local> {
        val seed = currentLocalDateSeed()
        if (cachedSeed != seed) {
            cachedResult = localDataSource.getRandomDistich(seed)
            cachedSeed = seed
        }
        return cachedResult!!
    }
}
