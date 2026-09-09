package abkabk.azbarkon.data.platform

import abkabk.azbarkon.core.notifications.MemorizationReviewNotificationPayload
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosMemorizationReviewNotificationScheduler(
    private val localDataSource: MemorizationLocalDataSource,
) : MemorizationReviewNotificationScheduler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var deliveryHour: Int = DEFAULT_HOUR
    private var deliveryMinute: Int = DEFAULT_MINUTE

    override fun enable(
        deliveryHour: Int,
        deliveryMinute: Int,
    ) {
        this.deliveryHour = deliveryHour
        this.deliveryMinute = deliveryMinute
        scope.launch {
            scheduleNotificationIfDue()
        }
    }

    override fun disable() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf(MemorizationReviewNotificationPayload.REQUEST_ID),
        )
        center.removeDeliveredNotificationsWithIdentifiers(
            listOf(MemorizationReviewNotificationPayload.REQUEST_ID),
        )
    }

    override fun rescheduleIfActive() {
        scope.launch {
            if (localDataSource.countActivePoems() == 0) {
                disable()
            } else {
                enable(deliveryHour, deliveryMinute)
            }
        }
    }

    private suspend fun scheduleNotificationIfDue() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf(MemorizationReviewNotificationPayload.REQUEST_ID),
        )

        val poemIds = localDataSource.getPoemIdsByStatus("ACTIVE")
        val dueCount = poemIds.sumOf { localDataSource.countDueCards(currentTimeMillis(), it) }
        if (dueCount <= 0) return

        val content =
            UNMutableNotificationContent().apply {
                setTitle(MEMORIZATION_REVIEW_TITLE)
                setBody("$dueCount بیت")
                setUserInfo(
                    mapOf<Any?, Any?>(
                        MemorizationReviewNotificationPayload.KEY_OPEN_MEMORIZATION_PRACTICE to "true",
                        MemorizationReviewNotificationPayload.KEY_DUE_CARD_COUNT to dueCount.toString(),
                    ),
                )
            }

        val dateComponents =
            NSDateComponents().apply {
                hour = deliveryHour.toLong()
                minute = deliveryMinute.toLong()
            }

        val trigger =
            UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                dateComponents = dateComponents,
                repeats = true,
            )

        val request =
            UNNotificationRequest.requestWithIdentifier(
                identifier = MemorizationReviewNotificationPayload.REQUEST_ID,
                content = content,
                trigger = trigger,
            )

        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    private companion object {
        const val DEFAULT_HOUR = 10
        const val DEFAULT_MINUTE = 0
        const val MEMORIZATION_REVIEW_TITLE = "مرور امروز"
    }
}
