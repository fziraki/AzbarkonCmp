package abkabk.azbarkon.core.di

import abkabk.azbarkon.data.local.SqlDelightDailyDistichLocalDataSource
import abkabk.azbarkon.data.repository.OfflineFirstDailyDistichRepository
import abkabk.azbarkon.domain.datasource.DailyDistichLocalDataSource
import abkabk.azbarkon.domain.repository.DailyDistichRepository
import org.koin.dsl.module

val dailyDistichDataModule =
    module {
        single<DailyDistichLocalDataSource> {
            SqlDelightDailyDistichLocalDataSource(
                verseQueries = get(),
            )
        }
        single<DailyDistichRepository> {
            OfflineFirstDailyDistichRepository(
                localDataSource = get(),
            )
        }
    }
