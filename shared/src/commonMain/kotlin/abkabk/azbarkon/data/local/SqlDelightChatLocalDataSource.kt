package abkabk.azbarkon.data.local

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.dbQuery
import abkabk.azbarkon.domain.datasource.ChatLocalDataSource
import abkabk.azbarkon.domain.model.ChatDistich
import abkabk.azbarkon.domain.model.ChatDistichFallback
import com.sarv.db.VerseQueries

class SqlDelightChatLocalDataSource(
    private val verseQueries: VerseQueries,
) : ChatLocalDataSource {
    override suspend fun findDistichByPrefix(
        poetId: Int,
        prefix: String,
    ): Result<ChatDistich, DataError.Local> =
        dbQuery {
            val firstHemistich =
                verseQueries
                    .selectChatDistichByPoetAndPrefix(
                        poet_id = poetId.toLong(),
                        prefix = prefix,
                    ).executeAsOneOrNull()

            if (firstHemistich == null) {
                ChatDistich(
                    poemId = ChatDistichFallback.POEM_ID,
                    rightText = ChatDistichFallback.RIGHT_TEXT,
                    leftText = ChatDistichFallback.LEFT_TEXT,
                )
            } else {
                val secondHemistich =
                    verseQueries
                        .selectVerseTextByPoemVorderPosition(
                            poem_id = firstHemistich.poem_id,
                            vorder = firstHemistich.vorder + 1,
                            position = 1,
                        ).executeAsOneOrNull()
                        ?: return Result.Error(DataError.Local.NOT_FOUND)

                ChatDistich(
                    poemId = firstHemistich.poem_id.toInt(),
                    rightText = firstHemistich.right_text,
                    leftText = secondHemistich,
                )
            }
        }
}
