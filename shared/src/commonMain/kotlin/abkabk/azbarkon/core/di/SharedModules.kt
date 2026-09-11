package abkabk.azbarkon.core.di

import org.koin.core.module.Module

val sharedModules: List<Module> =
    listOf(
        networkModule,
        databaseModule,
        poetsDataModule,
        poemDataModule,
        dailyDistichDataModule,
        searchDataModule,
        savedPoemDataModule,
        chatDataModule,
        userDataModule,
        userPreferencesDataModule,
        memorizationDataModule,
        gamesDataModule,
        updateDataModule,
        useCaseModule,
        homePresentationModule,
        profilePresentationModule,
        poetsPresentationModule,
        searchPresentationModule,
        chatPresentationModule,
        tasvirNegarPresentationModule,
        memorizationPresentationModule,
        gamesPresentationModule
    )
