package abkabk.azbarkon.data.mapper

import abkabk.azbarkon.domain.model.RandomDistich
import com.sarv.db.SelectRandomDistichAtOffsetByPoet

fun SelectRandomDistichAtOffsetByPoet.toRandomDistich(): RandomDistich =
    RandomDistich(
        poemId = poem_id.toInt(),
        vorder = vorder.toInt(),
        rightText = right_text,
        leftText = left_text,
        poetName = poet_name,
    )
