package abkabk.azbarkon.core.util

private const val MILLIS_PER_DAY = 86_400_000L

fun dayKeyFromMillis(millis: Long): Int = ((millis) / MILLIS_PER_DAY).toInt()

fun nextVisitStreak(
    currentStreak: Int,
    lastPlayDayKey: Int?,
    playDayKey: Int,
): Int =
    when (lastPlayDayKey) {
        null -> 1
        playDayKey -> currentStreak
        playDayKey - 1 -> currentStreak + 1
        else -> 1
    }
