package abkabk.azbarkon.features.memorization.navigation

import abkabk.azbarkon.features.memorization.list.MemorizationListRoot
import abkabk.azbarkon.features.memorization.practice.MemorizationPracticeRoot
import abkabk.azbarkon.features.memorization.select.MemorizationSelectRoot
import abkabk.azbarkon.features.poets.navigation.PoemListRoute
import abkabk.azbarkon.features.poets.navigation.PoetDetailRoute
import abkabk.azbarkon.features.poets.navigation.PoetsListRoute
import abkabk.azbarkon.features.search.navigation.navigateToSearch
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

fun NavGraphBuilder.memorizationGraph(
    navController: NavController,
) {
    composable<MemorizationSelectRoute> {
        MemorizationSelectRoot(
            onBackClick = navController::navigateUp,
            onNavigateToPoetDetail = { poetId ->
                navController.navigate(PoetDetailRoute(poetId))
            },
            onNavigateToPoemList = { catId, title ->
                navController.navigate(PoemListRoute(catId = catId, title = title))
            },
            onNavigateToTreasury = {
                navController.navigate(PoetsListRoute)
            },
            onNavigateToSearch = {
                navController.navigateToSearch()
            },
            onNavigateToActivePoems = {
                navController.navigate(ActiveMemorizationRoute)
            },
        )
    }

    composable<ActiveMemorizationRoute> {
        MemorizationListRoot(
            onBackClick = navController::navigateUp,
            onNavigateToPractice = { poemId ->
                navController.navigate(MemorizationPracticeRoute(poemId = poemId))
            },
            onNavigateToSelect = {
                navController.navigate(MemorizationSelectRoute)
            },
        )
    }

    composable<MemorizationPracticeRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MemorizationPracticeRoute>()
        MemorizationPracticeRoot(
            poemId = route.poemId,
            onBackClick = navController::popPracticeToActiveMemorization,
            onNavigateToPoem = { poemId ->
                navController.navigate(MemorizationPracticeRoute(poemId = poemId)) {
                    popUpTo<MemorizationPracticeRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}

fun NavController.popPracticeToActiveMemorization() {
    navigate(ActiveMemorizationRoute) {
        popUpTo<MemorizationPracticeRoute> { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToMemorizationSelect() {
    navigate(MemorizationSelectRoute)
}

fun NavController.navigateToMemorizationPractice(poemId: Int? = null) {
    navigate(MemorizationPracticeRoute(poemId = poemId))
}

fun NavController.navigateToActiveMemorization() {
    navigate(ActiveMemorizationRoute)
}
