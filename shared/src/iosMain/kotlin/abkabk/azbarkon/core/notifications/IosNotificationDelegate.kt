package abkabk.azbarkon.core.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

object IosNotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
    val poemId = MutableStateFlow<Int?>(null)
    val openMemorizationPractice = MutableStateFlow(false)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun install() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = this
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (ULong) -> Unit,
    ) {
        withCompletionHandler(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit,
    ) {
        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        userInfo[DailyDistichNotificationPayload.KEY_POEM_ID]
            ?.toString()
            ?.toIntOrNull()
            ?.let { poemId.value = it }

        if (userInfo[MemorizationReviewNotificationPayload.KEY_OPEN_MEMORIZATION_PRACTICE]?.toString() == "true") {
            openMemorizationPractice.value = true
            scope.launch {
                delay(1_500)
                openMemorizationPractice.value = false
            }
        }
        withCompletionHandler()
    }
}
