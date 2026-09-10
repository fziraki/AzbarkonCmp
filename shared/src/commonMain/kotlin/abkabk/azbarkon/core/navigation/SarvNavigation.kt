package abkabk.azbarkon.core.navigation

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import abkabk.azbarkon.core.uidata.LocalSarvAppState
import abkabk.azbarkon.core.uidata.LocalSnackbarHostState
import abkabk.azbarkon.core.uidata.SarvAppState
import abkabk.azbarkon.core.uidata.rememberSarvAppState
import abkabk.azbarkon.features.games.navigation.GamePlayRoute
import abkabk.azbarkon.features.games.navigation.GameTypeRoute
import abkabk.azbarkon.features.games.navigation.GamesRoute
import abkabk.azbarkon.features.games.navigation.gamesGraph
import abkabk.azbarkon.features.games.navigation.navigateToGame
import abkabk.azbarkon.features.home.HomeCallbacks
import abkabk.azbarkon.features.home.navigation.HomeRoute
import abkabk.azbarkon.features.home.navigation.MyPoemsRoute
import abkabk.azbarkon.features.home.navigation.homeGraph
import abkabk.azbarkon.features.memorization.navigation.MemorizationPracticeRoute
import abkabk.azbarkon.features.memorization.navigation.memorizationGraph
import abkabk.azbarkon.features.memorization.navigation.navigateToActiveMemorization
import abkabk.azbarkon.features.memorization.navigation.navigateToMemorizationPractice
import abkabk.azbarkon.features.memorization.navigation.navigateToMemorizationSelect
import abkabk.azbarkon.features.poets.navigation.ChatRoute
import abkabk.azbarkon.features.poets.navigation.PoemDetailRoute
import abkabk.azbarkon.features.poets.navigation.PoetDetailRoute
import abkabk.azbarkon.features.poets.navigation.PoetsListRoute
import abkabk.azbarkon.features.poets.navigation.poetsGraph
import abkabk.azbarkon.features.profile.navigation.ProfileRoute
import abkabk.azbarkon.features.profile.navigation.profileGraph
import abkabk.azbarkon.features.search.navigation.navigateToSearch
import abkabk.azbarkon.features.search.navigation.searchGraph
import abkabk.azbarkon.features.tasvirNegar.navigation.TasvirNegarRoute
import abkabk.azbarkon.features.tasvirNegar.navigation.tasvirNegarGraph
import abkabk.azbarkon.ui.components.SarvSnackbarHost
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.Shekasteh
import sarv.shared.generated.resources.app_name
import sarv.shared.generated.resources.arrow_back_right
import sarv.shared.generated.resources.cd_back
import sarv.shared.generated.resources.cd_search
import sarv.shared.generated.resources.cd_settings
import sarv.shared.generated.resources.search
import sarv.shared.generated.resources.settings

// ponytail: null destination = start destination not yet resolved; treat as a root tab
// so top/bottom bars render on the first frame and the content never reflows.
private fun NavDestination?.isRootTabDestination(): Boolean =
    this == null ||
        hasRoute<HomeRoute>() ||
        hasRoute<PoetsListRoute>() ||
        hasRoute<GamesRoute>() ||
        hasRoute<ProfileRoute>()

private val bottomNavItems =
    listOf(
        BottomNavItem.Home,
        BottomNavItem.Treasure,
        BottomNavItem.Games,
        BottomNavItem.Profile,
    )

// Screens with their own Scaffold + bottomBar render the snackbar host themselves,
// so the root host must not duplicate it there.
private fun NavDestination?.hasOwnScaffold(): Boolean =
    this != null &&
        (hasRoute<PoemDetailRoute>() ||
            hasRoute<TasvirNegarRoute>() ||
            hasRoute<ChatRoute>() ||
            hasRoute<MemorizationPracticeRoute>() ||
            hasRoute<GamePlayRoute>())

