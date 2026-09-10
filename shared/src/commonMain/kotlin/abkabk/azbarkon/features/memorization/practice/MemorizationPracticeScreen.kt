package abkabk.azbarkon.features.memorization.practice

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.notifications.rememberNotificationPermissionRequester
import abkabk.azbarkon.core.ui.keyboardAboveIme
import abkabk.azbarkon.core.ui.rememberKeyboardLiftPx
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSnackbarHostState
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import abkabk.azbarkon.domain.srs.CardGenerator
import abkabk.azbarkon.domain.srs.DiffTokenType
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.components.HeaderAction
import abkabk.azbarkon.ui.components.SarvAlertDialog
import abkabk.azbarkon.ui.components.SarvButton
import abkabk.azbarkon.ui.components.SarvPrimaryButton
import abkabk.azbarkon.ui.components.SarvSnackbarHost
import abkabk.azbarkon.ui.theme.LightColorScheme
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.ic_help
import sarv.shared.generated.resources.keyboard
import sarv.shared.generated.resources.memorization_grade_again
import sarv.shared.generated.resources.memorization_grade_easy
import sarv.shared.generated.resources.memorization_grade_good
import sarv.shared.generated.resources.memorization_grade_hard
import sarv.shared.generated.resources.memorization_keyboard_content_description
import sarv.shared.generated.resources.memorization_keyboard_label
import sarv.shared.generated.resources.memorization_next_verse
import sarv.shared.generated.resources.memorization_practice_close
import sarv.shared.generated.resources.memorization_practice_complete
import sarv.shared.generated.resources.memorization_practice_done
import sarv.shared.generated.resources.memorization_practice_help
import sarv.shared.generated.resources.memorization_practice_help_body
import sarv.shared.generated.resources.memorization_practice_help_title
import sarv.shared.generated.resources.memorization_practice_next_poem
import sarv.shared.generated.resources.memorization_practice_progress
import sarv.shared.generated.resources.memorization_practice_stat_learned
import sarv.shared.generated.resources.memorization_practice_stat_mistakes
import sarv.shared.generated.resources.memorization_practice_stat_today
import sarv.shared.generated.resources.memorization_practice_title
import sarv.shared.generated.resources.memorization_reveal_content_description
import sarv.shared.generated.resources.memorization_reveal_label
import sarv.shared.generated.resources.memorization_submit_typing
import sarv.shared.generated.resources.memorization_typing_hint
import sarv.shared.generated.resources.reveal_eye

private const val PROGRESS_FLIP_ROTATION_DEGREES = 180f
private val CORRECT_DIFF_COLOR = Color(0xFF2E7D32)
private val MISSING_DIFF_COLOR = Color(0xFFF9A825)
private val WRONG_DIFF_COLOR = Color(0xFFC62828)

