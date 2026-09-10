package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.util.currentLocalDateSeed
import abkabk.azbarkon.domain.datasource.DailyDistichLocalDataSource
import abkabk.azbarkon.domain.model.RandomDistich
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OfflineFirstDailyDistichRepositoryTest {
    @Test
    fun `getTodayDistich uses local data source with stable seed`() =
        runTest {
            val fakeDataSource = FakeDailyDistichLocalDataSource()
            val repository = OfflineFirstDailyDistichRepository(fakeDataSource)

            val result = repository.getTodayDistich()

            assertThat(result).isInstanceOf(Result.Success::class)
            assertThat((result as Result.Success).data.poetName).isEqualTo("حافظ")
            assertThat(fakeDataSource.lastSeed).isEqualTo(currentLocalDateSeed())
            assertThat(fakeDataSource.lastPoetId).isEqualTo(0)
        }

    @Test
    fun `getRandomDistich forwards poet filter to local data source`() =
        runTest {
            val fakeDataSource = FakeDailyDistichLocalDataSource()
            val repository = OfflineFirstDailyDistichRepository(fakeDataSource)

            repository.getRandomDistich(seed = 42L, poetId = 7)

            assertThat(fakeDataSource.lastSeed).isEqualTo(42L)
            assertThat(fakeDataSource.lastPoetId).isEqualTo(7)
        }

    private class FakeDailyDistichLocalDataSource : DailyDistichLocalDataSource {
        var lastSeed: Long? = null
        var lastPoetId: Int? = null

        override suspend fun getRandomDistich(
            seed: Long,
            poetId: Int,
        ): Result<RandomDistich, DataError.Local> {
            lastSeed = seed
            lastPoetId = poetId
            return Result.Success(
                RandomDistich(
                    poemId = 10,
                    vorder = 2,
                    rightText = "الا یا ایها الساقی",
                    leftText = "بدو تا جام جم بده",
                    poetName = "حافظ",
                ),
            )
        }
    }
}
