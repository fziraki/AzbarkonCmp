package abkabk.azbarkon

import abkabk.azbarkon.core.notifications.DailyDistichNotificationPayload
import abkabk.azbarkon.core.notifications.MemorizationReviewNotificationPayload
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private var initialPoemId by mutableIntStateOf(-1)
    private var openMemorizationPractice by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        readNavigationIntent(intent)

        setContent {
            App(
                initialPoemId = initialPoemId.takeIf { it >= 0 },
                openMemorizationPractice = openMemorizationPractice,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readNavigationIntent(intent)
    }

    private fun readNavigationIntent(intent: Intent?) {
        initialPoemId = intent?.getIntExtra(DailyDistichNotificationPayload.KEY_POEM_ID, -1) ?: -1
        openMemorizationPractice =
            intent?.getBooleanExtra(
                MemorizationReviewNotificationPayload.KEY_OPEN_MEMORIZATION_PRACTICE,
                false,
            ) ?: false
    }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}
