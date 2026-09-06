package abkabk.azbarkon.data.local

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.dbQuery
import abkabk.azbarkon.data.generator.GameQuestionGenerator
import abkabk.azbarkon.data.generator.GameSessionPoolBuilder
import abkabk.azbarkon.data.generator.VerseRow
import abkabk.azbarkon.data.mapper.toPoet
import abkabk.azbarkon.domain.datasource.GamesLocalDataSource
import abkabk.azbarkon.domain.model.Poet
import abkabk.azbarkon.domain.model.games.GameConstants
import abkabk.azbarkon.domain.model.games.GameDistichCandidate
import abkabk.azbarkon.domain.model.games.GameGenerationCache
import abkabk.azbarkon.domain.model.games.GamePoemBundle
import abkabk.azbarkon.domain.model.games.GameQuestion
import abkabk.azbarkon.domain.model.games.GameType
import com.sarv.db.PoemQueries
import com.sarv.db.PoetQueries
import com.sarv.db.VerseQueries
import kotlin.random.Random

private const val SEED_STRIDE = 9973L
private const val SEED_SALT = 17L
private const val DISTRACTOR_COUNT = 3

class SqlDelightGamesLocalDataSource(
    private val verseQueries: VerseQueries,
    private val poemQueries: PoemQueries,
    private val poetQueries: PoetQueries,
) : GamesLocalDataSource {
    override suspend fun getAllPoets(): Result<List<Poet>, DataError.Local> =
        dbQuery {
            poetQueries
                .selectAllWithCatUrl()
                .executeAsList()
                .map { it.toPoet() }
        }

    override suspend fun buildPoemBundle(
        gameType: GameType,
        quizIndex: Int,
        cache: GameGenerationCache,
    ): Result<Unit, DataError.Local> {
        if (cache.hasBundle(quizIndex)) return Result.Success(Unit)

        return dbQuery {
            repeat(GameConstants.MAX_POEM_FETCH_ATTEMPTS) {
                val poemId =
                    verseQueries
                        .selectRandomGamePoemId()
                        .executeAsOneOrNull()
                        ?: return@repeat

                if (poemId in cache.usedPoemIds) return@repeat

                val poetInfo =
                    poemQueries
                        .selectPoetForPoemId(poemId)
                        .executeAsOneOrNull()
                        ?: return@repeat

                if (!cache.isPoetAvailable(poetInfo.poet_id)) return@repeat

                val verses =
                    verseQueries
                        .selectByPoemId(poemId)
                        .executeAsList()
                        .map { verse ->
                            VerseRow(
                                vorder = verse.vorder,
                                position = verse.position,
                                text = verse.text,
                            )
                        }

                val extraction =
                    GameSessionPoolBuilder.extractFromVerses(
                        poemId = poemId,
                        poetId = poetInfo.poet_id,
                        poetName = poetInfo.poet_name,
                        verses = verses,
                    )

                if (
                    !GameSessionPoolBuilder.isPoemAcceptable(
                        gameType = gameType,
                        distichCount = extraction.distichs.size,
                        organizeCount = extraction.organizeWindows.size,
                        totalVerses = verses.groupBy { it.vorder }.size,
                    )
                ) {
                    return@repeat
                }

                cache.poemBundles[quizIndex] =
                    GamePoemBundle(
                        poemId = poemId,
                        poetId = poetInfo.poet_id,
                        poetName = poetInfo.poet_name,
                        distichs = extraction.distichs,
                        organizeWindows = extraction.organizeWindows,
                        poemWords = extraction.poemWords,
                    )
                cache.usedPoemIds += poemId
                cache.recordPoetUse(poetInfo.poet_id)
                return Result.Success(Unit)
            }
            Result.Error(DataError.Local.NOT_FOUND)
        }
    }

    override suspend fun generateQuestion(
        gameType: GameType,
        quizIndex: Int,
        seed: Long,
        cache: GameGenerationCache,
    ): Result<GameQuestion, DataError.Local> {
        repeat(MAX_GENERATION_ATTEMPTS) { attempt ->
            val attemptSeed = seed + attempt * SEED_STRIDE
            val question =
                when (gameType) {
                    GameType.NEXT_VERSE -> generateNextVerse(attemptSeed, quizIndex, cache)
                    GameType.FIND_POET -> generateFindPoet(attemptSeed, quizIndex, cache)
                    GameType.COMPLETE_POEM -> generateCompletePoem(attemptSeed, quizIndex, cache)
                    GameType.ORGANIZE_POEM -> generateOrganizePoem(attemptSeed, quizIndex, cache)
                }
            if (question != null) {
                return Result.Success(question)
            }
        }
        return Result.Error(DataError.Local.NOT_FOUND)
    }

    private fun generateNextVerse(
        seed: Long,
        quizIndex: Int,
        cache: GameGenerationCache,
    ): GameQuestion.NextVerse? {
        val bundle = cache.poemBundles[quizIndex] ?: return null
        if (bundle.distichs.isEmpty()) return null

        val distich = bundle.distichs[bundleIndex(seed, bundle.distichs.size)]
        val distractors = pickBundleDistractors(seed, bundle, distich) ?: return null

        return GameQuestionGenerator.buildNextVerseQuestion(
            promptLine = distich.firstHemistich,
            poetName = distich.poetName,
            correctAnswer = distich.secondHemistich,
            distractors = distractors,
            seed = seed,
        )
    }

    private fun generateFindPoet(
        seed: Long,
        quizIndex: Int,
        cache: GameGenerationCache,
    ): GameQuestion.FindPoet? {
        val distich =
            cache.poemBundles[quizIndex]?.let { bundle ->
                bundle.distichs.getOrNull(bundleIndex(seed, bundle.distichs.size))
            } ?: return null
        val allPoets = cache.cachedPoets ?: return null
        val correctPoet = allPoets.firstOrNull { it.id?.toLong() == distich.poetId } ?: return null

        return GameQuestionGenerator.buildFindPoetQuestion(
            rightHemistich = distich.firstHemistich,
            leftHemistich = distich.secondHemistich,
            correctPoet = correctPoet,
            allPoets = allPoets,
            seed = seed,
        )
    }

    private fun generateCompletePoem(
        seed: Long,
        quizIndex: Int,
        cache: GameGenerationCache,
    ): GameQuestion.CompletePoem? {
        val bundle = cache.poemBundles[quizIndex] ?: return null
        if (bundle.distichs.isEmpty()) return null

        val distich = bundle.distichs[bundleIndex(seed, bundle.distichs.size)]

        return GameQuestionGenerator.buildCompletePoemQuestion(
            rightHemistich = distich.firstHemistich,
            leftHemistich = distich.secondHemistich,
            poetName = bundle.poetName,
            poemWords = bundle.poemWords,
            seed = seed,
        )
    }

    private fun generateOrganizePoem(
        seed: Long,
        quizIndex: Int,
        cache: GameGenerationCache,
    ): GameQuestion.OrganizePoem? {
        val bundle = cache.poemBundles[quizIndex] ?: return null
        if (bundle.organizeWindows.isEmpty()) return null

        val window = bundle.organizeWindows[bundleIndex(seed, bundle.organizeWindows.size)]

        return GameQuestionGenerator.buildOrganizePoemQuestion(
            poemId = window.poemId,
            startVorder = window.startVorder,
            poetName = bundle.poetName,
            lines = window.lines,
            seed = seed,
        )
    }

    private fun pickBundleDistractors(
        seed: Long,
        bundle: GamePoemBundle,
        distich: GameDistichCandidate,
    ): List<String>? {
        val candidates =
            bundle.distichs
                .flatMap { listOf(it.firstHemistich, it.secondHemistich) }
                .filter { text ->
                    text.isNotBlank() &&
                        text != distich.secondHemistich &&
                        text != distich.firstHemistich
                }.distinct()

        if (candidates.size < DISTRACTOR_COUNT) return null

        return candidates
            .shuffled(Random(seed + SEED_SALT))
            .take(DISTRACTOR_COUNT)
    }

    private fun bundleIndex(
        seed: Long,
        size: Int,
    ): Int = ((seed and Long.MAX_VALUE) % size).toInt()

    private companion object {
        const val MAX_GENERATION_ATTEMPTS = 8
    }
}
