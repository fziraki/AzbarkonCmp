package abkabk.azbarkon.features.chat

import abkabk.azbarkon.core.ui.keyboardAboveIme
import abkabk.azbarkon.core.ui.rememberKeyboardLiftPx
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSnackbarHostState
import abkabk.azbarkon.ui.components.SarvSnackbarHost
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.features.poets.list.PoetAvatar
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.arrow_back_right
import sarv.shared.generated.resources.cd_back
import sarv.shared.generated.resources.cd_send_message
import sarv.shared.generated.resources.chat_input_hint
import sarv.shared.generated.resources.chat_poet_says
import sarv.shared.generated.resources.chat_poet_typing
import sarv.shared.generated.resources.chat_subtitle
import sarv.shared.generated.resources.chat_title
import sarv.shared.generated.resources.send
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private val KEYBOARD_SCROLL_RETRY_DELAY_MILLIS = 100.milliseconds
private const val SEND_ICON_ROTATION_DEGREES = 180f

@Immutable
private data class ChatColors(
    val userBubble: androidx.compose.ui.graphics.Color,
    val userBubbleText: androidx.compose.ui.graphics.Color,
    val poetBubble: androidx.compose.ui.graphics.Color,
    val poetBubbleText: androidx.compose.ui.graphics.Color,
    val poetBubbleBorder: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val onAccent: androidx.compose.ui.graphics.Color,
    val inputBackground: androidx.compose.ui.graphics.Color,
    val inputBorder: androidx.compose.ui.graphics.Color,
    val inputText: androidx.compose.ui.graphics.Color,
    val inputPlaceholder: androidx.compose.ui.graphics.Color,
    val timestamp: androidx.compose.ui.graphics.Color,
)

private fun chatColors(colorScheme: ColorScheme): ChatColors =
    ChatColors(
        userBubble = colorScheme.surface,
        userBubbleText = colorScheme.onSurface,
        poetBubble = colorScheme.surfaceVariant,
        poetBubbleText = colorScheme.onSurfaceVariant,
        poetBubbleBorder = colorScheme.outlineVariant,
        accent = colorScheme.secondary,
        onAccent = colorScheme.onSecondary,
        inputBackground = colorScheme.surfaceVariant,
        inputBorder = colorScheme.outlineVariant,
        inputText = colorScheme.onSurfaceVariant,
        inputPlaceholder = colorScheme.onSurfaceVariant,
        timestamp = colorScheme.onSurfaceVariant,
    )

