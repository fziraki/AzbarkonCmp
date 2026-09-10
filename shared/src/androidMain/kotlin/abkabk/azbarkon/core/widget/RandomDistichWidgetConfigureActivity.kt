package abkabk.azbarkon.core.widget

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.Poet
import abkabk.azbarkon.domain.repository.PoetRepository
import abkabk.azbarkon.shared.R
import abkabk.azbarkon.ui.theme.SarvTheme
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RandomDistichWidgetConfigureActivity :
    ComponentActivity(),
    KoinComponent {
    private val poetRepository: PoetRepository by inject()
    private val preferences: RandomDistichWidgetPreferences by inject()
    private val updater: RandomDistichWidgetUpdater by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)
        appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            SarvTheme {
                RandomDistichWidgetConfigureScreen(
                    loadPoets = poetRepository::getPoets,
                    onPoetSelect = ::onPoetSelected,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun onPoetSelected(poetId: Int) {
        preferences.savePoetId(appWidgetId, poetId)

        scope.launch {
            updater.update(
                context = this@RandomDistichWidgetConfigureActivity,
                appWidgetManager = AppWidgetManager.getInstance(this@RandomDistichWidgetConfigureActivity),
                appWidgetId = appWidgetId,
            )

            val resultIntent =
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

@Composable
private fun RandomDistichWidgetConfigureScreen(
    loadPoets: suspend () -> Result<List<Poet>, DataError.Local>,
    onPoetSelect: (Int) -> Unit,
) {
    var poets by remember { mutableStateOf<List<Poet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentLoadPoets by rememberUpdatedState(loadPoets)

    LaunchedEffect(Unit) {
        when (val result = currentLoadPoets()) {
            is Result.Success -> {
                poets = result.data.filter { it.isDownloaded }
                isLoading = false
            }
            is Result.Error -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                PoetSelectionList(
                    poets = poets,
                    onPoetSelect = onPoetSelect,
                    padding = padding,
                )
            }
        }
    }
}

@Composable
private fun PoetSelectionList(
    poets: List<Poet>,
    onPoetSelect: (Int) -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
    ) {
        item {
            Text(
                text = stringResource(R.string.widget_random_distich_config_title),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            )
        }
        item {
            PoetConfigRow(
                name = stringResource(R.string.widget_random_distich_all_poets),
                onClick = { onPoetSelect(RandomDistichWidgetConstants.ALL_POETS_ID) },
            )
        }
        items(poets.filter { it.id != null && it.name != null }, key = { it.id!! }) { poet ->
            PoetConfigRow(
                name = poet.name!!,
                onClick = { onPoetSelect(poet.id!!) },
            )
        }
    }
}

@Composable
private fun PoetConfigRow(
    name: String,
    onClick: () -> Unit,
) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
