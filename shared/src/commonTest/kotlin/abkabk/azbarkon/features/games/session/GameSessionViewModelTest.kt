package abkabk.azbarkon.features.games.session

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.games.GameConstants
import abkabk.azbarkon.domain.model.games.GameGenerationCache
import abkabk.azbarkon.domain.model.games.GameQuestion
import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.domain.model.games.OrganizeLine
import abkabk.azbarkon.domain.repository.GamesRepository
import abkabk.azbarkon.domain.usecase.ApplyGameHintUseCase
import abkabk.azbarkon.domain.usecase.EvaluateGameAnswerUseCase
import abkabk.azbarkon.testing.FakeUserPreferencesRepository
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import kotlinx.coroutines.flow.first
import abkabk.azbarkon.testing.runViewModelTest
import kotlin.test.Test

class GameSessionViewModelTest {

    @Test
    fun `hint is blocked when coin balance is below cost`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            preferences.adjustCoinBalance(-700)
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            assertThat(viewModel.state.value.canUseHint).isFalse()
        }

    @Test
    fun `hint deducts coins and disables one wrong option`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            val balanceBefore = preferences.getCoinBalance()
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(viewModel.state.value.hintUsedThisQuiz).isTrue()
            assertThat(viewModel.state.value.disabledOptionIndices).isNotEmpty()
            assertThat(preferences.getCoinBalance()).isEqualTo(balanceBefore - GameConstants.HINT_COST)
        }

    @Test
    fun `skip without selection counts as no answer without deducting coins`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            val balanceBefore = preferences.getCoinBalance()
            assertThat(viewModel.state.value.hasSelection).isFalse()
            assertThat(viewModel.state.value.canPressPrimaryAction).isTrue()

            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.answerPhase).isEqualTo(QuizAnswerPhase.Wrong)
            assertThat(viewModel.state.value.noAnswerCount).isEqualTo(1)
            assertThat(viewModel.state.value.wrongCount).isEqualTo(0)
            assertThat(viewModel.state.value.isRevealing).isTrue()
            assertThat(viewModel.state.value.currentQuizIndex).isEqualTo(0)
            assertThat(viewModel.state.value.canPressPrimaryAction).isTrue()
            assertThat(preferences.getCoinBalance()).isEqualTo(balanceBefore)
            assertThat(viewModel.state.value.sessionScoreDelta).isEqualTo(0)
        }

    @Test
    fun `quiz advances only after continue during reveal`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.isRevealing).isTrue()
            assertThat(viewModel.state.value.currentQuizIndex).isEqualTo(0)

            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.isAnswering).isTrue()
            assertThat(viewModel.state.value.currentQuizIndex).isEqualTo(1)
        }

    @Test
    fun `hasSelection becomes true after option is selected`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            assertThat(viewModel.state.value.hasSelection).isFalse()
            viewModel.onAction(GameSessionAction.OnOptionSelected(1))
            assertThat(viewModel.state.value.hasSelection).isTrue()
        }

    @Test
    fun `wrong answer increments wrong count immediately`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            viewModel.onAction(GameSessionAction.OnOptionSelected(1))
            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.answerPhase).isEqualTo(QuizAnswerPhase.Wrong)
            assertThat(viewModel.state.value.wrongCount).isEqualTo(1)
            assertThat(viewModel.state.value.noAnswerCount).isEqualTo(0)
        }

    @Test
    fun `second hint on same quiz does not deduct additional coins`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            val balanceBefore = preferences.getCoinBalance()
            viewModel.onAction(GameSessionAction.OnHintClick)
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(preferences.getCoinBalance()).isEqualTo(balanceBefore - GameConstants.HINT_COST)
            assertThat(viewModel.state.value.hintUsedThisQuiz).isTrue()
        }

    @Test
    fun `perfect session increments perfectGameSessions`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository =
                        object : GamesRepository {
                            override fun createGenerationCache() = GameGenerationCache()

                            override suspend fun generateQuestion(
                                gameType: GameType,
                                sessionSeed: Long,
                                quizIndex: Int,
                                cache: GameGenerationCache,
                            ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
                                Result.Success(
                                    GameQuestion.NextVerse(
                                        promptLine = "prompt",
                                        poetName = "حافظ",
                                        options = listOf("correct", "wrong1", "wrong2", "wrong3"),
                                        correctIndex = 0,
                                    ),
                                )

                            override suspend fun generateQuizBatch(
                                gameType: GameType,
                                seed: Long,
                                count: Int,
                            ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
                                Result.Success(
                                    List(count) {
                                        GameQuestion.NextVerse(
                                            promptLine = "prompt",
                                            poetName = "حافظ",
                                            options = listOf("correct", "wrong1", "wrong2", "wrong3"),
                                            correctIndex = 0,
                                        )
                                    },
                                )
                        },
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            viewModel.events.test {
                repeat(GameConstants.QUIZ_COUNT) {
                    viewModel.onAction(GameSessionAction.OnOptionSelected(0))
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                }
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            assertThat(preferences.observeGameStats().first().perfectGameSessions).isEqualTo(1)
        }

    @Test
    fun `session with wrong answer does not increment perfectGameSessions`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository = FakeGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )

            viewModel.events.test {
                viewModel.onAction(GameSessionAction.OnOptionSelected(1))
                viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

                repeat(GameConstants.QUIZ_COUNT - 1) {
                    viewModel.onAction(GameSessionAction.OnOptionSelected(0))
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                }
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            assertThat(preferences.observeGameStats().first().perfectGameSessions).isEqualTo(0)
        }

    @Test
    fun `correct answer on last quiz navigates to result summary`() =
        runViewModelTest {
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.NEXT_VERSE,
                    gamesRepository =
                        object : GamesRepository {
                            override fun createGenerationCache() = GameGenerationCache()

                            override suspend fun generateQuestion(
                                gameType: GameType,
                                sessionSeed: Long,
                                quizIndex: Int,
                                cache: GameGenerationCache,
                            ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
                                Result.Success(
                                    GameQuestion.NextVerse(
                                        promptLine = "prompt",
                                        poetName = "حافظ",
                                        options = listOf("correct", "wrong1", "wrong2", "wrong3"),
                                        correctIndex = 0,
                                    ),
                                )

                            override suspend fun generateQuizBatch(
                                gameType: GameType,
                                seed: Long,
                                count: Int,
                            ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
                                Result.Success(
                                    List(count) {
                                        GameQuestion.NextVerse(
                                            promptLine = "prompt",
                                            poetName = "حافظ",
                                            options = listOf("correct", "wrong1", "wrong2", "wrong3"),
                                            correctIndex = 0,
                                        )
                                    },
                                )
                        },
                    userPreferencesRepository = FakeUserPreferencesRepository(),
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(FakeUserPreferencesRepository()),
                )

            viewModel.events.test {
                repeat(GameConstants.QUIZ_COUNT) {
                    viewModel.onAction(GameSessionAction.OnOptionSelected(0))
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                }

                val event = awaitItem()
                assertThat(event).isInstanceOf(GameSessionEvent.NavigateToResult::class)
                assertThat((event as GameSessionEvent.NavigateToResult).summary.correctCount)
                    .isEqualTo(GameConstants.QUIZ_COUNT)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `complete poem word selection adds word to filledWords`() =
        runViewModelTest {
            val viewModel = createCompletePoemViewModel()

            viewModel.onAction(GameSessionAction.OnWordSelected("word1"))

            assertThat(viewModel.state.value.filledWords).isEqualTo(listOf("word1"))
        }

    @Test
    fun `complete poem word selection toggles off when same word tapped again`() =
        runViewModelTest {
            val viewModel = createCompletePoemViewModel()

            viewModel.onAction(GameSessionAction.OnWordSelected("word1"))
            viewModel.onAction(GameSessionAction.OnWordSelected("word1"))

            assertThat(viewModel.state.value.filledWords).isEqualTo(emptyList())
        }

    @Test
    fun `complete poem ignores third word until one selection is cleared`() =
        runViewModelTest {
            val viewModel = createCompletePoemViewModel()

            viewModel.onAction(GameSessionAction.OnWordSelected("word1"))
            viewModel.onAction(GameSessionAction.OnWordSelected("word2"))
            viewModel.onAction(GameSessionAction.OnWordSelected("wrong1"))

            assertThat(viewModel.state.value.filledWords).isEqualTo(listOf("word1", "word2"))

            viewModel.onAction(GameSessionAction.OnWordSelected("word1"))
            viewModel.onAction(GameSessionAction.OnWordSelected("wrong1"))

            assertThat(viewModel.state.value.filledWords).isEqualTo(listOf("word2", "wrong1"))
        }

    @Test
    fun `reorder lines updates orderedLineIds`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-1", "line-0", "line-2", "line-3"))

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 0, toIndex = 2))

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-2", "line-1", "line-3"))
        }

    @Test
    fun `reorder lines supports jumping from last index to first`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-1", "line-0", "line-2", "line-3"))

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 3, toIndex = 0))

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-3", "line-1", "line-0", "line-2"))
        }

    @Test
    fun `reorder ignores moves involving pinned line`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()
            viewModel.onAction(GameSessionAction.OnHintClick)

            val orderAfterHint = viewModel.state.value.orderedLineIds
            val pinnedIndex = viewModel.state.value.pinnedLineIndex
            assertThat(pinnedIndex).isEqualTo(1)

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = pinnedIndex!!, toIndex = 3))
            assertThat(viewModel.state.value.orderedLineIds).isEqualTo(orderAfterHint)

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 3, toIndex = pinnedIndex))
            assertThat(viewModel.state.value.orderedLineIds).isEqualTo(orderAfterHint)
        }

    @Test
    fun `reorder can move up past pinned line`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-1", "line-2", "line-3"))
            assertThat(viewModel.state.value.pinnedLineIndex).isEqualTo(1)

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 3, toIndex = 2))

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-1", "line-3", "line-2"))
            assertThat(viewModel.state.value.orderedLineIds[viewModel.state.value.pinnedLineIndex!!])
                .isEqualTo(viewModel.state.value.pinnedLineId)
        }

    @Test
    fun `reorder can move down past pinned line`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-1", "line-2", "line-3"))
            assertThat(viewModel.state.value.pinnedLineIndex).isEqualTo(1)

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 0, toIndex = 2))

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-2", "line-1", "line-0", "line-3"))
            assertThat(viewModel.state.value.orderedLineIds[viewModel.state.value.pinnedLineIndex!!])
                .isEqualTo(viewModel.state.value.pinnedLineId)
        }

    @Test
    fun `reorder swap to row above pinned third row keeps pin fixed`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel =
                GameSessionViewModel(
                    gameType = GameType.ORGANIZE_POEM,
                    gamesRepository = OrganizePoemPinAtThirdRowGamesRepository(),
                    userPreferencesRepository = preferences,
                    evaluateGameAnswer = EvaluateGameAnswerUseCase(),
                    applyGameHint = ApplyGameHintUseCase(preferences),
                )
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(viewModel.state.value.pinnedLineId).isEqualTo("line-2")
            assertThat(viewModel.state.value.pinnedLineIndex).isEqualTo(2)
            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-1", "line-2", "line-3"))

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 3, toIndex = 1))

            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-3", "line-2", "line-1"))
            assertThat(viewModel.state.value.orderedLineIds[2]).isEqualTo("line-2")
        }

    @Test
    fun `organize poem hint deducts coins and pins first wrong line`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel = createOrganizePoemViewModel(preferences)

            val balanceBefore = preferences.getCoinBalance()
            viewModel.onAction(GameSessionAction.OnHintClick)

            assertThat(viewModel.state.value.hintUsedThisQuiz).isTrue()
            assertThat(viewModel.state.value.pinnedLineId).isEqualTo("line-1")
            assertThat(viewModel.state.value.pinnedLineIndex).isEqualTo(1)
            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(listOf("line-0", "line-1", "line-2", "line-3"))
            assertThat(preferences.getCoinBalance()).isEqualTo(balanceBefore - GameConstants.HINT_COST)
        }

    @Test
    fun `wrong arrangement increments wrong count immediately`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 0, toIndex = 2))
            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.answerPhase).isEqualTo(QuizAnswerPhase.Wrong)
            assertThat(viewModel.state.value.wrongCount).isEqualTo(1)
            assertThat(viewModel.state.value.noAnswerCount).isEqualTo(0)
        }

    @Test
    fun `skip without reorder counts as no answer for organize poem`() =
        runViewModelTest {
            val preferences = FakeUserPreferencesRepository()
            val viewModel = createOrganizePoemViewModel(preferences)

            val balanceBefore = preferences.getCoinBalance()
            assertThat(viewModel.state.value.hasSelection).isFalse()
            assertThat(viewModel.state.value.canPressPrimaryAction).isTrue()

            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.answerPhase).isEqualTo(QuizAnswerPhase.Wrong)
            assertThat(viewModel.state.value.noAnswerCount).isEqualTo(1)
            assertThat(viewModel.state.value.wrongCount).isEqualTo(0)
            assertThat(viewModel.state.value.orderedLineIds)
                .isEqualTo(
                    (viewModel.state.value.currentQuestion as GameQuestion.OrganizePoem).correctOrder,
                )
            assertThat(viewModel.state.value.isRevealing).isTrue()
            assertThat(viewModel.state.value.canPressPrimaryAction).isTrue()
            assertThat(preferences.getCoinBalance()).isEqualTo(balanceBefore)
            assertThat(viewModel.state.value.sessionScoreDelta).isEqualTo(0)
        }

    @Test
    fun `hasSelection becomes true after reorder for organize poem`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            assertThat(viewModel.state.value.hasSelection).isFalse()
            viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 0, toIndex = 2))
            assertThat(viewModel.state.value.hasSelection).isTrue()
        }

    @Test
    fun `hasSelection becomes true after hint for organize poem`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            assertThat(viewModel.state.value.hasSelection).isFalse()
            viewModel.onAction(GameSessionAction.OnHintClick)
            assertThat(viewModel.state.value.hasSelection).isTrue()
        }

    @Test
    fun `correct arrangement adds score`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            reorderOrganizePoemToCorrect(viewModel)
            viewModel.onAction(GameSessionAction.OnCheckAnswerClick)

            assertThat(viewModel.state.value.answerPhase).isEqualTo(QuizAnswerPhase.Correct)
            assertThat(viewModel.state.value.sessionScoreDelta).isEqualTo(GameType.ORGANIZE_POEM.baseScore)
        }

    @Test
    fun `organize poem primary action is always enabled while answering`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            assertThat(viewModel.state.value.hasSelection).isFalse()
            assertThat(viewModel.state.value.canPressPrimaryAction).isTrue()
        }

    @Test
    fun `correct organize poem answer on last quiz navigates to result summary`() =
        runViewModelTest {
            val viewModel = createOrganizePoemViewModel()

            viewModel.events.test {
                repeat(GameConstants.QUIZ_COUNT) {
                    reorderOrganizePoemToCorrect(viewModel)
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                    viewModel.onAction(GameSessionAction.OnCheckAnswerClick)
                }

                val event = awaitItem()
                assertThat(event).isInstanceOf(GameSessionEvent.NavigateToResult::class)
                assertThat((event as GameSessionEvent.NavigateToResult).summary.correctCount)
                    .isEqualTo(GameConstants.QUIZ_COUNT)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun reorderOrganizePoemToCorrect(viewModel: GameSessionViewModel) {
        viewModel.onAction(GameSessionAction.OnReorderLines(fromIndex = 0, toIndex = 1))
    }

    private fun createCompletePoemViewModel(
        userPreferencesRepository: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
    ): GameSessionViewModel =
        GameSessionViewModel(
            gameType = GameType.COMPLETE_POEM,
            gamesRepository = CompletePoemGamesRepository(),
            userPreferencesRepository = userPreferencesRepository,
            evaluateGameAnswer = EvaluateGameAnswerUseCase(),
            applyGameHint = ApplyGameHintUseCase(userPreferencesRepository),
        )

    private class CompletePoemGamesRepository : GamesRepository {
        override fun createGenerationCache() = GameGenerationCache()

        override suspend fun generateQuestion(
            gameType: GameType,
            sessionSeed: Long,
            quizIndex: Int,
            cache: GameGenerationCache,
        ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(createQuestion())

        override suspend fun generateQuizBatch(
            gameType: GameType,
            seed: Long,
            count: Int,
        ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(List(count) { createQuestion() })

        private fun createQuestion() =
            GameQuestion.CompletePoem(
                rightHemistich = "بیت اول",
                blankedLeftHemistich = "شروع ____ وسط ____ پایان",
                poetName = "حافظ",
                options = listOf("word1", "word2", "wrong1", "wrong2"),
                correctWords = "word1" to "word2",
            )
    }

    private fun createOrganizePoemViewModel(
        userPreferencesRepository: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
    ): GameSessionViewModel =
        GameSessionViewModel(
            gameType = GameType.ORGANIZE_POEM,
            gamesRepository = OrganizePoemGamesRepository(),
            userPreferencesRepository = userPreferencesRepository,
            evaluateGameAnswer = EvaluateGameAnswerUseCase(),
            applyGameHint = ApplyGameHintUseCase(userPreferencesRepository),
        )

    private class OrganizePoemGamesRepository : GamesRepository {
        override fun createGenerationCache() = GameGenerationCache()

        override suspend fun generateQuestion(
            gameType: GameType,
            sessionSeed: Long,
            quizIndex: Int,
            cache: GameGenerationCache,
        ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(createQuestion())

        override suspend fun generateQuizBatch(
            gameType: GameType,
            seed: Long,
            count: Int,
        ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(List(count) { createQuestion() })

        private fun createQuestion(): GameQuestion.OrganizePoem {
            val lines =
                listOf(
                    OrganizeLine("line-0", "First"),
                    OrganizeLine("line-1", "Second"),
                    OrganizeLine("line-2", "Third"),
                    OrganizeLine("line-3", "Fourth"),
                )
            return GameQuestion.OrganizePoem(
                poetName = "حافظ",
                lines = listOf(lines[1], lines[0], lines[2], lines[3]),
                correctOrder = lines.map { it.id },
            )
        }
    }

    private class OrganizePoemPinAtThirdRowGamesRepository : GamesRepository {
        override fun createGenerationCache() = GameGenerationCache()

        override suspend fun generateQuestion(
            gameType: GameType,
            sessionSeed: Long,
            quizIndex: Int,
            cache: GameGenerationCache,
        ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(createQuestion())

        override suspend fun generateQuizBatch(
            gameType: GameType,
            seed: Long,
            count: Int,
        ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(List(count) { createQuestion() })

        private fun createQuestion(): GameQuestion.OrganizePoem {
            val lines =
                listOf(
                    OrganizeLine("line-0", "First"),
                    OrganizeLine("line-1", "Second"),
                    OrganizeLine("line-2", "Third"),
                    OrganizeLine("line-3", "Fourth"),
                )
            return GameQuestion.OrganizePoem(
                poetName = "حافظ",
                lines = listOf(lines[0], lines[2], lines[1], lines[3]),
                correctOrder = lines.map { it.id },
            )
        }
    }

    private class FakeGamesRepository : GamesRepository {
        override fun createGenerationCache() = GameGenerationCache()

        override suspend fun generateQuestion(
            gameType: GameType,
            sessionSeed: Long,
            quizIndex: Int,
            cache: GameGenerationCache,
        ): Result<GameQuestion, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(createQuestion())

        override suspend fun generateQuizBatch(
            gameType: GameType,
            seed: Long,
            count: Int,
        ): Result<List<GameQuestion>, abkabk.azbarkon.core.domain.result.DataError.Local> =
            Result.Success(List(count) { createQuestion() })

        private fun createQuestion() =
            GameQuestion.NextVerse(
                promptLine = "prompt",
                poetName = "حافظ",
                options = listOf("correct", "wrong1", "wrong2", "wrong3"),
                correctIndex = 0,
            )
    }
}
