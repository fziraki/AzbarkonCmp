package abkabk.azbarkon.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateConfig(
    val android: PlatformUpdateConfig = PlatformUpdateConfig(),
    val ios: PlatformUpdateConfig = PlatformUpdateConfig(),
)

@Serializable
data class PlatformUpdateConfig(
    @SerialName("stable_version_code") val stableVersionCode: Int = 0,
    @SerialName("last_version_code") val lastVersionCode: Int = 0,
)
