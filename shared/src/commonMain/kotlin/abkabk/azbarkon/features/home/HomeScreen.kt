package abkabk.azbarkon.features.home

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.designsystem.brown
import abkabk.azbarkon.core.notifications.MAX_NOTIFICATION_PERMISSION_DECLINES
import abkabk.azbarkon.core.notifications.NotificationPermissionSheet
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSarvAppState
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.domain.model.Poet
import abkabk.azbarkon.domain.model.RandomDistich
import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import abkabk.azbarkon.ui.components.NetworkImage
import abkabk.azbarkon.ui.components.SarvButton
import abkabk.azbarkon.ui.theme.LightColorScheme
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.all
import sarv.shared.generated.resources.continue_memorization_desc
import sarv.shared.generated.resources.continue_memorization_title
import sarv.shared.generated.resources.herobg
import sarv.shared.generated.resources.image_creator_bg
import sarv.shared.generated.resources.memorization_button
import sarv.shared.generated.resources.my_poems
import sarv.shared.generated.resources.new_memorization_button
import sarv.shared.generated.resources.new_memorization_desc
import sarv.shared.generated.resources.new_memorization_title
import sarv.shared.generated.resources.newsstand
import sarv.shared.generated.resources.next_verse_game_bg
import sarv.shared.generated.resources.palette
import sarv.shared.generated.resources.pic_negar
import sarv.shared.generated.resources.poetry_memorization
import sarv.shared.generated.resources.popular_poets
import sarv.shared.generated.resources.review
import sarv.shared.generated.resources.search
import sarv.shared.generated.resources.slider_distich_of_day_poet
import sarv.shared.generated.resources.slider_distich_of_day_text
import sarv.shared.generated.resources.slider_distich_of_day_title
import sarv.shared.generated.resources.slider_challenge_button
import sarv.shared.generated.resources.slider_challenge_text
import sarv.shared.generated.resources.slider_challenge_title
import sarv.shared.generated.resources.slider_tasvir_negar_button
import sarv.shared.generated.resources.slider_tasvir_negar_text
import sarv.shared.generated.resources.slider_tasvir_negar_title
import sarv.shared.generated.resources.today_distich_bg
import sarv.shared.generated.resources.unknown

private const val SLIDER_TOP_WEIGHT = 0.4f
private const val SLIDER_CONTENT_WEIGHT = 0.6f
private const val TOP_SLIDER_HEIGHT_FRACTION = 0.25f
private const val TOP_SLIDER_HEIGHT_FRACTION_EXPAND = 0.3f
private const val BEYT_SLIDE_SPACER_WEIGHT_EXPANDED = 0.2f
private const val HERO_CARD_BUTTON_WIDTH_FRACTION = 0.5f

data class HomeCallbacks(
    val onNavigateToPoetsList: () -> Unit,
    val onNavigateToPoetDetail: (Int) -> Unit,
    val onNavigateToPoemDetail: (Int) -> Unit,
    val onNavigateToMyPoems: () -> Unit,
    val onNavigateToSearch: () -> Unit,
    val onNavigateToTasvirNegar: () -> Unit,
    val onNavigateToMemorizationSelect: () -> Unit,
    val onNavigateToMemorizationPractice: () -> Unit,
    val onNavigateToActiveMemorization: () -> Unit,
    val onNavigateToGame: () -> Unit = {},
)

