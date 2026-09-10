package abkabk.azbarkon.core.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentLocalDateSeed(): Long {
    val components =
        NSCalendar.currentCalendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = NSDate(),
        )
    val year = components.year.toLong()
    val month = components.month.toLong()
    val day = components.day.toLong()
    return year * 10_000L + month * 100L + day
}
