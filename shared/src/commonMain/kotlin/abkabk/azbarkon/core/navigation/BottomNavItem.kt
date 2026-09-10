package abkabk.azbarkon.core.navigation

import abkabk.azbarkon.features.games.navigation.GamesRoute
import abkabk.azbarkon.features.home.navigation.HomeRoute
import abkabk.azbarkon.features.poets.navigation.PoetsListRoute
import abkabk.azbarkon.features.profile.navigation.ProfileRoute
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.app_name
import sarv.shared.generated.resources.games
import sarv.shared.generated.resources.games_subtitle
import sarv.shared.generated.resources.home
import sarv.shared.generated.resources.treasure
import sarv.shared.generated.resources.profile
import sarv.shared.generated.resources.treasure_subtitle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

sealed class BottomNavItem(
    val route: KClass<*>,
    val title: StringResource,
    val icon: DrawableResource,
    val headerTitle: StringResource,
    val subtitle: StringResource? = null,
) {
    data object Home : BottomNavItem(
        route = HomeRoute::class,
        title = Res.string.home,
        icon = Res.drawable.home,
        headerTitle = Res.string.app_name,
    )

    data object Treasure : BottomNavItem(
        route = PoetsListRoute::class,
        title = Res.string.treasure,
        icon = Res.drawable.treasure,
        headerTitle = Res.string.treasure,
        subtitle = Res.string.treasure_subtitle
    )

    data object Games : BottomNavItem(
        route = GamesRoute::class,
        title = Res.string.games,
        icon = Res.drawable.games,
        headerTitle = Res.string.games,
        subtitle = Res.string.games_subtitle,
    )

    data object Profile : BottomNavItem(
        route = ProfileRoute::class,
        title = Res.string.profile,
        icon = Res.drawable.profile,
        headerTitle = Res.string.profile,
    )
}
