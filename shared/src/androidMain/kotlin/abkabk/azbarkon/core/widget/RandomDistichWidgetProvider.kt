package abkabk.azbarkon.core.widget

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import abkabk.azbarkon.shared.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RandomDistichWidgetProvider :
    android.appwidget.AppWidgetProvider(),
    KoinComponent {
    private val updater: RandomDistichWidgetUpdater by inject()
    private val preferences: RandomDistichWidgetPreferences by inject()
    private val refresher: RandomDistichWidgetRefresher by inject()
    private val ioDispatcher: CoroutineDispatcher by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updater.update(
                        context = context,
                        appWidgetManager = appWidgetManager,
                        appWidgetId = appWidgetId,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach(preferences::clear)
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            RandomDistichWidgetConstants.ACTION_COPY -> {
                val text = intent.getStringExtra(RandomDistichWidgetConstants.EXTRA_COPY_TEXT) ?: return
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("distich", text))
                Toast.makeText(context, context.getString(R.string.widget_copied_toast), Toast.LENGTH_SHORT).show()
            }
            RandomDistichWidgetConstants.ACTION_REFRESH -> {
                val appWidgetId =
                    intent.getIntExtra(
                        RandomDistichWidgetConstants.EXTRA_APP_WIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID,
                    )
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

                val pendingResult = goAsync()
                CoroutineScope(ioDispatcher).launch {
                    try {
                        updater.update(
                            context = context,
                            appWidgetManager = AppWidgetManager.getInstance(context),
                            appWidgetId = appWidgetId,
                            source = WidgetDistichSource.Random,
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> {
                val pendingResult = goAsync()
                CoroutineScope(ioDispatcher).launch {
                    try {
                        refresher.updateAllWidgets(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
