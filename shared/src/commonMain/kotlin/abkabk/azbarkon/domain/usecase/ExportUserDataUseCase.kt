package abkabk.azbarkon.domain.usecase

import abkabk.azbarkon.data.backup.UserBackupManager
import abkabk.azbarkon.domain.platform.ShareService
import io.github.aakira.napier.Napier

class ExportUserDataUseCase(
    private val userBackupManager: UserBackupManager,
    private val shareService: ShareService,
) {
    suspend operator fun invoke(): Boolean {
        return try {
            val json = userBackupManager.exportJson()
            shareService.shareFile(
                bytes = json.encodeToByteArray(),
                fileName = "sarv-backup.json",
                mimeType = "application/json",
                title = null,
            )
            true
        } catch (e: IllegalStateException) {
            Napier.e("ExportUserDataUseCase failed", e)
            false
        }
    }
}
