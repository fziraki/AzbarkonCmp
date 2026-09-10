package abkabk.azbarkon.features.chat

import androidx.compose.runtime.Stable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Stable
data class ChatMessageUi(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timeLabel: String,
)

fun formatChatTimeLabel(
    epochMillis: Long = Clock.System.now().toEpochMilliseconds()
): String {
    val dateTime = Instant
        .fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.hour.toString().padStart(2, '0')}:" +
            dateTime.minute.toString().padStart(2, '0')
}
