package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.shared.R
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MemorizationReviewWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters),
    KoinComponent {
    private val localDataSource: MemorizationLocalDataSource by inject()
    private val scheduler: MemorizationReviewNotificationScheduler by inject()
    private val notificationPresenter: MemorizationReviewNotificationPresenter by inject()

    override suspend fun doWork(): Result {
        return try {
            if (localDataSource.countActivePoems() == 0) {
                scheduler.disable()
                return Result.success()
            }

            val poemIds = localDataSource.getPoemIdsByStatus("ACTIVE")
            val dueCount = poemIds.sumOf { localDataSource.countDueCards(it) }
            if (dueCount > 0) {
                notificationPresenter.show(dueCount)
            }
            Result.success()
        } catch (e: IllegalStateException) {
            Napier.e("MemorizationReviewWorker failed", e)
            showFailureNotification()
            Result.failure()
        }
    }

    private fun showFailureNotification() {
        if (!applicationContext.canPostNotifications()) return

        applicationContext.ensureNotificationChannel(
            MemorizationReviewNotificationPayload.CHANNEL_ID,
            applicationContext.getString(R.string.memorization_review_notification_channel_name),
            applicationContext.getString(R.string.memorization_review_notification_channel_description),
        )

        val retryIntent =
            applicationContext.launchAppPendingIntent(
                MemorizationReviewNotificationPayload.NOTIFICATION_ID + 1,
            ) {
                putExtra(MemorizationReviewNotificationPayload.KEY_OPEN_MEMORIZATION_PRACTICE, true)
            }

        val notification =
            androidx.core.app.NotificationCompat
                .Builder(applicationContext, MemorizationReviewNotificationPayload.CHANNEL_ID)
                .setSmallIcon(abkabk.azbarkon.shared.R.drawable.ic_notification)
                .setContentTitle(applicationContext.getString(R.string.memorization_review_worker_failed_title))
                .setContentText(applicationContext.getString(R.string.memorization_review_worker_failed_body))
                .setContentIntent(retryIntent)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .build()

        androidx.core.app.NotificationManagerCompat.from(applicationContext).notify(
            MemorizationReviewNotificationPayload.NOTIFICATION_ID + 1,
            notification,
        )
    }
}
