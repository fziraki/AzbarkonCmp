package abkabk.azbarkon.core.di

import abkabk.azbarkon.data.local.SqlDelightMemorizationLocalDataSource
import abkabk.azbarkon.data.repository.OfflineFirstMemorizationRepository
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.repository.MemorizationRepository
import com.azbarkon.memorization.SrsPoemQueries
import com.azbarkon.memorization.MemorizationDatabase
import com.azbarkon.memorization.ReviewLogQueries
import com.azbarkon.memorization.SrsPoemCardQueries
import org.koin.dsl.module

val memorizationDataModule =
    module {
        single<SrsPoemQueries> {
            get<MemorizationDatabase>().srsPoemQueries
        }

        single<SrsPoemCardQueries> {
            get<MemorizationDatabase>().srsPoemCardQueries
        }

        single<ReviewLogQueries> {
            get<MemorizationDatabase>().reviewLogQueries
        }

        single<MemorizationLocalDataSource> {
            SqlDelightMemorizationLocalDataSource(
                poemQueries = get(),
                cardQueries = get(),
                reviewLogQueries = get(),
                poetQueries = get(),
                catQueries = get(),
                database = get(),
            )
        }

        single<MemorizationRepository> {
            OfflineFirstMemorizationRepository(
                localDataSource = get(),
                poemRepository = get(),
                reviewNotificationCoordinator = get(),
            )
        }

        single {
            MemorizationReviewNotificationCoordinator(
                localDataSource = get(),
                scheduler = get(),
                userPreferencesRepository = get(),
            )
        }
    }
