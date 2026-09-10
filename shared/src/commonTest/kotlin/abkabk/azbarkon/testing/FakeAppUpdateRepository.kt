package abkabk.azbarkon.testing

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.AppUpdateConfig
import abkabk.azbarkon.domain.platform.AppUpdateRepository

class FakeAppUpdateRepository : AppUpdateRepository {
    var updateConfig: AppUpdateConfig = AppUpdateConfig()
    var shouldFail = false

    override suspend fun getUpdateConfig(): Result<AppUpdateConfig, DataError.Network> =
        if (shouldFail) {
            Result.Error(DataError.Network.UNKNOWN)
        } else {
            Result.Success(updateConfig)
        }
}
