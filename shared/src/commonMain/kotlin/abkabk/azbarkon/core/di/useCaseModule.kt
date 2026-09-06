package abkabk.azbarkon.core.di

import abkabk.azbarkon.domain.usecase.ApplyGameHintUseCase
import abkabk.azbarkon.domain.usecase.BuildProfileStatsUseCase
import abkabk.azbarkon.domain.usecase.BuildShareTextUseCase
import abkabk.azbarkon.domain.usecase.EvaluateGameAnswerUseCase
import abkabk.azbarkon.domain.usecase.ExportUserDataUseCase
import abkabk.azbarkon.domain.usecase.GetMyPoemsUseCase
import abkabk.azbarkon.domain.usecase.GetRandomGhazalForPoetUseCase
import abkabk.azbarkon.domain.usecase.ImportUserDataUseCase
import abkabk.azbarkon.domain.usecase.StartMemorizationFromPoemUseCase
import org.koin.dsl.module

val useCaseModule =
    module {
        single { GetRandomGhazalForPoetUseCase(verseQueries = get()) }
        single { BuildShareTextUseCase() }
        single { StartMemorizationFromPoemUseCase(memorizationRepository = get()) }
        single { GetMyPoemsUseCase(poemRepository = get(), savedPoemRepository = get()) }
        single { BuildProfileStatsUseCase(memorizationRepository = get()) }
        single {
            ExportUserDataUseCase(
                userBackupManager = get(),
                shareService = get(),
            )
        }
        single {
            ImportUserDataUseCase(
                userBackupManager = get(),
                userPreferencesRepository = get(),
                dailyDistichNotificationScheduler = get(),
                memorizationReviewNotificationCoordinator = get(),
            )
        }
        single { EvaluateGameAnswerUseCase() }
        single { ApplyGameHintUseCase(userPreferencesRepository = get()) }
    }
