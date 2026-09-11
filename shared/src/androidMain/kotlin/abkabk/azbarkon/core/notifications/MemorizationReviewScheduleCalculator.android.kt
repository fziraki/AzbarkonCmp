package abkabk.azbarkon.core.notifications

import java.util.Calendar
internal object MemorizationReviewScheduleCalculator {
    fun initialDelayMillis(
        deliveryHour: Int,
        deliveryMinute: Int,
    ): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, deliveryHour)
            set(Calendar.MINUTE, deliveryMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return next.timeInMillis - now.timeInMillis
    }

    const val PERIODIC_INTERVAL_HOURS: Long = 24
}
