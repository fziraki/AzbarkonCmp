package abkabk.azbarkon.core.di

import abkabk.azbarkon.core.local.DatabaseDriverFactory
import abkabk.azbarkon.core.local.MemorizationDatabaseDriverFactory
import abkabk.azbarkon.core.local.PoetDbDriverFactory
import abkabk.azbarkon.core.local.PoetDbFileStorage
import abkabk.azbarkon.core.notifications.DailyDistichNotificationPresenter
import abkabk.azbarkon.core.notifications.DailyDistichWorker
import abkabk.azbarkon.core.notifications.MemorizationReviewNotificationPresenter
import abkabk.azbarkon.core.notifications.MemorizationReviewWorker
import abkabk.azbarkon.core.widget.RandomDistichWidgetPreferences
import abkabk.azbarkon.core.widget.RandomDistichWidgetRefresher
import abkabk.azbarkon.core.widget.RandomDistichWidgetUpdater
import abkabk.azbarkon.core.platform.ClipboardManager
import abkabk.azbarkon.core.platform.KeyValueStore
import abkabk.azbarkon.core.platform.ImageExportManager
import abkabk.azbarkon.core.platform.ShareManager
import abkabk.azbarkon.core.player.AudioPlayer
import abkabk.azbarkon.core.player.Media3AudioPlayer
import abkabk.azbarkon.data.platform.AndroidDailyDistichNotificationScheduler
import abkabk.azbarkon.data.platform.AndroidMemorizationReviewNotificationScheduler
import abkabk.azbarkon.data.platform.AndroidNotificationPermissionGateway
import abkabk.azbarkon.data.cache.CoilPoetImagePrefetcher
import abkabk.azbarkon.domain.datasource.PoetImagePrefetcher
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import androidx.media3.exoplayer.ExoPlayer
import com.sarv.db.SarvDatabase
import com.azbarkon.memorization.MemorizationDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val androidPlatformModule =
    module {
        @Suppress("InjectDispatcher")
        single<CoroutineDispatcher> { Dispatchers.IO }

        single {
            DatabaseDriverFactory(
                context = androidContext(),
            )
        }

        single {
            PoetDbFileStorage(context = androidContext())
        }

        single {
            PoetDbDriverFactory(context = androidContext())
        }

        single<app.cash.sqldelight.db.SqlDriver> {
            get<DatabaseDriverFactory>().createDriver()
        }

        single {
            SarvDatabase(
                driver = get(),
            )
        }

        single {
            MemorizationDatabaseDriverFactory(
                context = androidContext(),
            )
        }

        single {
            MemorizationDatabase(
                driver = get<MemorizationDatabaseDriverFactory>().createDriver(),
            )
        }

        single {
            KeyValueStore(context = androidContext())
        }

        single<PoetImagePrefetcher> {
            CoilPoetImagePrefetcher(platformContext = androidContext())
        }

        single {
            ClipboardManager(context = androidContext())
        }

        single {
            ShareManager(context = androidContext())
        }

        single {
            ImageExportManager(context = androidContext())
        }

        single<DailyDistichNotificationScheduler> {
            AndroidDailyDistichNotificationScheduler(
                context = androidContext(),
                userPreferencesRepository = get(),
                dailyDistichRepository = get(),
                notificationPresenter = get(),
                ioDispatcher = get(),
            )
        }

        single<NotificationPermissionGateway> {
            AndroidNotificationPermissionGateway(
                context = androidContext(),
            )
        }

        single<MemorizationReviewNotificationScheduler> {
            AndroidMemorizationReviewNotificationScheduler(
                context = androidContext(),
                localDataSource = get(),
            )
        }

        single {
            MemorizationReviewNotificationPresenter(
                context = androidContext(),
            )
        }

        single {
            DailyDistichNotificationPresenter(
                context = androidContext(),
            )
        }

        single {
            RandomDistichWidgetPreferences(
                context = androidContext(),
            )
        }

        single {
            RandomDistichWidgetUpdater()
        }

        single {
            RandomDistichWidgetRefresher()
        }

        workerOf(::DailyDistichWorker)
        workerOf(::MemorizationReviewWorker)

        factory<AudioPlayer> {
            Media3AudioPlayer(ExoPlayer.Builder(androidContext()).build())
        }
    }
