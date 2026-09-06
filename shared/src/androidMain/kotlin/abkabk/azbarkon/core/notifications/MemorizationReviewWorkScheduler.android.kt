package abkabk.azbarkon.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal object MemorizationReviewWorkScheduler {
    fun schedule(
        context: Context,
        deliveryHour: Int,
        deliveryMinute: Int,
    ) {
        val request =
            PeriodicWorkRequestBuilder<MemorizationReviewWorker>(
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
            MemorizationReviewNotificationPayload.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(
            MemorizationReviewNotificationPayload.WORK_NAME,
        )
    }
}
