package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.domain.model.RandomDistich
import abkabk.azbarkon.shared.R
import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class DailyDistichNotificationPresenter(
    private val context: Context,
) {
    fun show(distich: RandomDistich) {
        if (!context.canPostNotifications()) return

        context.ensureNotificationChannel(
            DailyDistichNotificationPayload.CHANNEL_ID,
            context.getString(R.string.daily_distich_notification_channel_name),
            context.getString(R.string.daily_distich_notification_channel_description),
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC,
        )

        val expandedView = buildRemoteViews(distich, expanded = true)
        val collapsedView = buildRemoteViews(distich, expanded = false)

        val notification =
            NotificationCompat
                .Builder(context, DailyDistichNotificationPayload.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.daily_distich_notification_title))
                .setContentText(distichBody(distich))
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setContentIntent(
                    context.launchAppPendingIntent(distich.poemId) {
                        putExtra(DailyDistichNotificationPayload.KEY_POEM_ID, distich.poemId)
                    },
                )
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context).notify(
            DailyDistichNotificationPayload.NOTIFICATION_ID,
            notification,
        )
    }

    private fun distichBody(distich: RandomDistich): String =
        "${distich.rightText}\n${distich.leftText}"

    private fun buildRemoteViews(
        distich: RandomDistich,
        expanded: Boolean,
    ): RemoteViews {
        val layoutId =
            if (expanded) {
                R.layout.notification_daily_distich
            } else {
                R.layout.notification_daily_distich_collapsed
            }
        return RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.notification_title, context.getString(R.string.daily_distich_notification_title))
            setTextViewText(R.id.notification_right_line, distich.rightText)
            setTextViewText(R.id.notification_left_line, distich.leftText)
            setTextViewText(R.id.notification_poet_name, distich.poetName)
        }
    }
}
