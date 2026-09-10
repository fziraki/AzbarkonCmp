package abkabk.azbarkon.core.di

import abkabk.azbarkon.features.memorization.list.MemorizationListViewModel
import abkabk.azbarkon.features.memorization.practice.MemorizationPracticeViewModel
import abkabk.azbarkon.features.memorization.select.MemorizationSelectViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val memorizationPresentationModule =
    module {
        viewModelOf(::MemorizationSelectViewModel)
        viewModelOf(::MemorizationListViewModel)
        viewModel { parameters ->
            MemorizationPracticeViewModel(
                memorizationRepository = get(),
                poemId = parameters.getOrNull(),
            )
        }
    }
