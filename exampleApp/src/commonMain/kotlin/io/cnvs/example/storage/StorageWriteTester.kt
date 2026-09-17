package io.cnvs.example.storage

/**
 * Real writes to shared storage, so the example app can show what the write storage permission
 * actually buys. There are two of them because the answer differs: the platform reports the
 * permission as granted from Android 10 on, but that covers only the files the app owns, and the
 * only way to see where the line falls is to write on both sides of it.
 */
interface StorageWriteTester {
    /**
     * Writes a file the app owns, the way a modern app is meant to: through the MediaStore on
     * Android 10 and above, and a plain file below that.
     */
    suspend fun writeOwnedFile(): StorageWriteResult

    /**
     * Writes straight into a shared public folder by file path, without going through the
     * MediaStore. This is the write the old WRITE_EXTERNAL_STORAGE permission used to govern.
     */
    suspend fun writeToExternalFolder(): StorageWriteResult

    companion object {
        /** Keeps repeat runs from piling up files with unreadable names. */
        const val OWNED_FILE_NAME = "kmmpermissions-write-test.txt"

        const val EXTERNAL_FILE_NAME = "kmmpermissions-external-test.txt"

        const val FILE_CONTENT = "Written by the KmmPermissions example app."
    }
}
