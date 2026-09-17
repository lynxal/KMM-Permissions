package io.cnvs.example.storage

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataUsingEncoding

/**
 * Writes the test file into the app's Documents directory. iOS asks for no permission there, since
 * the sandbox is the app's own, which is why the library reports WRITE_STORAGE as granted on this
 * platform. This write is what shows the report is true.
 *
 * The external folder write has no iOS equivalent: there is no shared filesystem outside the
 * sandbox, so the attempt is made against a path outside it and the refusal is the answer.
 */
class IosStorageWriteTester : StorageWriteTester {
    private companion object {
        /** Outside the app sandbox on every iOS device; the write is meant to be refused. */
        const val OUTSIDE_SANDBOX_DIRECTORY = "/private/var/mobile/Media"
    }

    override suspend fun writeOwnedFile(): StorageWriteResult {
        val documents = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        ).firstOrNull() as? String
            ?: return StorageWriteResult("iOS gave no Documents directory to write to", false)

        return write("$documents/${StorageWriteTester.OWNED_FILE_NAME}")
    }

    override suspend fun writeToExternalFolder(): StorageWriteResult {
        val result =
            write("$OUTSIDE_SANDBOX_DIRECTORY/${StorageWriteTester.EXTERNAL_FILE_NAME}")

        return if (result.wrote == true) result
        else StorageWriteResult(
            "Refused, as expected: iOS has no shared folder outside the app sandbox",
            false
        )
    }

    private fun write(path: String): StorageWriteResult {
        // The Kotlin/Native way to reach NSString's methods from a Kotlin String.
        @Suppress("CAST_NEVER_SUCCEEDS")
        val contents = (StorageWriteTester.FILE_CONTENT as NSString)
            .dataUsingEncoding(NSUTF8StringEncoding)
            ?: return StorageWriteResult("Could not encode the file contents", null)

        val wrote = NSFileManager.defaultManager.createFileAtPath(
            path = path,
            contents = contents,
            attributes = null
        )

        return if (wrote) StorageWriteResult("Wrote $path", true)
        else StorageWriteResult("iOS refused the write at $path", false)
    }
}