@Composable
fun HomeRoot(
    callbacks: HomeCallbacks,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appState = LocalSarvAppState.current
    val permissionGateway: NotificationPermissionGateway = koinInject()
    val userPreferencesRepository: UserPreferencesRepository = koinInject()
    var showNotificationPermissionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!appState.notificationPermissionSheetShownThisLaunch &&
            !permissionGateway.areNotificationsEnabled() &&
            userPreferencesRepository.getNotificationPermissionDeclineCount() < MAX_NOTIFICATION_PERMISSION_DECLINES
        ) {
            appState.notificationPermissionSheetShownThisLaunch = true
            showNotificationPermissionSheet = true
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToPoetsList -> callbacks.onNavigateToPoetsList()

            is HomeEvent.NavigateToPoetDetail -> callbacks.onNavigateToPoetDetail(event.poetId)

            is HomeEvent.NavigateToPoemDetail -> callbacks.onNavigateToPoemDetail(event.poemId)

            HomeEvent.NavigateToMyPoems -> callbacks.onNavigateToMyPoems()

            HomeEvent.NavigateToSearch -> callbacks.onNavigateToSearch()

            HomeEvent.NavigateToTasvirNegar -> callbacks.onNavigateToTasvirNegar()

            HomeEvent.NavigateToMemorizationSelect -> callbacks.onNavigateToMemorizationSelect()

            HomeEvent.NavigateToMemorizationPractice -> callbacks.onNavigateToMemorizationPractice()

            HomeEvent.NavigateToActiveMemorization -> callbacks.onNavigateToActiveMemorization()

            HomeEvent.NavigateToGame -> callbacks.onNavigateToGame()
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        HomeScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    }

    if (showNotificationPermissionSheet) {
        NotificationPermissionSheet(
            onDismiss = { showNotificationPermissionSheet = false },
            onResult = { showNotificationPermissionSheet = false },
        )
    }
}

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpandedScreen =
        LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val topSliderHeight = if (!isExpandedScreen){
            maxHeight * TOP_SLIDER_HEIGHT_FRACTION
        }else{
            maxHeight * TOP_SLIDER_HEIGHT_FRACTION_EXPAND
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = if (!isExpandedScreen) {
                    LocalSarvDimensions.current.dimen12
                } else {
                    LocalSarvDimensions.current.dimen1
                }
            ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12)
        ) {
            item {
                TopSlider(
                    items =
                        listOf(
                            SliderPage.DistichOfDay,
                            SliderPage.Challenge,
                            SliderPage.TasvirNegar,
                        ),
                    modifier = Modifier.height(topSliderHeight),
                    todayDistich = state.todayDistich,
                    isExpandedScreen = isExpandedScreen,
                    onTasvirNegarClick = { onAction(HomeAction.OnTasvirNegarClick) },
                    onChallengeClick = { onAction(HomeAction.OnChallengeClick) },
                onDistichOfDayClick = { onAction(HomeAction.OnDistichOfDayClick) },
            )
        }
        item {
            if (isExpandedScreen) {

                var maxHeight by remember { mutableIntStateOf(0) }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalSarvDimensions.current.dimen16),
                ) {

                    QuickAccessMenu(
                        onMyPoemsClick = { onAction(HomeAction.OnMyPoemsClick) },
                        onSearchClick = { onAction(HomeAction.OnSearchClick) },
                        onTasvirNegarClick = { onAction(HomeAction.OnTasvirNegarClick) },
                        onReviewClick = { onAction(HomeAction.OnReviewClick) },
                        isExpanded = true,
                        modifier = Modifier.weight(1f)
                            .onGloballyPositioned { maxHeight = it.size.height },
                    )
                    HeroCard(
                        hero = state.memorizationHero,
                        onClick = { onAction(HomeAction.OnMemorizationClick) },
                        modifier = Modifier.weight(1f)
                            .height(with(LocalDensity.current) { maxHeight.toDp().coerceAtLeast(0.dp) }),
                        isExpandedScreen = isExpandedScreen
                    )
                }
            } else {
                HeroCard(
                    hero = state.memorizationHero,
                    onClick = { onAction(HomeAction.OnMemorizationClick) },
                    isExpandedScreen = isExpandedScreen
                )
            }
        }
        if (!isExpandedScreen) {
            item {
                QuickAccessMenu(
                    onMyPoemsClick = { onAction(HomeAction.OnMyPoemsClick) },
                    onSearchClick = { onAction(HomeAction.OnSearchClick) },
                    onTasvirNegarClick = { onAction(HomeAction.OnTasvirNegarClick) },
                    onReviewClick = { onAction(HomeAction.OnReviewClick) },
                )
            }
        }
        item {
            Poets(
                poets = state.poets,
                onSeeAllClick = { onAction(HomeAction.OnSeeAllPoetsClick) },
                onPoetClick = { poetId -> onAction(HomeAction.OnPoetClick(poetId)) },
            )
        }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    SarvTheme {
        HomeScreen(
            state = HomeState(),
            onAction = {},
        )
    }
}

