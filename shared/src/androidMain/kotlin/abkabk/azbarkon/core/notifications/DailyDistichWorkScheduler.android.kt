package abkabk.azbarkon.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal object DailyDistichWorkScheduler {
    fun schedule(
        context: Context,
        deliveryHour: Int,
        deliveryMinute: Int,
    ) {
        val request =
            PeriodicWorkRequestBuilder<DailyDistichWorker>(
                DailyDistichScheduleCalculator.PERIODIC_INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setInitialDelay(
                    DailyDistichScheduleCalculator.initialDelayMillis(
                        deliveryHour = deliveryHour,
                        deliveryMinute = deliveryMinute,
                    ),
                    TimeUnit.MILLISECONDS,
                )
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyDistichNotificationPayload.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyDistichNotificationPayload.WORK_NAME)
    }
}
