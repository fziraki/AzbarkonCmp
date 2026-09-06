package abkabk.azbarkon.core.util

import java.util.Calendar

actual fun currentLocalDateSeed(): Long {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) * YEAR_SEED_SCALE +
        (calendar.get(Calendar.MONTH) + 1) * MONTH_SEED_SCALE +
        calendar.get(Calendar.DAY_OF_MONTH)
}

private const val YEAR_SEED_SCALE = 10_000L
private const val MONTH_SEED_SCALE = 100L