@Composable
private fun SarvTopBar(
    currentItem: BottomNavItem?,
    currentDestination: NavDestination?,
    navController: NavController,
    appState: SarvAppState,
    isExpandedScreen: Boolean,
) {
    Box(
        modifier =
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .heightIn(min = if (isExpandedScreen) LocalSarvDimensions.current.dimen40 else LocalSarvDimensions.current.dimen56),
    ) {
        if (navController.previousBackStackEntry != null) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { navController.navigateUp() },
            ) {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.arrow_back_right),
                    contentDescription = stringResource(Res.string.cd_back),
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            currentItem?.headerTitle?.let {

                if (it == Res.string.app_name){
                    Text(
                        text = stringResource(it),
                        fontFamily = FontFamily(Font(Res.font.Shekasteh)),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }else{
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

            }

            currentItem?.subtitle?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (currentDestination?.hasRoute<ProfileRoute>() == true) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { appState.onProfileSettingsClick?.invoke() },
            ) {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.settings),
                    contentDescription = stringResource(Res.string.cd_settings),
                )
            }
        }

        if (currentDestination?.hasRoute<PoetsListRoute>() == true) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { navController.navigateToSearch() },
            ) {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.search),
                    contentDescription = stringResource(Res.string.cd_search),
                )
            }
        }
    }
}

