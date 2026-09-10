package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.domain.repository.DailyDistichRepository
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DailyDistichWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters),
    KoinComponent {
    private val dailyDistichRepository: DailyDistichRepository by inject()
    private val notificationPresenter: DailyDistichNotificationPresenter by inject()

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        return when (val result = dailyDistichRepository.getTodayDistich()) {
            is abkabk.azbarkon.core.domain.result.Result.Success -> {
                notificationPresenter.show(result.data)
                androidx.work.ListenableWorker.Result.success()
            }
            is abkabk.azbarkon.core.domain.result.Result.Error -> {
                androidx.work.ListenableWorker.Result.failure()
            }
        }
    }
}
