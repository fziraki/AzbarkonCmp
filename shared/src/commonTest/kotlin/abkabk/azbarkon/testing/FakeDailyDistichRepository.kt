package abkabk.azbarkon.testing

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.RandomDistich
import abkabk.azbarkon.domain.repository.DailyDistichRepository

class FakeDailyDistichRepository : DailyDistichRepository {
    var shouldFail: Boolean = false
    var todayDistich: RandomDistich = RandomDistich(
        poemId = 1,
        vorder = 1,
        rightText = "مصراع راست",
        leftText = "مصراع چپ",
        poetName = "حافظ",
    )

    override suspend fun getRandomDistich(seed: Long, poetId: Int): Result<RandomDistich, DataError.Local> =
        if (shouldFail) Result.Error(DataError.Local.UNKNOWN)
        else Result.Success(todayDistich)

    override suspend fun getTodayDistich(): Result<RandomDistich, DataError.Local> =
        if (shouldFail) Result.Error(DataError.Local.UNKNOWN)
        else Result.Success(todayDistich)
}
