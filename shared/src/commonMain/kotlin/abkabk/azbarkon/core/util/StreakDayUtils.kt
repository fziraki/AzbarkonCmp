package abkabk.azbarkon.core.util

private const val MILLIS_PER_DAY = 86_400_000L

fun dayKeyFromMillis(millis: Long): Int = ((millis) / MILLIS_PER_DAY).toInt()

fun consecutiveDayStreak(
    reviewDayKeys: List<Int>,
    todayDayKey: Int = dayKeyFromMillis(currentTimeMillis()),
): Int {
    if (reviewDayKeys.isEmpty()) return 0

    val distinctDescending = reviewDayKeys.distinct().sortedDescending()
    var streak = 0
    var expectedDay = todayDayKey

    for (day in distinctDescending) {
        if (day == expectedDay) {
            streak++
            expectedDay--
        } else if (day < expectedDay) {
            break
        }
    }

    return streak
}

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
