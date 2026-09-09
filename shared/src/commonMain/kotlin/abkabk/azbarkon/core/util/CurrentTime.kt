package abkabk.azbarkon.core.util

import kotlin.time.Clock

fun currentTimeMillis(): Long =
    Clock.System.now().toEpochMilliseconds()
