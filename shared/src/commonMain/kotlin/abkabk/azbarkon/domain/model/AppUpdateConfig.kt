package abkabk.azbarkon.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateConfig(
    val android: PlatformUpdateConfig = PlatformUpdateConfig(),
    val ios: PlatformUpdateConfig = PlatformUpdateConfig(),
)

@Serializable
data class PlatformUpdateConfig(
    val stable_version_code: Int = 0,
    val last_version_code: Int = 0,
)
