package abkabk.azbarkon

import abkabk.azbarkon.core.di.initKoin
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.work.Configuration
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject
import org.koin.androidx.workmanager.factory.KoinWorkerFactory

class SarvApp :
    Application(),
    Configuration.Provider {
    private val dailyDistichNotificationScheduler: DailyDistichNotificationScheduler by inject()
    private val memorizationReviewNotificationScheduler: MemorizationReviewNotificationScheduler by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin(this)
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Napier.base(DebugAntilog())
        }
        dailyDistichNotificationScheduler.rescheduleIfEnabled()
        memorizationReviewNotificationScheduler.rescheduleIfActive()
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()
}
