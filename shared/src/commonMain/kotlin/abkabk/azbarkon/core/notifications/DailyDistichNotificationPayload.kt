package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.domain.model.RandomDistich

object DailyDistichNotificationPayload {
    const val KEY_POET_NAME = "poet_name"
    const val KEY_RIGHT_TEXT = "right_text"
    const val KEY_LEFT_TEXT = "left_text"
    const val KEY_POEM_ID = "poem_id"
    const val KEY_VORDER = "vorder"
    const val NOTIFICATION_ID = 1001
    const val PREVIEW_NOTIFICATION_ID = 1002
    const val CHANNEL_ID = "daily_distich"
    const val WORK_NAME = "daily_distich_notification"
    const val REQUEST_ID = "daily_distich"
    const val IMMEDIATE_REQUEST_ID = "daily_distich_immediate"

    fun RandomDistich.toPayloadMap(): Map<String, String> =
        mapOf(
            KEY_POET_NAME to poetName,
            KEY_RIGHT_TEXT to rightText,
            KEY_LEFT_TEXT to leftText,
            KEY_POEM_ID to poemId.toString(),
            KEY_VORDER to vorder.toString(),
        )
}
