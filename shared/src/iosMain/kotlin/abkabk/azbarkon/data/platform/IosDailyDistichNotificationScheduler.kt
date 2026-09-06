package abkabk.azbarkon.data.platform

import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.notifications.DailyDistichNotificationPayload
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.repository.DailyDistichRepository
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosDailyDistichNotificationScheduler(
    private val dailyDistichRepository: DailyDistichRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : DailyDistichNotificationScheduler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var deliveryHour: Int = DEFAULT_HOUR
    private var deliveryMinute: Int = DEFAULT_MINUTE

    override fun enable(
        deliveryHour: Int,
        deliveryMinute: Int,
        showImmediately: Boolean,
    ) {
        this.deliveryHour = deliveryHour
        this.deliveryMinute = deliveryMinute
        scope.launch {
            scheduleNotification(showImmediately = showImmediately)
        }
    }

    override fun disable() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf(
                DailyDistichNotificationPayload.REQUEST_ID,
                DailyDistichNotificationPayload.IMMEDIATE_REQUEST_ID,
            ),
        )
        center.removeDeliveredNotificationsWithIdentifiers(
            listOf(
                DailyDistichNotificationPayload.REQUEST_ID,
                DailyDistichNotificationPayload.IMMEDIATE_REQUEST_ID,
            ),
        )
    }

    override fun rescheduleIfEnabled() {
        if (userPreferencesRepository.isDailyDistichNotificationEnabled()) {
            enable(deliveryHour, deliveryMinute)
        }
    }

    private suspend fun scheduleNotification(showImmediately: Boolean) {
        registerCategory()
        val center = UNUserNotificationCenter.currentNotificationCenter()

        dailyDistichRepository.getTodayDistich().onSuccess { distich ->
            if (showImmediately) {
                val immediateRequest =
                    UNNotificationRequest.requestWithIdentifier(
                        identifier = DailyDistichNotificationPayload.IMMEDIATE_REQUEST_ID,
                        content = buildPreviewContent(distich),
                        trigger = null,
                    )
                center.addNotificationRequest(immediateRequest, withCompletionHandler = null)
            }

            val content = buildNotificationContent(distich)

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
                    identifier = DailyDistichNotificationPayload.REQUEST_ID,
                    content = content,
                    trigger = trigger,
                )

            center.removePendingNotificationRequestsWithIdentifiers(
                listOf(DailyDistichNotificationPayload.REQUEST_ID),
            )
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    private fun buildPreviewContent(distich: abkabk.azbarkon.domain.model.RandomDistich): UNMutableNotificationContent =
        UNMutableNotificationContent().apply {
            setTitle("بیت امروز")
            setSubtitle(distich.poetName)
            setBody("${distich.rightText}\n${distich.leftText}")
        }

    private fun buildNotificationContent(distich: abkabk.azbarkon.domain.model.RandomDistich): UNMutableNotificationContent =
        UNMutableNotificationContent().apply {
            setTitle("بیت امروز")
            setSubtitle(distich.poetName)
            setBody("${distich.rightText}\n${distich.leftText}")
            setCategoryIdentifier(DAILY_DISTICH_CATEGORY)
            setUserInfo(
                mapOf<Any?, Any?>(
                    DailyDistichNotificationPayload.KEY_POET_NAME to distich.poetName,
                    DailyDistichNotificationPayload.KEY_RIGHT_TEXT to distich.rightText,
                    DailyDistichNotificationPayload.KEY_LEFT_TEXT to distich.leftText,
                    DailyDistichNotificationPayload.KEY_POEM_ID to distich.poemId.toString(),
                    DailyDistichNotificationPayload.KEY_VORDER to distich.vorder.toString(),
                ),
            )
        }

    private fun registerCategory() {
        val category =
            UNNotificationCategory.categoryWithIdentifier(
                identifier = DAILY_DISTICH_CATEGORY,
                actions = emptyList<UNNotificationAction>(),
                intentIdentifiers = emptyList<String>(),
                options = 0uL,
            )
        UNUserNotificationCenter.currentNotificationCenter().setNotificationCategories(setOf(category))
    }

    private companion object {
        const val DEFAULT_HOUR = 8
        const val DEFAULT_MINUTE = 0
        const val DAILY_DISTICH_CATEGORY = "DAILY_DISTICH"
    }
}
