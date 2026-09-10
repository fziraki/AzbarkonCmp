package abkabk.azbarkon

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.designsystem.sarvDimensions
import abkabk.azbarkon.core.navigation.SarvNavigation
import abkabk.azbarkon.core.ui.DeviceScaleInfo
import abkabk.azbarkon.core.ui.LocalDeviceScaleInfo
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.calculateWindowSizeClass
import abkabk.azbarkon.core.ui.createSarvImageLoader
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.compose.koinInject

@Composable
fun App(
    initialPoemId: Int? = null,
    openMemorizationPractice: Boolean = false,
) {
    setSingletonImageLoaderFactory { context ->
        createSarvImageLoader(context)
    }

    val userPreferencesRepository: UserPreferencesRepository = koinInject()
    val themeMode by userPreferencesRepository.observeThemeMode().collectAsStateWithLifecycle(
        initialValue = userPreferencesRepository.getThemeMode(),
    )
    val darkTheme =
        when (themeMode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }

    val fontSizeScale by userPreferencesRepository.observeFontSizeScale().collectAsStateWithLifecycle(
        initialValue = 1f,
    )

    val windowSizeClass = calculateWindowSizeClass()
    println("SarvDebug: WindowSizeClass = ${windowSizeClass.widthSizeClass}")

    val deviceScaleInfo = DeviceScaleInfo(
        widthSizeClass = windowSizeClass.widthSizeClass,
        widthDp = windowSizeClass.widthDp,
        heightDp = windowSizeClass.heightDp
    )

    val sarvDimensions = sarvDimensions(
        widthDp = windowSizeClass.widthDp,
        heightDp = windowSizeClass.heightDp
    )

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
        LocalDeviceScaleInfo provides deviceScaleInfo,
        LocalSarvDimensions provides sarvDimensions,
    ) {
        SarvTheme(darkTheme = darkTheme, fontSizeScale = fontSizeScale) {
            SarvNavigation(
                initialPoemId = initialPoemId,
                openMemorizationPractice = openMemorizationPractice,
            )
        }
    }
}
