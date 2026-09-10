package abkabk.azbarkon.features.games.session

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.games.GameConstants
import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.ui.components.SarvButtonDefaults
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.complete_poem_title
import sarv.shared.generated.resources.game_back_to_list
import sarv.shared.generated.resources.game_replay
import sarv.shared.generated.resources.game_result_correct
import sarv.shared.generated.resources.game_result_no_answer
import sarv.shared.generated.resources.game_result_progress_answered_format
import sarv.shared.generated.resources.game_result_progress_percent_format
import sarv.shared.generated.resources.game_result_progress_question_count_label
import sarv.shared.generated.resources.game_result_progress_total_questions_format
import sarv.shared.generated.resources.game_result_score
import sarv.shared.generated.resources.game_result_title
import sarv.shared.generated.resources.game_result_wrong
import sarv.shared.generated.resources.next_line_title
import sarv.shared.generated.resources.poetry_arrangement_title
import sarv.shared.generated.resources.replay
import sarv.shared.generated.resources.whois_poet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import androidx.compose.foundation.layout.offset

private const val PERCENT_MULTIPLIER = 100

@Composable
fun GameResultRoot(
    correctCount: Int,
    wrongCount: Int,
    noAnswerCount: Int,
    scoreDelta: Int,
    onReplayClick: () -> Unit,
    onBackToListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseScreen(
        screenState = UiScreenState.Success,
        modifier = modifier,
    ) {
        GameResultScreen(
            correctCount = correctCount,
            wrongCount = wrongCount,
            noAnswerCount = noAnswerCount,
            scoreDelta = scoreDelta,
            onReplayClick = onReplayClick,
            onBackToListClick = onBackToListClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun GameResultScreen(
    correctCount: Int,
    wrongCount: Int,
    noAnswerCount: Int,
    scoreDelta: Int,
    onReplayClick: () -> Unit,
    onBackToListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(LocalSarvDimensions.current.dimen16),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {
        Header(
            title = stringResource(Res.string.game_result_title),
            onBackClick = onBackToListClick,
        )

        GameResultScoreCard(scoreDelta = scoreDelta)

        GameResultStatsRow(
            correctCount = correctCount,
            wrongCount = wrongCount,
            noAnswerCount = noAnswerCount,
        )

        GameResultProgressCard(
            correctCount = correctCount,
            wrongCount = wrongCount,
        )

        GameResultReplayButton(
            onClick = onReplayClick,
            modifier = Modifier.fillMaxWidth(),
        )

        GameResultBackToListButton(
            onClick = onBackToListClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GameResultScoreCard(
    scoreDelta: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .background(MaterialTheme.colorScheme.primary)
                .padding(LocalSarvDimensions.current.dimen20),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
        ) {
            Text(
                text = scoreDelta.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "◆",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = stringResource(Res.string.game_result_score),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "◆",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

    }
}

@Composable
private fun GameResultStatsRow(
    correctCount: Int,
    wrongCount: Int,
    noAnswerCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        GameResultStatCard(
            value = noAnswerCount.toString(),
            label = stringResource(Res.string.game_result_no_answer),
            valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        GameResultStatCard(
            value = wrongCount.toString(),
            label = stringResource(Res.string.game_result_wrong),
            valueColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )

        GameResultStatCard(
            value = correctCount.toString(),
            label = stringResource(Res.string.game_result_correct),
            valueColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GameResultStatCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = LocalSarvDimensions.current.dimen16, horizontal = LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GameResultProgressCard(
    correctCount: Int,
    wrongCount: Int,
    modifier: Modifier = Modifier,
) {
    val answeredCount = correctCount + wrongCount
    val percent = (answeredCount * PERCENT_MULTIPLIER) / GameConstants.QUIZ_COUNT

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).background(MaterialTheme.colorScheme.surface)
                .padding(LocalSarvDimensions.current.dimen16),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            Text(
                text = stringResource(Res.string.game_result_progress_question_count_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(
                        Res.string.game_result_progress_total_questions_format,
                        GameConstants.QUIZ_COUNT,
                    ),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            GameResultSegmentedBar(
                filledCount = answeredCount,
                total = GameConstants.QUIZ_COUNT,
            )
            Text(
                text =
                    stringResource(
                        Res.string.game_result_progress_answered_format,
                        answeredCount,
                        GameConstants.QUIZ_COUNT,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        GameResultCircularProgress(
            percent = percent,
        )
    }
}

@Composable
private fun GameResultCircularProgress(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val strokeWidth = LocalSarvDimensions.current.dimen6

    Box(
        modifier = modifier.size(LocalSarvDimensions.current.dimen88),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * (percent / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen2),
        ) {
            Text(
                text =
                    stringResource(
                        Res.string.game_result_progress_percent_format,
                        percent,
                    ),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )

        }
    }
}

@Composable
private fun GameResultSegmentedBar(
    filledCount: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
    ) {
        repeat(total) { index ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(LocalSarvDimensions.current.dimen10)
                        .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen4))
                        .background(
                            if (index < filledCount) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
            )
        }
    }
}

@Composable
private fun GameResultReplayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = SarvButtonDefaults.Shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                vertical = LocalSarvDimensions.current.dimen8),
            horizontalArrangement = Arrangement.spacedBy(
                space = LocalSarvDimensions.current.dimen8,
                alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = stringResource(Res.string.game_replay),
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                painter = painterResource(Res.drawable.replay),
                contentDescription = null,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
            )

        }
    }
}

@Composable
private fun GameResultBackToListButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = SarvButtonDefaults.Shape,
        border = BorderStroke(LocalSarvDimensions.current.dimen1, MaterialTheme.colorScheme.outline),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth()
                .padding(
                    vertical = LocalSarvDimensions.current.dimen8
                ),
            text = stringResource(Res.string.game_back_to_list),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun GameResultScreenPreview() {
    SarvTheme {
        GameResultScreen(
            correctCount = 2,
            wrongCount = 1,
            noAnswerCount = 7,
            scoreDelta = 20,
            onReplayClick = {},
            onBackToListClick = {},
        )
    }
}
