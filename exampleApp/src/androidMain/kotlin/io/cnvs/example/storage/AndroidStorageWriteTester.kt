package io.cnvs.example.storage

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Writes a small text file two ways, because the write storage permission covers only one of them
 * on a modern device. Which API does the writing is the point of the test: from Android 10 on the
 * MediaStore insert needs no permission and the file is owned by the app, while an older device
 * writes a plain file and is refused without WRITE_EXTERNAL_STORAGE.
 */
class AndroidStorageWriteTester(private val appContext: Context) : StorageWriteTester {
    override suspend fun writeOwnedFile(): StorageWriteResult = runWrite {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) writeThroughMediaStore()
        else writeToPublicFolder(StorageWriteTester.OWNED_FILE_NAME)
    }

    override suspend fun writeToExternalFolder(): StorageWriteResult = runWrite {
        writeToPublicFolder(StorageWriteTester.EXTERNAL_FILE_NAME)
    }

    private suspend fun runWrite(write: () -> StorageWriteResult): StorageWriteResult =
        withContext(Dispatchers.IO) {
            try {
                write()
            } catch (e: SecurityException) {
                StorageWriteResult("Storage write refused: ${e.message}", false)
            } catch (e: Exception) {
                StorageWriteResult("Storage write failed: ${e.message}", null)
            }
        }

    private fun writeThroughMediaStore(): StorageWriteResult {
        val downloads = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val resolver = appContext.contentResolver
        val entry = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, StorageWriteTester.OWNED_FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
        }

        // An insert with a display name already on disk does not replace that file — the
        // MediaStore renames the new one to "name (1).txt" and keeps going. Clearing the old rows
        // first is what keeps repeat runs from piling up copies. Deleting by a display-name
        // selection does not work here, so each row is looked up and deleted by its own id.
        deletePreviousCopies(resolver, downloads)

        val target = resolver.insert(downloads, entry)
            ?: return StorageWriteResult("The MediaStore refused to create the file", false)

        resolver.openOutputStream(target).use { stream ->
            stream ?: return StorageWriteResult("The MediaStore gave no stream to write to", false)
            stream.write(StorageWriteTester.FILE_CONTENT.encodeToByteArray())
        }

        // The name that was asked for and the name on disk can differ, so the report reads the
        // name back rather than repeating the request.
        return StorageWriteResult(
            "Wrote ${displayNameOf(resolver, target)} to Downloads through the MediaStore",
            true
        )
    }

    /** Removes every Downloads row this app owns whose name came from an earlier run. */
    private fun deletePreviousCopies(resolver: ContentResolver, downloads: Uri) {
        val baseName = StorageWriteTester.OWNED_FILE_NAME.substringBeforeLast(".")

        resolver.query(
            downloads,
            arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                if (!cursor.getString(nameColumn).startsWith(baseName)) continue

                val row = ContentUris.withAppendedId(downloads, cursor.getLong(idColumn))
                runCatching { resolver.delete(row, null, null) }
            }
        }
    }

    private fun displayNameOf(resolver: ContentResolver, target: Uri): String =
        resolver.query(target, arrayOf(MediaStore.Downloads.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
            ?: StorageWriteTester.OWNED_FILE_NAME

    private fun writeToPublicFolder(fileName: String): StorageWriteResult {
        @Suppress("DEPRECATION")
        val downloads =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloads.mkdirs()

        val target = File(downloads, fileName)

        // Without the permission this path fails with a plain IOException carrying "Permission
        // denied", not a SecurityException, so the refusal has to be recognised here. On this
        // branch a failed write is the permission answer: the folder is public and the file is the
        // app's own, so nothing else explains it.
        return try {
            target.writeText(StorageWriteTester.FILE_CONTENT)
            StorageWriteResult("Wrote ${target.absolutePath}", true)
        } catch (e: IOException) {
            StorageWriteResult("Storage write refused: ${e.message}", false)
        }
    }
}
