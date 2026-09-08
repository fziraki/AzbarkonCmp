package abkabk.azbarkon.features.memorization.practice

import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.testing.FakeMemorizationRepository
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import abkabk.azbarkon.testing.runViewModelTest
import kotlin.test.Test

class MemorizationPracticeViewModelTest {
    private var repository = FakeMemorizationRepository()

    @Test
    fun `loads due cards into session`() =
        runViewModelTest {
            repository.dueCards =
                abkabk.azbarkon.core.domain.result.Result.Success(
                    listOf(
                        sampleCard(id = 1),
                        sampleCard(id = 2),
                    ),
                )

            val viewModel = MemorizationPracticeViewModel(repository, poemId = null)
            val state = viewModel.state.value

            assertThat(state.currentCard?.id).isEqualTo(1)
            assertThat(state.totalCards).isEqualTo(2)
            assertThat(state.phase).isEqualTo(PracticePhase.SHOW_FRONT)
        }

    @Test
    fun `reveal grade selection submits on next card`() =
        runViewModelTest {
            val card = sampleCard(id = 5)
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))
            repository.reviewResult =
                abkabk.azbarkon.core.domain.result.Result.Success(
                    card.copy(interval = 3, consecutiveCorrect = 1),
                )

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnRevealClick)
            viewModel.onAction(MemorizationPracticeAction.OnGradeClick(SrsGrade.GOOD))

            assertThat(viewModel.state.value.phase).isEqualTo(PracticePhase.REVEALED)
            assertThat(viewModel.state.value.selectedGrade).isEqualTo(SrsGrade.GOOD)

            viewModel.onAction(MemorizationPracticeAction.OnNextCard)

            assertThat(repository.lastPoemReviewGrades).isEqualTo(listOf(SrsGrade.GOOD))
            assertThat(viewModel.state.value.phase).isEqualTo(PracticePhase.COMPLETE)
            assertThat(viewModel.state.value.sessionReviewed).isEqualTo(1)
            assertThat(viewModel.state.value.sessionLearned).isEqualTo(1)
            assertThat(viewModel.state.value.sessionMistakes).isEqualTo(0)
        }

    @Test
    fun `again grade increments session mistakes`() =
        runViewModelTest {
            val card = sampleCard(id = 11)
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))
            repository.reviewResult =
                abkabk.azbarkon.core.domain.result.Result.Success(
                    card.copy(interval = 0, consecutiveCorrect = 0),
                )

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnRevealClick)
            viewModel.onAction(MemorizationPracticeAction.OnGradeClick(SrsGrade.AGAIN))
            viewModel.onAction(MemorizationPracticeAction.OnNextCard)

            assertThat(viewModel.state.value.sessionReviewed).isEqualTo(1)
            assertThat(viewModel.state.value.sessionMistakes).isEqualTo(1)
            assertThat(viewModel.state.value.sessionLearned).isEqualTo(0)
        }

    @Test
    fun `typing check locks grade and submits on next card`() =
        runViewModelTest {
            val card =
                sampleCard(
                    id = 7,
                    front = "مصرع دوم ...",
                    back = "مصرع دوم",
                )
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))
            repository.reviewResult =
                abkabk.azbarkon.core.domain.result.Result.Success(
                    card.copy(interval = 1, consecutiveCorrect = 1),
                )

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnTypingModeClick)
            viewModel.onAction(MemorizationPracticeAction.OnTypedAnswerChange("مصرع دوم"))
            viewModel.onAction(MemorizationPracticeAction.OnSubmitTypedAnswer)

            assertThat(viewModel.state.value.phase).isEqualTo(PracticePhase.FEEDBACK)
            assertThat(viewModel.state.value.gradesLocked).isTrue()
            assertThat(viewModel.state.value.selectedGrade).isEqualTo(SrsGrade.EASY)

            viewModel.onAction(MemorizationPracticeAction.OnGradeClick(SrsGrade.AGAIN))
            assertThat(viewModel.state.value.selectedGrade).isEqualTo(SrsGrade.EASY)

            viewModel.onAction(MemorizationPracticeAction.OnNextCard)

            assertThat(repository.lastPoemReviewGrades).isEqualTo(listOf(SrsGrade.EASY))
        }

    @Test
    fun `typing evaluation uses continuation only`() =
        runViewModelTest {
            val card =
                sampleCard(
                    id = 8,
                    front = "مصرع ...",
                    back = "مصرع دوم",
                )
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnTypingModeClick)
            viewModel.onAction(MemorizationPracticeAction.OnTypedAnswerChange("کاملا غلط"))
            viewModel.onAction(MemorizationPracticeAction.OnSubmitTypedAnswer)

            assertThat(viewModel.state.value.selectedGrade).isEqualTo(SrsGrade.AGAIN)
        }

    @Test
    fun `eye from typing reveals immediately`() =
        runViewModelTest {
            val card = sampleCard(id = 9)
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnTypingModeClick)
            viewModel.onAction(MemorizationPracticeAction.OnTypedAnswerChange("test"))
            viewModel.onAction(MemorizationPracticeAction.OnRevealClick)

            assertThat(viewModel.state.value.phase).isEqualTo(PracticePhase.REVEALED)
            assertThat(viewModel.state.value.isTypingMode).isFalse()
            assertThat(viewModel.state.value.typedAnswer).isEqualTo("")
        }

    @Test
    fun `char based grading ignores extra words when letters match continuation`() =
        runViewModelTest {
            val card =
                sampleCard(
                    id = 10,
                    front = "مصرع دوم ...",
                    back = "مصرع دوم",
                )
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(listOf(card))

            val viewModel = MemorizationPracticeViewModel(repository, poemId = 10)
            viewModel.onAction(MemorizationPracticeAction.OnTypingModeClick)
            viewModel.onAction(MemorizationPracticeAction.OnTypedAnswerChange("مصرع اول مصرع دوم"))
            viewModel.onAction(MemorizationPracticeAction.OnSubmitTypedAnswer)

            assertThat(viewModel.state.value.selectedGrade).isEqualTo(SrsGrade.EASY)
        }

    @Test
    fun `empty due queue completes session`() =
        runViewModelTest {
            repository.dueCards = abkabk.azbarkon.core.domain.result.Result.Success(emptyList())

            val viewModel = MemorizationPracticeViewModel(repository, poemId = null)

            assertThat(viewModel.state.value.phase).isEqualTo(PracticePhase.COMPLETE)
            assertThat(viewModel.state.value.currentCard).isEqualTo(null)
        }

    private fun sampleCard(
        id: Long,
        front: String = "front",
        back: String = "back",
    ) = SrsCard(
        id = id,
        poemId = 10,
        cardIndex = 0,
        front = front,
        back = back,
        interval = 0,
        dueDateMillis = 0,
        consecutiveCorrect = 0,
    )
}
