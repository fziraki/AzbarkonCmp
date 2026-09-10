package abkabk.azbarkon.core.di

import abkabk.azbarkon.core.local.DatabaseDriverFactory
import abkabk.azbarkon.core.local.MemorizationDatabaseDriverFactory
import abkabk.azbarkon.core.local.PoetDbDriverFactory
import abkabk.azbarkon.core.local.PoetDbFileStorage
import abkabk.azbarkon.core.platform.ClipboardManager
import abkabk.azbarkon.core.platform.KeyValueStore
import abkabk.azbarkon.core.platform.ImageExportManager
import abkabk.azbarkon.core.platform.ShareManager
import abkabk.azbarkon.core.player.AudioPlayer
import abkabk.azbarkon.core.player.AvAudioPlayer
import abkabk.azbarkon.data.cache.CoilPoetImagePrefetcher
import abkabk.azbarkon.data.platform.IosDailyDistichNotificationScheduler
import abkabk.azbarkon.domain.datasource.PoetImagePrefetcher
import abkabk.azbarkon.data.platform.IosMemorizationReviewNotificationScheduler
import abkabk.azbarkon.data.platform.IosNotificationPermissionGateway
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.platform.MemorizationReviewNotificationScheduler
import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import com.sarv.db.SarvDatabase
import com.azbarkon.memorization.MemorizationDatabase
import coil3.PlatformContext
import org.koin.dsl.module

val iosPlatformModule =
    module {
        single {
            DatabaseDriverFactory()
        }

        single {
            PoetDbFileStorage()
        }

        single {
            PoetDbDriverFactory()
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
            MemorizationDatabaseDriverFactory()
        }

        single {
            MemorizationDatabase(
                driver = get<MemorizationDatabaseDriverFactory>().createDriver(),
            )
        }

        single {
            KeyValueStore()
        }

        single<PoetImagePrefetcher> {
            CoilPoetImagePrefetcher(platformContext = PlatformContext.INSTANCE)
        }

        single {
            ClipboardManager()
        }

        single {
            ShareManager()
        }

        single {
            ImageExportManager()
        }

        single<DailyDistichNotificationScheduler> {
            IosDailyDistichNotificationScheduler(
                dailyDistichRepository = get(),
                userPreferencesRepository = get(),
            )
        }

        single<MemorizationReviewNotificationScheduler> {
            IosMemorizationReviewNotificationScheduler(
                localDataSource = get(),
            )
        }

        single<NotificationPermissionGateway> {
            IosNotificationPermissionGateway()
        }

        factory<AudioPlayer> { AvAudioPlayer() }
    }
