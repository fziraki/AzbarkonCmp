package abkabk.azbarkon

import abkabk.azbarkon.core.di.initKoinIfNeeded
import abkabk.azbarkon.core.notifications.IosNotificationDelegate
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object IosAppBootstrap : KoinComponent {
    private val dailyDistichNotificationScheduler: DailyDistichNotificationScheduler by inject()
    private val reviewNotificationCoordinator: MemorizationReviewNotificationCoordinator by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun onLaunch() {
        initKoinIfNeeded()
        IosNotificationDelegate.install()
        dailyDistichNotificationScheduler.rescheduleIfEnabled()
        scope.launch {
            reviewNotificationCoordinator.sync()
        }
    }
}

fun MainViewController() =
    ComposeUIViewController {
        IosAppBootstrap.onLaunch()
        val poemId by IosNotificationDelegate.poemId.collectAsState()
        val openMemorizationPractice by IosNotificationDelegate.openMemorizationPractice.collectAsState()
        App(
            initialPoemId = poemId,
            openMemorizationPractice = openMemorizationPractice,
        )
    }
