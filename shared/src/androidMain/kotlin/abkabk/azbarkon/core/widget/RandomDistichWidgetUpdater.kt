package abkabk.azbarkon.core.widget

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.notifications.DailyDistichNotificationPayload
import abkabk.azbarkon.domain.model.RandomDistich
import abkabk.azbarkon.domain.repository.DailyDistichRepository
import abkabk.azbarkon.shared.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.view.View
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RandomDistichWidgetUpdater :
    KoinComponent {
    private val dailyDistichRepository: DailyDistichRepository by inject()
    private val preferences: RandomDistichWidgetPreferences by inject()

    private companion object {
        const val COPYrequestCode = 10_000
    }

    suspend fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        source: WidgetDistichSource = WidgetDistichSource.Daily,
    ) {
        val poetId = preferences.getPoetId(appWidgetId)
        val seed =
            when (source) {
                WidgetDistichSource.Daily -> dailyDistichSeed(poetId)
                WidgetDistichSource.Random -> randomDistichSeed(appWidgetId)
            }
        val views = buildRemoteViews(context, appWidgetId, distich = null)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        when (val result = dailyDistichRepository.getRandomDistich(seed = seed, poetId = poetId)) {
            is Result.Success -> {
                val successViews =
                    buildRemoteViews(
                        context = context,
                        appWidgetId = appWidgetId,
                        distich = result.data,
                    )
                appWidgetManager.updateAppWidget(appWidgetId, successViews)
            }
            is Result.Error -> {
                views.setViewVisibility(R.id.widget_loading, View.VISIBLE)
                views.setViewVisibility(R.id.widget_content, View.GONE)
                views.setViewVisibility(R.id.widget_poet_name, View.GONE)
                views.setTextViewText(R.id.widget_loading, context.getString(R.string.widget_random_distich_error))
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        appWidgetId: Int,
        distich: RandomDistich?,
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_random_distich).apply {
            setTextViewText(R.id.widget_title, context.getString(R.string.widget_random_distich_title))

            if (distich == null) {
                setViewVisibility(R.id.widget_loading, View.VISIBLE)
                setViewVisibility(R.id.widget_content, View.GONE)
                setViewVisibility(R.id.widget_poet_name, View.GONE)
                setTextViewText(R.id.widget_loading, context.getString(R.string.widget_random_distich_loading))
            } else {
                setViewVisibility(R.id.widget_loading, View.GONE)
                setViewVisibility(R.id.widget_content, View.VISIBLE)
                setViewVisibility(R.id.widget_poet_name, View.VISIBLE)
                setTextViewText(R.id.widget_right_line, distich.rightText)
                setTextViewText(R.id.widget_left_line, distich.leftText)
                setTextViewText(R.id.widget_poet_name, distich.poetName)
            }

            setOnClickPendingIntent(
                R.id.widget_refresh,
                refreshPendingIntent(context, appWidgetId),
            )

            if (distich != null) {
                setOnClickPendingIntent(
                    R.id.widget_copy,
                    copyPendingIntent(context, appWidgetId, distich),
                )
                setOnClickPendingIntent(
                    R.id.widget_content,
                    openPoemPendingIntent(context, distich.poemId),
                )
                setOnClickPendingIntent(
                    R.id.widget_poet_name,
                    openPoemPendingIntent(context, distich.poemId),
                )
            }
        }

    private fun copyPendingIntent(
        context: Context,
        appWidgetId: Int,
        distich: RandomDistich,
    ): PendingIntent {
        val intent =
            Intent(context, RandomDistichWidgetProvider::class.java).apply {
                action = RandomDistichWidgetConstants.ACTION_COPY
                putExtra(RandomDistichWidgetConstants.EXTRA_APP_WIDGET_ID, appWidgetId)
                putExtra(RandomDistichWidgetConstants.EXTRA_COPY_TEXT, "${distich.rightText}\n${distich.leftText}\n${distich.poetName}")
            }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId + COPYrequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun refreshPendingIntent(
        context: Context,
        appWidgetId: Int,
    ): PendingIntent {
        val intent =
            Intent(context, RandomDistichWidgetProvider::class.java).apply {
                action = RandomDistichWidgetConstants.ACTION_REFRESH
                putExtra(RandomDistichWidgetConstants.EXTRA_APP_WIDGET_ID, appWidgetId)
            }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openPoemPendingIntent(
        context: Context,
        poemId: Int,
    ): PendingIntent {
        val intent =
            Intent().apply {
                setClassName(context.packageName, RandomDistichWidgetConstants.MAIN_ACTIVITY_CLASS)
                putExtra(DailyDistichNotificationPayload.KEY_POEM_ID, poemId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            poemId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
