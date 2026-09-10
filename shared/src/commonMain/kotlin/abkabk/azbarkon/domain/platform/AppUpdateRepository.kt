package abkabk.azbarkon.domain.platform

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.AppUpdateConfig

interface AppUpdateRepository {
    suspend fun getUpdateConfig(): Result<AppUpdateConfig, DataError.Network>
}
