package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.SarvApp
import abkabk.azbarkon.core.di.initKoin
import abkabk.azbarkon.core.widget.RandomDistichWidgetRefresher
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.context.GlobalContext

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val application = context.applicationContext
        if (GlobalContext.getOrNull() == null && application is SarvApp) {
            initKoin(application)
        }

        val scheduler = GlobalContext.get().get<DailyDistichNotificationScheduler>()
        scheduler.rescheduleIfEnabled()

        val reviewScheduler = GlobalContext.get().get<MemorizationReviewNotificationScheduler>()
        reviewScheduler.rescheduleIfActive()

        GlobalContext.get().get<RandomDistichWidgetRefresher>().updateAllWidgetsAsync(context.applicationContext)
    }
}
