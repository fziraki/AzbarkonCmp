package abkabk.azbarkon.core.di

import abkabk.azbarkon.data.platform.KtorAppUpdateRepository
import abkabk.azbarkon.domain.platform.AppUpdateRepository
import org.koin.dsl.module

val updateDataModule =
    module {
        single<AppUpdateRepository> { KtorAppUpdateRepository(httpClient = get()) }
    }
