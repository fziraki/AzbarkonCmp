package abkabk.azbarkon.core.di

import abkabk.azbarkon.features.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profilePresentationModule =
    module {
        viewModel {
            ProfileViewModel(
                memorizationRepository = get(),
                userPreferencesRepository = get(),
                dailyDistichNotificationScheduler = get(),
                notificationPermissionGateway = get(),
                memorizationReviewNotificationCoordinator = get(),
                buildProfileStats = get(),
                exportUserData = get(),
                importUserData = get(),
            )
        }
    }
