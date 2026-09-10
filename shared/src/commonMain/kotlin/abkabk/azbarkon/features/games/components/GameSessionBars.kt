package abkabk.azbarkon.features.games.components

import abkabk.azbarkon.domain.model.games.GameConstants
import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.features.games.session.QuizAnswerPhase
import abkabk.azbarkon.ui.components.SarvButtonDefaults
import abkabk.azbarkon.ui.components.SarvPrimaryButton
import abkabk.azbarkon.ui.theme.LightColorScheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.arrow_back_left
import sarv.shared.generated.resources.cd_back
import sarv.shared.generated.resources.coin
import sarv.shared.generated.resources.complete_poem_title
import sarv.shared.generated.resources.game_continue
import sarv.shared.generated.resources.game_hint
import sarv.shared.generated.resources.game_hint_cost
import sarv.shared.generated.resources.game_next
import sarv.shared.generated.resources.game_quiz_progress_format
import sarv.shared.generated.resources.game_review
import sarv.shared.generated.resources.next_line_title
import sarv.shared.generated.resources.poetry_arrangement_title
import sarv.shared.generated.resources.whois_poet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val PROGRESS_FLIP_ROTATION_DEGREES = 180f
private const val PRIMARY_BUTTON_WEIGHT = 0.6f
private const val HINT_BUTTON_WEIGHT = 0.4f

@Composable
fun GameSessionTopBar(
    gameType: GameType,
    coinBalance: Int,
    currentQuizIndex: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        GameSessionTitleRow(
            title = gameTitle(gameType),
            onBackClick = onBackClick,
        )

        GameQuizProgressSection(
            currentQuizIndex = currentQuizIndex,
            coinBalance = coinBalance,
        )
    }
}

@Composable
private fun GameSessionTitleRow(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Icon(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(LocalSarvDimensions.current.dimen24)
                    .clickable(onClick = onBackClick),
            painter = painterResource(Res.drawable.arrow_back_left),
            contentDescription = stringResource(Res.string.cd_back),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun GameQuizProgressSection(
    currentQuizIndex: Int,
    coinBalance: Int,
    modifier: Modifier = Modifier,
) {
    val quizNumber = currentQuizIndex + 1
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GameCoinBadge(balance = coinBalance)

            Text(
                text =
                    stringResource(
                        Res.string.game_quiz_progress_format,
                        quizNumber,
                        GameConstants.QUIZ_COUNT,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }

        LinearProgressIndicator(
            progress = { (quizNumber.toFloat() / GameConstants.QUIZ_COUNT).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().rotate(PROGRESS_FLIP_ROTATION_DEGREES),
            gapSize = (-4).dp,
            drawStopIndicator = {},
            trackColor = LightColorScheme.outlineVariant,
        )
    }
}

@Composable
private fun GameCoinBadge(balance: Int) {
    Row(
        modifier =
            Modifier
                .border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).padding(horizontal = LocalSarvDimensions.current.dimen10, vertical = LocalSarvDimensions.current.dimen6),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.coin),
            contentDescription = null,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = balance.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GameSessionBottomBar(
    hasSelection: Boolean,
    isRevealing: Boolean,
    canPressPrimaryAction: Boolean,
    onHintClick: () -> Unit,
    onCheckAnswerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(LocalSarvDimensions.current.dimen16),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SarvPrimaryButton(
            text = checkAnswerLabel(hasSelection = hasSelection, isRevealing = isRevealing),
            onClick = onCheckAnswerClick,
            enabled = canPressPrimaryAction,
            modifier = Modifier.weight(PRIMARY_BUTTON_WEIGHT),
        )

        GameHintButton(
            onClick = onHintClick,
            modifier = Modifier.weight(HINT_BUTTON_WEIGHT),
        )
    }
}

@Composable
private fun GameHintButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = SarvButtonDefaults.Shape,
        border =
            androidx.compose.foundation.BorderStroke(
                LocalSarvDimensions.current.dimen1,
                MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.coin),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
            )
            Text(
                text = stringResource(Res.string.game_hint_cost),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.game_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun gameTitle(gameType: GameType): String =
    when (gameType) {
        GameType.NEXT_VERSE -> stringResource(Res.string.next_line_title)
        GameType.FIND_POET -> stringResource(Res.string.whois_poet_title)
        GameType.COMPLETE_POEM -> stringResource(Res.string.complete_poem_title)
        GameType.ORGANIZE_POEM -> stringResource(Res.string.poetry_arrangement_title)
    }

@Composable
private fun checkAnswerLabel(
    hasSelection: Boolean,
    isRevealing: Boolean,
): String =
    when {
        isRevealing -> stringResource(Res.string.game_continue)
        hasSelection -> stringResource(Res.string.game_review)
        else -> stringResource(Res.string.game_next)
    }

enum class GameOptionState {
    Default,
    Selected,
    Correct,
    Wrong,
    Disabled,
}

@Composable
fun GamePoemCard(
    modifier: Modifier = Modifier,
    poetName: String? = null,
    poetNameColor: Color? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .border(
                    LocalSarvDimensions.current.dimen1,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).padding(LocalSarvDimensions.current.dimen16),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)
    ) {
        poetName?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = poetNameColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
        content()
    }
}

@Composable
fun gamePoemUserAnswerTextColor(): Color = MaterialTheme.colorScheme.tertiary

@Composable
fun gamePoemCorrectAnswerTextColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun GamePoemCorrectRevealText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        style = style,
        color = gamePoemCorrectAnswerTextColor(),
        textAlign = textAlign,
    )
}

@Composable
fun GameInstructionText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.fillMaxWidth().padding(top = LocalSarvDimensions.current.dimen16),
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

fun optionStateForIndex(
    index: Int,
    selectedIndex: Int?,
    correctIndex: Int,
    disabledIndices: Set<Int>,
    answerPhase: QuizAnswerPhase,
): GameOptionState {
    if (index in disabledIndices) return GameOptionState.Disabled
    return when (answerPhase) {
        QuizAnswerPhase.Answering ->
            if (index == selectedIndex) GameOptionState.Selected else GameOptionState.Default

        QuizAnswerPhase.Correct ->
            when {
                index == correctIndex -> GameOptionState.Correct
                index == selectedIndex -> GameOptionState.Wrong
                else -> GameOptionState.Default
            }

        QuizAnswerPhase.Wrong ->
            when {
                index == correctIndex -> GameOptionState.Correct
                index == selectedIndex -> GameOptionState.Wrong
                else -> GameOptionState.Default
            }
    }
}

@Composable
fun gameOptionColors(state: GameOptionState): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (state) {
        GameOptionState.Default ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface

        GameOptionState.Disabled ->
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

        GameOptionState.Selected ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.onSurface

        GameOptionState.Correct ->
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary

        GameOptionState.Wrong ->
            MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

@Composable
fun Modifier.gameOptionStyle(state: GameOptionState): Modifier {
    val shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12)
    val (background, _) = gameOptionColors(state)
    val primary = MaterialTheme.colorScheme.primary
    return this
        .clip(shape)
        .then(
            when (state) {
                GameOptionState.Selected,
                GameOptionState.Correct,
                -> Modifier.border(LocalSarvDimensions.current.dimen2, primary, shape)

                GameOptionState.Disabled ->
                    Modifier.border(LocalSarvDimensions.current.dimen2, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape)

                else -> Modifier
            },
        ).background(background)
}
