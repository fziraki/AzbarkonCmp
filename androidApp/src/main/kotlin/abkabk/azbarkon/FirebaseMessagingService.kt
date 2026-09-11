package abkabk.azbarkon

import abkabk.azbarkon.core.notifications.canPostNotifications
import abkabk.azbarkon.core.notifications.ensureNotificationChannel
import abkabk.azbarkon.core.notifications.launchAppPendingIntent
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data[TITLE_KEY] ?: return
        val body = message.notification?.body ?: message.data[BODY_KEY].orEmpty()
        showNotification(title, body, message.messageId)
    }

    @SuppressLint("MissingPermission") // guarded by canPostNotifications()
    private fun showNotification(title: String, body: String, messageId: String?) {
        if (!canPostNotifications()) return

        ensureNotificationChannel(CHANNEL_ID, getString(R.string.app_name))

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(launchAppPendingIntent(NOTIFICATION_ID))
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this).notify(
            messageId?.hashCode() ?: NOTIFICATION_ID,
            notification,
        )
    }

    private companion object {
        const val CHANNEL_ID = "firebase_notification"
        const val NOTIFICATION_ID = 2001
        const val TITLE_KEY = "title"
        const val BODY_KEY = "body"
    }
}
