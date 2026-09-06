package abkabk.azbarkon.features.games.findpoet

import abkabk.azbarkon.domain.model.games.GameQuestion
import abkabk.azbarkon.features.games.components.GameInstructionText
import abkabk.azbarkon.features.games.components.GameOptionState
import abkabk.azbarkon.features.games.components.GamePoemCard
import abkabk.azbarkon.features.games.components.gameOptionColors
import abkabk.azbarkon.features.games.components.gameOptionStyle
import abkabk.azbarkon.features.games.components.gamePoemCorrectAnswerTextColor
import abkabk.azbarkon.features.games.components.gamePoemUserAnswerTextColor
import abkabk.azbarkon.features.games.components.optionStateForIndex
import abkabk.azbarkon.features.games.session.QuizAnswerPhase
import abkabk.azbarkon.ui.components.NetworkImage
import abkabk.azbarkon.ui.components.ShimmerPlaceholder
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.game_find_poet_instruction
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun FindPoetContent(
    question: GameQuestion.FindPoet,
    selectedPoetId: Int?,
    disabledOptionIndices: Set<Int>,
    answerPhase: QuizAnswerPhase,
    enabled: Boolean,
    onPoetSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val correctIndex = question.options.indexOfFirst { it.id == question.correctPoetId }
    val selectedIndex = question.options.indexOfFirst { it.id == selectedPoetId }.takeIf { it >= 0 }
    val correctPoet = question.options.first { it.id == question.correctPoetId }
    val selectedPoet = selectedPoetId?.let { id -> question.options.firstOrNull { it.id == id } }
    val poetName = when {
        answerPhase != QuizAnswerPhase.Answering -> correctPoet.name
        selectedPoet != null -> selectedPoet.name
        else -> "???"
    }
    val poetNameColor = when {
        answerPhase != QuizAnswerPhase.Answering -> gamePoemCorrectAnswerTextColor()
        selectedPoet != null -> gamePoemUserAnswerTextColor()
        else -> null
    }
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
                FindPoetPoemCard(question, poetName, poetNameColor)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
            ) {
                FindPoetOptionGrid(
                    question, correctIndex, selectedIndex,
                    disabledOptionIndices, answerPhase, enabled, onPoetSelect,
                )
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            FindPoetPoemCard(question, poetName, poetNameColor)
            FindPoetOptionGrid(
                question, correctIndex, selectedIndex,
                disabledOptionIndices, answerPhase, enabled, onPoetSelect,
            )
        }
    }
}

@Composable
private fun FindPoetPoemCard(
    question: GameQuestion.FindPoet,
    poetName: String,
    poetNameColor: Color?,
) {
    GamePoemCard(poetName = poetName, poetNameColor = poetNameColor) {
        Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = question.rightHemistich,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = question.leftHemistich,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
    GameInstructionText(text = stringResource(Res.string.game_find_poet_instruction))
}

@Composable
private fun FindPoetOptionGrid(
    question: GameQuestion.FindPoet,
    correctIndex: Int,
    selectedIndex: Int?,
    disabledOptionIndices: Set<Int>,
    answerPhase: QuizAnswerPhase,
    enabled: Boolean,
    onPoetSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
        question.options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
            ) {
                rowOptions.forEach { poetOption ->
                    val index = question.options.indexOf(poetOption)
                    val state = optionStateForIndex(
                        index = index,
                        selectedIndex = selectedIndex,
                        correctIndex = correctIndex,
                        disabledIndices = disabledOptionIndices,
                        answerPhase = answerPhase,
                    )
                    val (_, contentColor) = gameOptionColors(state)
                    val clickable = enabled &&
                        state != GameOptionState.Disabled &&
                        answerPhase == QuizAnswerPhase.Answering

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .gameOptionStyle(state)
                            .clickable(enabled = clickable) {
                                onPoetSelect(poetOption.id)
                            }.padding(LocalSarvDimensions.current.dimen12),
                        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FindPoetOptionAvatar(imageUrl = poetOption.imageUrl)
                        Text(
                            modifier = Modifier.weight(1f),
                            text = poetOption.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FindPoetOptionAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(LocalSarvDimensions.current.dimen36).clip(CircleShape)) {
        if (imageUrl.isNullOrBlank()) {
            ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            NetworkImage(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