@Composable
fun Poets(
    poets: List<Poet>,
    onSeeAllClick: () -> Unit,
    onPoetClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LocalSarvDimensions.current.dimen16),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = LocalSarvDimensions.current.dimen8),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.popular_poets),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                modifier = Modifier.clickable(onClick = onSeeAllClick),
                text = stringResource(Res.string.all),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            items(
                items = poets,
                key = { poet -> poet.id ?: poet.name.orEmpty() },
            ) { poet ->
                PoetItem(
                    item = poet,
                    onClick = {
                        poet.id?.let(onPoetClick)
                    },
                )
            }
        }
    }
}

@Composable
fun PoetItem(
    item: Poet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(LocalSarvDimensions.current.dimen80)
                .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (item.imageUrl != null) {
            NetworkImage(
                modifier =
                    Modifier
                        .size(LocalSarvDimensions.current.dimen80)
                        .clip(CircleShape),
                imageUrl = item.imageUrl,
            )
        } else {
            Image(
                modifier =
                    Modifier
                        .size(LocalSarvDimensions.current.dimen80)
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.primary),
                painter = painterResource(Res.drawable.unknown),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.surface)
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            text = item.name ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun QuickAccessMenu(
    onMyPoemsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTasvirNegarClick: () -> Unit,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val spacedBy = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12)

    if (isExpanded) {
        Column(
            modifier = modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = LocalSarvDimensions.current.dimen16),
            verticalArrangement = spacedBy,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = spacedBy,
            ) {
                QuickAccessItem(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.newsstand,
                    title = Res.string.my_poems,
                    onItemClick = onMyPoemsClick,
                )
                QuickAccessItem(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.search,
                    title = Res.string.search,
                    onItemClick = onSearchClick,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = spacedBy,
            ) {
                QuickAccessItem(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.palette,
                    title = Res.string.pic_negar,
                    onItemClick = onTasvirNegarClick,
                )
                QuickAccessItem(
                    modifier = Modifier.weight(1f),
                    icon = Res.drawable.review,
                    title = Res.string.review,
                    onItemClick = onReviewClick,
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = LocalSarvDimensions.current.dimen16),
            horizontalArrangement = spacedBy,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickAccessItem(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.newsstand,
                title = Res.string.my_poems,
                onItemClick = onMyPoemsClick,
            )
            QuickAccessItem(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.search,
                title = Res.string.search,
                onItemClick = onSearchClick,
            )
            QuickAccessItem(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.palette,
                title = Res.string.pic_negar,
                onItemClick = onTasvirNegarClick,
            )
            QuickAccessItem(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.review,
                title = Res.string.review,
                onItemClick = onReviewClick,
            )
        }
    }
}

