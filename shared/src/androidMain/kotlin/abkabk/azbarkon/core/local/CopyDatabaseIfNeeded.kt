package abkabk.azbarkon.core.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.sarv.db.SarvDatabase
import io.github.aakira.napier.Napier
import java.io.File
import java.util.zip.ZipInputStream

internal const val DATABASE_NAME = "ganjoor.s3db"

// ponytail: no published users and no future db updates, so any version/schema mismatch is
// destructive like Room's fallback: delete the local db and re-copy the bundled asset
fun copyDatabaseIfNeeded(context: Context) {
    val dbFile = context.getDatabasePath(DATABASE_NAME)

    val exists = dbFile.exists()
    val hasIndex = exists && hasExpectedSchema(dbFile)
    val version = if (exists) databaseVersion(dbFile) else 0
    val copy = !exists || !hasIndex || version != SarvDatabase.Schema.version.toInt()

    if (copy) {
        dbFile.delete()
        copyBundledDatabase(context, dbFile)
    }
    syncBundledDatabaseVersion(dbFile.path)
    Napier.d(
        message = "db exists=$exists fts=$hasIndex version=$version copy=$copy -> ${dbFile.path}",
        tag = "PoetDb",
    )
}

private fun hasExpectedSchema(dbFile: File): Boolean =
    try {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery(
                """
                SELECT COUNT(*) FROM sqlite_master
                WHERE type = 'table' AND name IN ('verse_fts4', 'poet_meta')
                """.trimIndent(),
                null,
            ).use { it.moveToFirst(); it.getInt(0) == 2 }
        }
    } catch (e: SQLiteException) {
        Napier.e("hasExpectedSchema failed", e)
        false
    }

private fun databaseVersion(dbFile: File): Int =
    try {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { it.version }
    } catch (e: SQLiteException) {
        Napier.e("databaseVersion failed", e)
        0
    }

private fun copyBundledDatabase(context: Context, dbFile: File) {
    dbFile.parentFile?.mkdirs()
    context.assets.open("$DATABASE_NAME.zip").use { input ->
        extractFirstEntry(ZipInputStream(input), dbFile)
    }
}

private fun extractFirstEntry(zip: ZipInputStream, dbFile: File) {
    zip.nextEntry ?: return
    dbFile.outputStream().use { zip.copyTo(it) }
}

private fun syncBundledDatabaseVersion(dbPath: String) {
    val db =
        SQLiteDatabase.openDatabase(
            dbPath,
            null,
            SQLiteDatabase.OPEN_READWRITE,
        )
    try {
        if (db.version >= SarvDatabase.Schema.version.toInt()) return

        val hasPoetTable =
            db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'poet' LIMIT 1",
                null,
            ).use { it.moveToFirst() }

        if (hasPoetTable) {
            db.version = SarvDatabase.Schema.version.toInt()
        }
    } finally {
        db.close()
    }
}
