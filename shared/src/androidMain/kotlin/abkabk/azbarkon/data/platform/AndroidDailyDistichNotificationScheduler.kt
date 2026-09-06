package abkabk.azbarkon.data.platform

import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.notifications.DailyDistichNotificationPresenter
import abkabk.azbarkon.core.notifications.DailyDistichWorkScheduler
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.repository.DailyDistichRepository
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidDailyDistichNotificationScheduler(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dailyDistichRepository: DailyDistichRepository,
    private val notificationPresenter: DailyDistichNotificationPresenter,
    private val ioDispatcher: CoroutineDispatcher,
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
        DailyDistichWorkScheduler.schedule(
            context = context,
            deliveryHour = deliveryHour,
            deliveryMinute = deliveryMinute,
        )

        if (showImmediately) {
            scope.launch {
                withContext(ioDispatcher) {
                    dailyDistichRepository.getTodayDistich()
                }.onSuccess { distich ->
                    notificationPresenter.show(distich)
                }
            }
        }
    }

    override fun disable() {
        DailyDistichWorkScheduler.cancel(context)
    }

    override fun rescheduleIfEnabled() {
        if (userPreferencesRepository.isDailyDistichNotificationEnabled()) {
            enable(deliveryHour, deliveryMinute)
        }
    }

    private companion object {
        const val DEFAULT_HOUR = 8
        const val DEFAULT_MINUTE = 0
    }
}
