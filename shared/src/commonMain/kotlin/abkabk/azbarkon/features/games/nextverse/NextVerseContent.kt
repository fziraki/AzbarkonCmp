package abkabk.azbarkon.features.games.nextverse

import abkabk.azbarkon.domain.model.games.GameQuestion
import abkabk.azbarkon.features.games.components.GameInstructionText
import abkabk.azbarkon.features.games.components.GameOptionState
import abkabk.azbarkon.features.games.components.GamePoemCard
import abkabk.azbarkon.features.games.components.gamePoemCorrectAnswerTextColor
import abkabk.azbarkon.features.games.components.gamePoemUserAnswerTextColor
import abkabk.azbarkon.features.games.components.gameOptionColors
import abkabk.azbarkon.features.games.components.gameOptionStyle
import abkabk.azbarkon.features.games.components.optionStateForIndex
import abkabk.azbarkon.features.games.session.QuizAnswerPhase
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.game_next_verse_instruction
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun NextVerseContent(
    question: GameQuestion.NextVerse,
    selectedOptionIndex: Int?,
    disabledOptionIndices: Set<Int>,
    answerPhase: QuizAnswerPhase,
    enabled: Boolean,
    onOptionSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpanded) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
            ) {
                NextVersePoemCard(question, selectedOptionIndex, answerPhase)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
            ) {
                NextVerseOptionList(
                    question, selectedOptionIndex, disabledOptionIndices,
                    answerPhase, enabled, onOptionSelect,
                )
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            NextVersePoemCard(question, selectedOptionIndex, answerPhase)
            NextVerseOptionList(
                question, selectedOptionIndex, disabledOptionIndices,
                answerPhase, enabled, onOptionSelect,
            )
        }
    }
}

@Composable
private fun NextVersePoemCard(
    question: GameQuestion.NextVerse,
    selectedOptionIndex: Int?,
    answerPhase: QuizAnswerPhase,
) {
    GamePoemCard(poetName = question.poetName) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = question.promptLine,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        when {
            answerPhase != QuizAnswerPhase.Answering ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = question.options[question.correctIndex],
                    style = MaterialTheme.typography.bodyLarge,
                    color = gamePoemCorrectAnswerTextColor(),
                    textAlign = TextAlign.Center,
                )
            selectedOptionIndex != null ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = question.options[selectedOptionIndex],
                    style = MaterialTheme.typography.bodyLarge,
                    color = gamePoemUserAnswerTextColor(),
                    textAlign = TextAlign.Center,
                )
            else ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "\u2026",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
        }
    }
    GameInstructionText(text = stringResource(Res.string.game_next_verse_instruction))
}

@Composable
private fun NextVerseOptionList(
    question: GameQuestion.NextVerse,
    selectedOptionIndex: Int?,
    disabledOptionIndices: Set<Int>,
    answerPhase: QuizAnswerPhase,
    enabled: Boolean,
    onOptionSelect: (Int) -> Unit,
) {
    question.options.forEachIndexed { index, option ->
        val state = optionStateForIndex(
            index = index,
            selectedIndex = selectedOptionIndex,
            correctIndex = question.correctIndex,
            disabledIndices = disabledOptionIndices,
            answerPhase = answerPhase,
        )
        val (_, contentColor) = gameOptionColors(state)
        val clickable = enabled &&
            state != GameOptionState.Disabled &&
            answerPhase == QuizAnswerPhase.Answering

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .gameOptionStyle(state)
                .clickable(enabled = clickable) { onOptionSelect(index) }
                .padding(LocalSarvDimensions.current.dimen16),
            text = option,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}
