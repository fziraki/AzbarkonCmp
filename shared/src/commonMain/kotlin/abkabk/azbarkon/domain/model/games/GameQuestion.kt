package abkabk.azbarkon.domain.model.games

data class PoetOption(
    val id: Int,
    val name: String,
    val imageUrl: String?,
)

data class OrganizeLine(
    val id: String,
    val text: String,
)

sealed interface GameQuestion {
    data class NextVerse(
        val promptLine: String,
        val poetName: String,
        val options: List<String>,
        val correctIndex: Int,
    ) : GameQuestion

    data class FindPoet(
        val rightHemistich: String,
        val leftHemistich: String,
        val options: List<PoetOption>,
        val correctPoetId: Int,
    ) : GameQuestion

    data class CompletePoem(
        val rightHemistich: String,
        val blankedLeftHemistich: String,
        val poetName: String,
        val options: List<String>,
        val correctWords: Pair<String, String>,
    ) : GameQuestion

    data class OrganizePoem(
        val poetName: String,
        val lines: List<OrganizeLine>,
        val correctOrder: List<String>,
    ) : GameQuestion
}

data class GameSessionSummary(
    val correctCount: Int,
    val wrongCount: Int,
    val noAnswerCount: Int,
    val scoreDelta: Int,
)