@Composable
private fun SarvBottomBar(
    currentDestination: NavDestination?,
    navController: NavController,
) {
    NavigationBar(
        modifier =
            Modifier
                .shadow(spotColor = MaterialTheme.colorScheme.tertiary, elevation = LocalSarvDimensions.current.dimen1)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth()
                .heightIn(min = LocalSarvDimensions.current.dimen64),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        bottomNavItems.forEach { item ->
            val selected =
                when (item) {
                    BottomNavItem.Home -> currentDestination?.hasRoute<HomeRoute>() == true
                    BottomNavItem.Treasure -> currentDestination?.hasRoute<PoetsListRoute>() == true
                    BottomNavItem.Games -> currentDestination?.hasRoute<GamesRoute>() == true
                    BottomNavItem.Profile -> currentDestination?.hasRoute<ProfileRoute>() == true
                }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    val route =
                        when (item) {
                            BottomNavItem.Home -> HomeRoute
                            BottomNavItem.Treasure -> PoetsListRoute
                            BottomNavItem.Games -> GamesRoute
                            BottomNavItem.Profile -> ProfileRoute
                        }
                    if (item == BottomNavItem.Home) {
                        navController.navigate(HomeRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier.size(LocalSarvDimensions.current.dimen22),
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.title),
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent,
                    ),
                label = {
                    Text(
                        text = stringResource(item.title),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = false,
            )
        }
    }
}

@Composable
private fun SarvNavigationRail(
    currentDestination: NavDestination?,
    navController: NavController,
) {
    NavigationRail(
        modifier = Modifier.heightIn(min = LocalSarvDimensions.current.dimen64),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                space = LocalSarvDimensions.current.dimen16,
                alignment = Alignment.CenterVertically
            ),
        ) {
            bottomNavItems.forEach { item ->
                val selected =
                    when (item) {
                        BottomNavItem.Home -> currentDestination?.hasRoute<HomeRoute>() == true
                        BottomNavItem.Treasure -> currentDestination?.hasRoute<PoetsListRoute>() == true
                        BottomNavItem.Games -> currentDestination?.hasRoute<GamesRoute>() == true
                        BottomNavItem.Profile -> currentDestination?.hasRoute<ProfileRoute>() == true
                    }

                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        val route =
                            when (item) {
                                BottomNavItem.Home -> HomeRoute
                                BottomNavItem.Treasure -> PoetsListRoute
                                BottomNavItem.Games -> GamesRoute
                                BottomNavItem.Profile -> ProfileRoute
                            }
                        if (item == BottomNavItem.Home) {
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(LocalSarvDimensions.current.dimen22),
                            painter = painterResource(item.icon),
                            contentDescription = stringResource(item.title),
                        )
                    },
                    colors =
                        NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.secondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    label = {
                        Text(
                            text = stringResource(item.title),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

private fun homeCallbacks(navController: NavController): HomeCallbacks =
    HomeCallbacks(
        onNavigateToPoetsList = {
            navController.navigate(PoetsListRoute) {
                popUpTo(HomeRoute) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onNavigateToPoetDetail = { poetId ->
            navController.navigate(PoetDetailRoute(poetId))
        },
        onNavigateToPoemDetail = { poemId ->
            navController.navigate(PoemDetailRoute(poemId = poemId))
        },
        onNavigateToMyPoems = {
            navController.navigate(MyPoemsRoute)
        },
        onNavigateToSearch = {
            navController.navigateToSearch()
        },
        onNavigateToTasvirNegar = {
            navController.navigate(TasvirNegarRoute(poemId = null))
        },
        onNavigateToMemorizationSelect = {
            navController.navigateToMemorizationSelect()
        },
        onNavigateToMemorizationPractice = {
            navController.navigateToMemorizationPractice()
        },
        onNavigateToActiveMemorization = {
            navController.navigateToActiveMemorization()
        },
        onNavigateToGame = {
            navController.navigateToGame(GameTypeRoute.NEXT_VERSE)
        },
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SarvNavigation(
    modifier: Modifier = Modifier,
    initialPoemId: Int? = null,
    openMemorizationPractice: Boolean = false,
) {
    val navController = rememberNavController()
    val appState = rememberSarvAppState()
    val snackbarHostState = remember { SnackbarHostState() }
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpandedScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    LaunchedEffect(initialPoemId) {
        initialPoemId?.let { poemId ->
            navController.navigate(PoemDetailRoute(poemId = poemId)) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(openMemorizationPractice) {
        if (openMemorizationPractice) {
            navController.navigateToMemorizationPractice(poemId = null)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isRootDestination = currentDestination.isRootTabDestination()

    val currentItem =
        bottomNavItems.find { item ->
            when (item) {
                BottomNavItem.Home -> currentDestination?.hasRoute<HomeRoute>() == true
                BottomNavItem.Treasure -> currentDestination?.hasRoute<PoetsListRoute>() == true
                BottomNavItem.Games -> currentDestination?.hasRoute<GamesRoute>() == true
                BottomNavItem.Profile -> currentDestination?.hasRoute<ProfileRoute>() == true
            }
        } ?: BottomNavItem.Home

    CompositionLocalProvider(
        LocalSarvAppState provides appState,
        LocalSnackbarHostState provides snackbarHostState,
    ) {
        Row(modifier = modifier) {
            if (isExpandedScreen && isRootDestination) {
                SarvNavigationRail(
                    currentDestination = currentDestination,
                    navController = navController,
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = {
                    if (!currentDestination.hasOwnScaffold()) SarvSnackbarHost(hostState = snackbarHostState)
                },
                topBar = {
                    if (isRootDestination) {
                        SarvTopBar(
                            currentItem = currentItem,
                            currentDestination = currentDestination,
                            navController = navController,
                            appState = appState,
                            isExpandedScreen = isExpandedScreen,
                        )
                    }
                },
                bottomBar = {
                    if (isRootDestination && !isExpandedScreen) {
                        SarvBottomBar(
                            currentDestination = currentDestination,
                            navController = navController,
                        )
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.padding(padding),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                ) {
                    homeGraph(
                        callbacks = homeCallbacks(navController),
                        onBackFromMyPoems = navController::navigateUp,
                        onNavigateToPoemDetailFromMyPoems = { poemId ->
                            navController.navigate(PoemDetailRoute(poemId = poemId))
                        },
                    )
                    tasvirNegarGraph(onBackClick = navController::navigateUp)
                    memorizationGraph(navController)
                    gamesGraph(navController)
                    profileGraph()
                    poetsGraph(navController)
                    searchGraph(navController)
        }
        }
    }
}
    }