@Composable
fun ChatRoot(
    poetId: Int,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(poetId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ChatEvent.NavigateBack -> onBackClick()
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        ChatScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
fun ChatScreen(
    state: ChatState,
    onAction: (ChatAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val colors = chatColors(MaterialTheme.colorScheme)
    val keyboardLiftPx = rememberKeyboardLiftPx()

    LaunchedEffect(state.messages.size, state.isPoetTyping) {
        listState.scrollToLastMessage(state.messages.size, state.isPoetTyping)
    }

    LaunchedEffect(keyboardLiftPx, state.messages.size, state.isPoetTyping) {
        if (keyboardLiftPx > 0) {
            listState.scrollToLastMessage(state.messages.size, state.isPoetTyping)
            delay(KEYBOARD_SCROLL_RETRY_DELAY_MILLIS)
            listState.scrollToLastMessage(state.messages.size, state.isPoetTyping)
        }
    }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        topBar = {
            ChatTopBar(
                poetName = state.poetName,
                poetImageUrl = state.poetImageUrl,
                colors = colors,
                onBackClick = { onAction(ChatAction.OnBackClick) },
                isPoetTyping = state.isPoetTyping
            )
        },
        bottomBar = {

            ChatInputBar(
                value = state.inputText,
                colors = colors,
                onValueChange = { onAction(ChatAction.OnInputChange(it)) },
                onSendClick = { onAction(ChatAction.OnSendClick) },
            )
        },
        snackbarHost = {
            SarvSnackbarHost(hostState = LocalSnackbarHostState.current)
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + LocalSarvDimensions.current.dimen16,
                    bottom = paddingValues.calculateBottomPadding() + LocalSarvDimensions.current.dimen16,
                    start = LocalSarvDimensions.current.dimen16,
                    end = LocalSarvDimensions.current.dimen16,
                ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            items(
                items = state.messages,
                key = { message -> message.id },
            ) { message ->
                if (message.isFromUser) {
                    UserMessageBubble(message = message, colors = colors)
                } else {
                    PoetMessageBubble(
                        message = message,
                        poetName = state.poetName,
                        colors = colors,
                        onLongPress = { onAction(ChatAction.OnPoetMessageLongPress(message.id)) },
                    )
                }
            }

        }

    }
}

@Composable
private fun ChatTopBar(
    poetName: String,
    poetImageUrl: String?,
    colors: ChatColors,
    onBackClick: () -> Unit,
    isPoetTyping: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12)
    ) {

        Icon(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen24).clickable(onClick = onBackClick),
            painter = painterResource(Res.drawable.arrow_back_right),
            contentDescription = stringResource(Res.string.cd_back),
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = stringResource(Res.string.chat_title, poetName),
                style = MaterialTheme.typography.titleLarge,
                color = colors.accent,
                textAlign = TextAlign.End,
            )
            if (isPoetTyping) {
                Text(
                    text = stringResource(Res.string.chat_poet_typing, poetName),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.timestamp,
                    textAlign = TextAlign.End,
                )
            }else{
                Text(
                    text = stringResource(Res.string.chat_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.timestamp,
                    textAlign = TextAlign.End,
                )
            }
        }

        PoetAvatar(
            imageUrl = poetImageUrl,
            modifier =
                Modifier
                    .size(LocalSarvDimensions.current.dimen48)
                    .border(LocalSarvDimensions.current.dimen2, colors.accent.copy(alpha = 0.25f), CircleShape),
        )
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessageUi,
    colors: ChatColors,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .widthIn(max = maxWidth * 0.75f),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = LocalSarvDimensions.current.dimen20,
                                topEnd = LocalSarvDimensions.current.dimen20,
                                bottomStart = LocalSarvDimensions.current.dimen4,
                                bottomEnd = LocalSarvDimensions.current.dimen20,
                            ),
                        ).background(colors.userBubble)
                        .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.userBubbleText,
                    textAlign = TextAlign.Start,
                )
            }

            Row(
                modifier = Modifier.padding(top = LocalSarvDimensions.current.dimen4, start = LocalSarvDimensions.current.dimen4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
            ) {
                Text(
                    text = message.timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.timestamp,
                )
                Text(
                    text = "✓✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun PoetMessageBubble(
    message: ChatMessageUi,
    poetName: String,
    colors: ChatColors,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bubbleShape =
        RoundedCornerShape(
            topStart = LocalSarvDimensions.current.dimen20,
            topEnd = LocalSarvDimensions.current.dimen20,
            bottomStart = LocalSarvDimensions.current.dimen20,
            bottomEnd = LocalSarvDimensions.current.dimen4,
        )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .widthIn(max = maxWidth * 0.75f),
            horizontalAlignment = Alignment.End,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(bubbleShape)
                        .background(colors.poetBubble)
                        .border(LocalSarvDimensions.current.dimen1, colors.poetBubbleBorder, bubbleShape)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = onLongPress,
                        )
                        .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(Res.string.chat_poet_says, poetName),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.timestamp,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.poetBubbleText,
                        textAlign = TextAlign.End,
                    )
                }
            }

            Text(
                text = message.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.timestamp,
                modifier =
                    Modifier
                        .padding(top = LocalSarvDimensions.current.dimen4, end = LocalSarvDimensions.current.dimen4),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    colors: ChatColors,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val pillShape = RoundedCornerShape(LocalSarvDimensions.current.dimen28)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .keyboardAboveIme()
                .background(MaterialTheme.colorScheme.background)
                .padding(LocalSarvDimensions.current.dimen16),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(pillShape)
                    .background(colors.inputBackground)
                    .border(LocalSarvDimensions.current.dimen1, colors.inputBorder, pillShape)
                    .padding(LocalSarvDimensions.current.dimen4),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(
                onClick = onSendClick,
                modifier =
                    Modifier
                        .padding(start = LocalSarvDimensions.current.dimen4)
                        .size(LocalSarvDimensions.current.dimen40)
                        .clip(CircleShape)
                        .background(colors.accent),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.send),
                    contentDescription = stringResource(Res.string.cd_send_message),
                    tint = colors.onAccent,
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen20).rotate(SEND_ICON_ROTATION_DEGREES),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(
                            start = LocalSarvDimensions.current.dimen16,
                            end = LocalSarvDimensions.current.dimen4,
                            top = LocalSarvDimensions.current.dimen12,
                            bottom = LocalSarvDimensions.current.dimen12,
                        ),
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = colors.inputText,
                    ),
                cursorBrush = SolidColor(colors.accent),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.chat_input_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inputPlaceholder.copy(alpha = 0.7f),
                            )
                        }
                        innerTextField()
                    }
                },
            )


        }
    }
}

private suspend fun LazyListState.scrollToLastMessage(
    messageCount: Int,
    isPoetTyping: Boolean,
) {
    val lastIndex = messageCount + if (isPoetTyping) 1 else 0 - 1
    if (lastIndex >= 0) {
        animateScrollToItem(lastIndex)
    }
}

private val previewMessages =
    listOf(
        ChatMessageUi(
            id = "1",
            isFromUser = true,
            text = "دلم گرفته و خسته‌ام...",
            timeLabel = "11:30",
        ),
        ChatMessageUi(
            id = "2",
            isFromUser = false,
            text = "دلم گرفته و خسته‌ام...\n\nکه عشق آسان نمود اول",
            timeLabel = "11:30",
        ),
    )

@Preview
@Composable
private fun ChatScreenPreview() {
    SarvTheme(darkTheme = false) {
        ChatScreen(
            state =
                ChatState(
                    poetName = "حافظ",
                    messages = previewMessages,
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ChatScreenDarkPreview() {
    SarvTheme(darkTheme = true) {
        ChatScreen(
            state =
                ChatState(
                    poetName = "حافظ",
                    messages = previewMessages,
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ChatScreenTypingPreview() {
    SarvTheme {
        ChatScreen(
            state =
                ChatState(
                    poetName = "حافظ",
                    isPoetTyping = true,
                    messages =
                        listOf(
                            ChatMessageUi(
                                id = "1",
                                isFromUser = true,
                                text = "سلام",
                                timeLabel = "11:30",
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}
