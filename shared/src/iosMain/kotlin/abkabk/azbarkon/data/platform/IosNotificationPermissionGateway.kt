package abkabk.azbarkon.data.platform

import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume

class IosNotificationPermissionGateway : NotificationPermissionGateway {
    @Volatile
    private var notificationsEnabled: Boolean = false

    init {
        refreshEnabledState()
    }

    override fun areNotificationsEnabled(): Boolean = notificationsEnabled

    override suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                completionHandler = { granted, _ ->
                    refreshEnabledState()
                    if (continuation.isActive) {
                        continuation.resume(granted)
                    }
                },
            )
        }

    private fun refreshEnabledState() {
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                val status = settings?.authorizationStatus
                notificationsEnabled =
                    status == UNAuthorizationStatusAuthorized ||
                        status == UNAuthorizationStatusProvisional
            }
    }
}