@Composable
fun MemorizationPracticeRoot(
    poemId: Int?,
    onBackClick: () -> Unit,
    onNavigateToPoem: (Int) -> Unit = {},
    viewModel: MemorizationPracticeViewModel = koinViewModel { parametersOf(poemId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionGateway: NotificationPermissionGateway = koinInject()
    val reviewNotificationCoordinator: MemorizationReviewNotificationCoordinator = koinInject()
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(permissionGateway.areNotificationsEnabled()) }

    val requestNotificationPermission =
        rememberNotificationPermissionRequester { granted ->
            notificationsEnabled = granted
            if (granted) {
                scope.launch {
                    reviewNotificationCoordinator.sync()
                }
                viewModel.onAction(MemorizationPracticeAction.OnNotificationPermissionGranted)
            }
        }

    LaunchedEffect(Unit) {
        notificationsEnabled = permissionGateway.areNotificationsEnabled()
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MemorizationPracticeEvent.NavigateBack -> onBackClick()
            is MemorizationPracticeEvent.NavigateToPoem -> onNavigateToPoem(event.poemId)
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        MemorizationPracticeScreen(
            state = state,
            onAction = viewModel::onAction,
            notificationsEnabled = notificationsEnabled,
            onAlarmClick = requestNotificationPermission,
        )
    }
}

@Suppress("LongMethod")
@Composable
fun MemorizationPracticeScreen(
    state: MemorizationPracticeState,
    onAction: (MemorizationPracticeAction) -> Unit,
    modifier: Modifier = Modifier,
    notificationsEnabled: Boolean = false,
    onAlarmClick: (() -> Unit)? = null,
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    HelpDialog(show = showHelpDialog, onDismiss = { showHelpDialog = false })

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Header(
                title = stringResource(Res.string.memorization_practice_title),
                onBackClick = { onAction(MemorizationPracticeAction.OnBackClick) },
                action = HeaderAction.Alarm(isEnabled = notificationsEnabled, onClick = onAlarmClick ?: {}),
            )
        },
        bottomBar = {
            if (state.phase != PracticePhase.COMPLETE) {
                PracticeBottomPanel(
                    state = state,
                    onAction = onAction,
                    modifier =
                        Modifier
                            .padding(horizontal = LocalSarvDimensions.current.dimen20, vertical = LocalSarvDimensions.current.dimen12)
                            .keyboardAboveIme(),
                )
            }
        },
        snackbarHost = {
            SarvSnackbarHost(hostState = LocalSnackbarHostState.current)
        },
    ) { paddingValues ->
        when (state.phase) {
            PracticePhase.COMPLETE -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(LocalSarvDimensions.current.dimen24),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.memorization_practice_complete),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(LocalSarvDimensions.current.dimen16))
                    SarvPrimaryButton(
                        text = stringResource(
                            if (state.hasOtherDuePoems) Res.string.memorization_practice_next_poem
                            else Res.string.memorization_practice_close
                        ),
                        onClick = {
                            if (state.hasOtherDuePoems) {
                                onAction(MemorizationPracticeAction.OnNextPoemClick)
                            } else {
                                onAction(MemorizationPracticeAction.OnBackClick)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        verticalTextPadding = LocalSarvDimensions.current.dimen8
                    )
                }
            }

            else -> {
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding()),
                ) {
                    val density = LocalDensity.current
                    val keyboardLift = with(density) { rememberKeyboardLiftPx().toDp() }
                    val animatedKeyboardLift by animateDpAsState(keyboardLift, label = "keyboardLift")
                    val bottomContentPadding = maxHeight / 2 + animatedKeyboardLift / 2

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = LocalSarvDimensions.current.dimen20),
                    ) {
                        if (state.totalCards > 0) {
                            PracticeProgressSection(
                                cardIndex = state.cardIndex,
                                totalCards = state.totalCards,
                                onHelpClick = { showHelpDialog = true },
                                modifier = Modifier.padding(
                                    top = LocalSarvDimensions.current.dimen16,
                                    bottom = LocalSarvDimensions.current.dimen12,
                                ),
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = bottomContentPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillParentMaxWidth()
                                            .fillParentMaxHeight(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    state.currentCard?.let { card ->
                                        AnimatedContent(
                                            targetState = card.id,
                                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                                            label = "cardTransition",
                                        ) {
                                            PracticeCardContent(
                                                state = state,
                                                card = card,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    SarvAlertDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.memorization_practice_help_title),
        text = stringResource(Res.string.memorization_practice_help_body),
        confirmLabel = stringResource(Res.string.memorization_practice_done),
        onConfirm = onDismiss,
    )
}

@Composable
private fun PracticeProgressSection(
    cardIndex: Int,
    totalCards: Int,
    modifier: Modifier = Modifier,
    onHelpClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.width(LocalSarvDimensions.current.dimen24))
            Text(
                text =
                    stringResource(
                        Res.string.memorization_practice_progress,
                        cardIndex,
                        totalCards,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_help),
                contentDescription = stringResource(Res.string.memorization_practice_help),
                modifier = Modifier
                    .clickable { onHelpClick() }
                    .size(LocalSarvDimensions.current.dimen24),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LinearProgressIndicator(
            progress = { (cardIndex.toFloat() / totalCards.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().rotate(PROGRESS_FLIP_ROTATION_DEGREES),
            drawStopIndicator = {},
            trackColor = LightColorScheme.outlineVariant,
            gapSize = (-4).dp
        )
    }
}

@Composable
private fun PracticeCardContent(
    state: MemorizationPracticeState,
    card: PracticeCardUi,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = LocalSarvDimensions.current.dimen16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {
        when (state.phase) {
            PracticePhase.SHOW_FRONT -> {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            PracticePhase.REVEALED -> {
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            PracticePhase.FEEDBACK -> {
                RevealedFrontText(
                    front = card.front,
                    continuation = card.expectedContinuation,
                    modifier = Modifier.fillMaxWidth(),
                )
                DiffText(
                    tokens = state.diffTokens,
                    fallback = state.typedAnswer,
                    modifier = Modifier.fillMaxWidth().padding(top = LocalSarvDimensions.current.dimen8),
                )
            }

            PracticePhase.COMPLETE -> Unit
        }
    }
}

@Composable
private fun TypingInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(LocalSarvDimensions.current.dimen12),
        textStyle =
            MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = stringResource(Res.string.memorization_typing_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            inner()
        },
    )
}

@Composable
private fun PracticeBottomPanel(
    state: MemorizationPracticeState,
    onAction: (MemorizationPracticeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8, alignment = Alignment.CenterVertically),
    ) {
        if (state.phase == PracticePhase.SHOW_FRONT && state.isTypingMode) {
            TypingInputField(
                value = state.typedAnswer,
                onValueChange = { onAction(MemorizationPracticeAction.OnTypedAnswerChange(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.phase == PracticePhase.REVEALED || state.phase == PracticePhase.FEEDBACK) {
            GradeButtons(
                onAction = onAction,
                selectedGrade = state.selectedGrade,
                suggestedGrade = state.suggestedGrade,
                enabled = !state.gradesLocked,
            )
        }

        PracticeActionRow(
            state = state,
            onAction = onAction,
        )

        PracticeSessionStatsBar(state = state)
    }
}

@Composable
private fun RevealedFrontText(
    front: String,
    continuation: String,
    modifier: Modifier = Modifier,
) {
    val parts = CardGenerator.revealedFrontParts(front, continuation)
    val annotated =
        buildAnnotatedString {
            append(parts.prefix)
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
            append(parts.continuation)
            pop()
            append(parts.suffix)
        }
    Text(
        text = annotated,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun PracticeModeIconButton(
    icon: Painter,
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
    ) {
        Box(
            modifier =
                Modifier
                    .size(LocalSarvDimensions.current.dimen48)
                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen48))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                tint =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PracticeActionRow(
    state: MemorizationPracticeState,
    onAction: (MemorizationPracticeAction) -> Unit,
) {
    val showModeIcons = state.phase == PracticePhase.SHOW_FRONT
    val primaryButtonState = primaryButtonState(state)

    if (!showModeIcons) {
        SarvPrimaryButton(
            text = stringResource(primaryButtonState.labelRes),
            onClick = { onAction(primaryButtonState.action) },
            modifier = Modifier.fillMaxWidth(),
            enabled = primaryButtonState.enabled,
            verticalTextPadding = LocalSarvDimensions.current.dimen8
        )
        return
    }

    val showPrimaryButton = primaryButtonState.visible

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        PracticeModeIconButton(
            icon = painterResource(Res.drawable.reveal_eye),
            label = stringResource(Res.string.memorization_reveal_label),
            contentDescription = stringResource(Res.string.memorization_reveal_content_description),
            selected = !state.isTypingMode,
            onClick = { onAction(MemorizationPracticeAction.OnRevealClick) },
        )

        if (showPrimaryButton) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                SarvPrimaryButton(
                    text = stringResource(primaryButtonState.labelRes),
                    onClick = { onAction(primaryButtonState.action) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = primaryButtonState.enabled,
                    verticalTextPadding = LocalSarvDimensions.current.dimen8
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        PracticeModeIconButton(
            icon = painterResource(Res.drawable.keyboard),
            label = stringResource(Res.string.memorization_keyboard_label),
            contentDescription = stringResource(Res.string.memorization_keyboard_content_description),
            selected = state.isTypingMode,
            onClick = { onAction(MemorizationPracticeAction.OnTypingModeClick) },
        )
    }
    }

@Composable
private fun PracticeSessionStatsBar(state: MemorizationPracticeState) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = LocalSarvDimensions.current.dimen16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PracticeStatItem(
            value = state.sessionMistakes,
            label = stringResource(Res.string.memorization_practice_stat_mistakes),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.height(LocalSarvDimensions.current.dimen40),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        PracticeStatItem(
            value = state.sessionReviewed,
            label = stringResource(Res.string.memorization_practice_stat_today),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.height(LocalSarvDimensions.current.dimen40),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        PracticeStatItem(
            value = state.sessionLearned,
            label = stringResource(Res.string.memorization_practice_stat_learned),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PracticeStatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private data class PrimaryButtonState(
    val visible: Boolean,
    val labelRes: org.jetbrains.compose.resources.StringResource,
    val enabled: Boolean,
    val action: MemorizationPracticeAction,
)

private fun primaryButtonState(state: MemorizationPracticeState): PrimaryButtonState =
    when (state.phase) {
        PracticePhase.SHOW_FRONT ->
            if (state.isTypingMode) {
                PrimaryButtonState(
                    visible = true,
                    labelRes = Res.string.memorization_submit_typing,
                    enabled = state.typedAnswer.isNotBlank(),
                    action = MemorizationPracticeAction.OnSubmitTypedAnswer,
                )
            } else {
                PrimaryButtonState(
                    visible = false,
                    labelRes = Res.string.memorization_submit_typing,
                    enabled = false,
                    action = MemorizationPracticeAction.OnSubmitTypedAnswer,
                )
            }

        PracticePhase.REVEALED ->
            PrimaryButtonState(
                visible = true,
                labelRes = Res.string.memorization_next_verse,
                enabled = state.selectedGrade != null,
                action = MemorizationPracticeAction.OnNextCard,
            )

        PracticePhase.FEEDBACK ->
            PrimaryButtonState(
                visible = true,
                labelRes = Res.string.memorization_next_verse,
                enabled = state.selectedGrade != null,
                action = MemorizationPracticeAction.OnNextCard,
            )

        PracticePhase.COMPLETE ->
            PrimaryButtonState(
                visible = false,
                labelRes = Res.string.memorization_next_verse,
                enabled = false,
                action = MemorizationPracticeAction.OnNextCard,
            )
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GradeButtons(
    onAction: (MemorizationPracticeAction) -> Unit,
    selectedGrade: SrsGrade? = null,
    suggestedGrade: SrsGrade? = null,
    enabled: Boolean = true,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        GradeButton(
            label = stringResource(Res.string.memorization_grade_again),
            grade = SrsGrade.AGAIN,
            isSelected = selectedGrade == SrsGrade.AGAIN,
            isSuggested = suggestedGrade == SrsGrade.AGAIN,
            enabled = enabled,
            onAction = onAction,
        )
        GradeButton(
            label = stringResource(Res.string.memorization_grade_hard),
            grade = SrsGrade.HARD,
            isSelected = selectedGrade == SrsGrade.HARD,
            isSuggested = suggestedGrade == SrsGrade.HARD,
            enabled = enabled,
            onAction = onAction,
        )
        GradeButton(
            label = stringResource(Res.string.memorization_grade_good),
            grade = SrsGrade.GOOD,
            isSelected = selectedGrade == SrsGrade.GOOD,
            isSuggested = suggestedGrade == SrsGrade.GOOD,
            enabled = enabled,
            onAction = onAction,
        )
        GradeButton(
            label = stringResource(Res.string.memorization_grade_easy),
            grade = SrsGrade.EASY,
            isSelected = selectedGrade == SrsGrade.EASY,
            isSuggested = suggestedGrade == SrsGrade.EASY,
            enabled = enabled,
            onAction = onAction,
        )
    }
}

@Composable
private fun GradeButton(
    label: String,
    grade: SrsGrade,
    isSelected: Boolean,
    isSuggested: Boolean,
    enabled: Boolean,
    onAction: (MemorizationPracticeAction) -> Unit,
) {
    val containerColor =
        when {
            isSelected -> MaterialTheme.colorScheme.primary
            isSuggested -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    val contentColor =
        when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isSuggested -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }
    SarvButton(
        text = label,
        onClick = { onAction(MemorizationPracticeAction.OnGradeClick(grade)) },
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor,
                disabledContentColor = contentColor,
            ),
    )
}

@Composable
private fun DiffText(
    tokens: List<abkabk.azbarkon.domain.srs.DiffToken>,
    fallback: String,
    modifier: Modifier = Modifier,
) {
    val annotated: AnnotatedString =
        if (tokens.isEmpty()) {
            AnnotatedString(fallback)
        } else {
            buildAnnotatedString {
                tokens.forEachIndexed { index, token ->
                    if (index > 0) append(' ')
                    val color =
                        when (token.type) {
                            DiffTokenType.CORRECT -> CORRECT_DIFF_COLOR
                            DiffTokenType.MISSING -> MISSING_DIFF_COLOR
                            DiffTokenType.WRONG -> WRONG_DIFF_COLOR
                        }
                    pushStyle(SpanStyle(color = color))
                    append(token.text)
                    pop()
                }
            }
        }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun MemorizationPracticeScreenPreview() {
    SarvTheme {
        MemorizationPracticeScreen(
            state =
                MemorizationPracticeState(
                    phase = PracticePhase.SHOW_FRONT,
                    currentCard =
                        PracticeCardUi(
                            id = 1,
                            front = "که عشق آسان نمود ...",
                            back = "که عشق آسان نمود اول",
                            expectedContinuation = "اول",
                        ),
                    cardIndex = 1,
                    totalCards = 5,
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun MemorizationPracticeScreenRevealedPreview() {
    SarvTheme {
        MemorizationPracticeScreen(
            state =
                MemorizationPracticeState(
                    phase = PracticePhase.REVEALED,
                    currentCard =
                        PracticeCardUi(
                            id = 1,
                            front = "که عشق آسان نمود ...",
                            back = "که عشق آسان نمود اول",
                            expectedContinuation = "اول",
                        ),
                    cardIndex = 1,
                    totalCards = 5,
                ),
            onAction = {},
        )
    }
}
