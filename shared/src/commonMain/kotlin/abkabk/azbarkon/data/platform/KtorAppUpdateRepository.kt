package abkabk.azbarkon.data.platform

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.util.Constants
import abkabk.azbarkon.domain.model.AppUpdateConfig
import abkabk.azbarkon.domain.platform.AppUpdateRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class KtorAppUpdateRepository(
    private val httpClient: HttpClient,
) : AppUpdateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getUpdateConfig(): Result<AppUpdateConfig, DataError.Network> =
        try {
            val raw = httpClient.get(Constants.APP_UPDATE_CONFIG_URL).body<String>()
            Result.Success(json.decodeFromString(raw))
        } catch (_: Exception) {
            Result.Error(DataError.Network.SERIALIZATION)
        }
}