@Composable
fun QuickAccessItem(
    icon: DrawableResource,
    title: StringResource,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Surface(
        modifier = modifier.clickable { onItemClick() },
        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen24),
        tonalElevation = LocalSarvDimensions.current.dimen1,
        shadowElevation = LocalSarvDimensions.current.dimen1,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Column(
            modifier = Modifier.padding(vertical = LocalSarvDimensions.current.dimen20, horizontal = LocalSarvDimensions.current.dimen4),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )

            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun HeroCard(
    hero: MemorizationHeroUi,
    onClick: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val titleText =
        if (hero.hasActivePoems) {
            stringResource(Res.string.continue_memorization_title)
        } else {
            stringResource(Res.string.new_memorization_title)
        }
    val descText =
        if (hero.hasActivePoems) {
            stringResource(
                Res.string.continue_memorization_desc,
                hero.activePoemCount,
                hero.dueCardsToday,
            )
        } else {
            stringResource(Res.string.new_memorization_desc)
        }
    val buttonText =
        if (hero.hasActivePoems) {
            stringResource(Res.string.memorization_button)
        } else {
            stringResource(Res.string.new_memorization_button)
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LocalSarvDimensions.current.dimen16),
    ) {
        Image(
            painter = painterResource(Res.drawable.herobg),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier =
                Modifier
                    .padding(LocalSarvDimensions.current.dimen16)
                    .fillMaxSize(),
            verticalArrangement = if (isExpandedScreen){
                Arrangement.SpaceBetween
            }else {
                Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)
            },
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = stringResource(Res.string.poetry_memorization),
                style = MaterialTheme.typography.bodySmall,
                color = LightColorScheme.onSecondary,
            )
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = LightColorScheme.surfaceVariant,
            )
            Text(
                text = descText,
                style = MaterialTheme.typography.labelSmall,
                color = LightColorScheme.onSecondary,
            )
            SarvButton(
                text = buttonText,
                onClick = onClick,
                colors =
                    ButtonColors(
                        containerColor = brown,
                        contentColor = LightColorScheme.onPrimary,
                        disabledContainerColor = brown,
                        disabledContentColor = LightColorScheme.onPrimary,
                    ),
                modifier = Modifier.fillMaxWidth(HERO_CARD_BUTTON_WIDTH_FRACTION),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSlider(
    items: List<SliderPage>,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    autoPlayDuration: Long = 4000L,
    todayDistich: RandomDistich? = null,
    onTasvirNegarClick: () -> Unit = {},
    onChallengeClick: () -> Unit = {},
    onDistichOfDayClick: () -> Unit = {}
) {
    if (items.isEmpty()) return

    val pagerState =
        rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            pageCount = { Int.MAX_VALUE },
        )

    fun getItem(page: Int): SliderPage = items[page % items.size]

    // 🎬 AUTOPLAY (simple)
    LaunchedEffect(items.size) {
        while (true) {
            delay(autoPlayDuration)

            pagerState.animateScrollToPage(
                pagerState.currentPage + 1,
                animationSpec =
                    spring(
                        dampingRatio = 0.9f,
                        stiffness = 300f,
                    ),
            )
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = LocalSarvDimensions.current.dimen16,
            contentPadding = PaddingValues(horizontal = LocalSarvDimensions.current.dimen32),
            beyondViewportPageCount = 1,
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) { page ->

            val item = getItem(page)

            when (item) {
                is SliderPage.DistichOfDay -> DistichOfDaySlide(
                    distich = todayDistich,
                    isExpandedScreen = isExpandedScreen,
                    onClick = onDistichOfDayClick,
                )

                is SliderPage.Challenge -> ChallengeSlide(isExpandedScreen = isExpandedScreen, onClick = onChallengeClick)

                is SliderPage.TasvirNegar -> TasvirNegarSlide(isExpandedScreen = isExpandedScreen, onClick = onTasvirNegarClick)
            }
        }

        Spacer(modifier = Modifier.height(LocalSarvDimensions.current.dimen10))

        // 🎯 Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            val currentIndex = pagerState.currentPage % items.size

            repeat(items.size) { index ->
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = LocalSarvDimensions.current.dimen4)
                            .size(LocalSarvDimensions.current.dimen6)
                            .clip(CircleShape)
                            .background(
                                if (currentIndex == index) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                )
            }
        }
    }
}

