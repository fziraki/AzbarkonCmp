package abkabk.azbarkon.core.widget

object RandomDistichWidgetConstants {
    const val PREFS_NAME = "random_distich_widget"
    const val ACTION_REFRESH = "abkabk.azbarkon.action.WIDGET_REFRESH_DISTICH"
    const val ACTION_COPY = "abkabk.azbarkon.action.WIDGET_COPY_DISTICH"
    const val EXTRA_APP_WIDGET_ID = "extra_app_widget_id"
    const val EXTRA_COPY_TEXT = "extra_copy_text"
    const val ALL_POETS_ID = 0
    const val MAIN_ACTIVITY_CLASS = "abkabk.azbarkon.MainActivity"

    fun poetIdKey(appWidgetId: Int): String = "poet_id_$appWidgetId"
}