@Composable
fun TasvirNegarSlide(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Box(modifier = modifier.fillMaxSize()) {

        Image(
            painter = painterResource(Res.drawable.image_creator_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (isExpandedScreen){
                        Modifier.padding(
                            vertical = LocalSarvDimensions.current.dimen8,
                            horizontal = LocalSarvDimensions.current.dimen16
                        )
                    }else{
                        Modifier.padding(LocalSarvDimensions.current.dimen16)
                    }),
        ) {

            Spacer(modifier = Modifier.weight(SLIDER_TOP_WEIGHT))


            Column(
                modifier = Modifier.weight(SLIDER_CONTENT_WEIGHT).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.slider_tasvir_negar_title),
                    color = LightColorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isExpandedScreen){
                        stringResource(Res.string.slider_tasvir_negar_text).replace("\n"," ")
                    }else{
                        stringResource(Res.string.slider_tasvir_negar_text)
                    },
                    color = LightColorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                )

                SarvButton(
                    text = stringResource(Res.string.slider_tasvir_negar_button),
                    onClick = onClick,
                    modifier = Modifier.wrapContentSize(),
                    textStyle = MaterialTheme.typography.labelSmall,
                    colors =
                        ButtonColors(
                            containerColor = LightColorScheme.secondary,
                            contentColor = LightColorScheme.onSecondary,
                            disabledContainerColor = LightColorScheme.secondary,
                            disabledContentColor = LightColorScheme.onSecondary,
                        ),
                )
            }

        }
    }
}

@Composable
fun ChallengeSlide(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {

        Image(
            painter = painterResource(Res.drawable.next_verse_game_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (isExpandedScreen){
                        Modifier.padding(
                            vertical = LocalSarvDimensions.current.dimen8,
                            horizontal = LocalSarvDimensions.current.dimen16
                        )
                    }else{
                        Modifier.padding(LocalSarvDimensions.current.dimen16)
                    }),
        ) {

            Spacer(modifier = Modifier.weight(SLIDER_TOP_WEIGHT))

            Column(
                modifier = Modifier.weight(SLIDER_CONTENT_WEIGHT).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.slider_challenge_title),
                    color = LightColorScheme.tertiary,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isExpandedScreen){
                        stringResource(Res.string.slider_challenge_text).replace("\n"," ")
                    }else{stringResource(Res.string.slider_challenge_text)},
                    color = LightColorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                )

                SarvButton(
                    text = stringResource(Res.string.slider_challenge_button),
                    onClick = onClick,
                    modifier = Modifier.wrapContentSize(),
                    textStyle = MaterialTheme.typography.labelSmall,
                    colors =
                        ButtonColors(
                            containerColor = LightColorScheme.onSurfaceVariant,
                            contentColor = LightColorScheme.surfaceVariant,
                            disabledContainerColor = LightColorScheme.onSurfaceVariant,
                            disabledContentColor = LightColorScheme.surfaceVariant,
                        ),
                )
            }

        }
    }
}

@Composable
fun DistichOfDaySlide(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    distich: RandomDistich? = null,
    onClick: () -> Unit = {}
) {
    val distichText = buildString {
        distich?.rightText?.let { append(it) }
        if (distich != null) append("\n")
        distich?.leftText?.let { append(it) }
    }.ifEmpty { stringResource(Res.string.slider_distich_of_day_text) }
    val poetText = distich?.poetName ?: stringResource(Res.string.slider_distich_of_day_poet)

    Box(modifier = modifier
        .shadow(
            shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
            elevation = LocalSarvDimensions.current.dimen1,
            spotColor = MaterialTheme.colorScheme.tertiary,
            ambientColor = MaterialTheme.colorScheme.tertiary
        )
        .clickable(enabled = distich != null, onClick = onClick)
        .fillMaxSize()
    ){

        Image(
            painter = painterResource(Res.drawable.today_distich_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(LocalSarvDimensions.current.dimen16),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.slider_distich_of_day_title),
                    color = LightColorScheme.tertiary,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                )

                if (isExpandedScreen){

                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = distichText.replace("\n","      "),
                            color = LightColorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                        )

                        Text(
                            modifier = Modifier,
                            text = poetText,
                            color = LightColorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.End,
                        )
                    }

                }else{
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = distichText,
                        color = LightColorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = poetText,
                        color = LightColorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.End,
                    )
                }

            }

            if (isExpandedScreen) {
                Spacer(modifier = Modifier.weight(BEYT_SLIDE_SPACER_WEIGHT_EXPANDED))
            }else{
                Spacer(modifier = Modifier.weight(SLIDER_TOP_WEIGHT))
            }

        }
    }

}
