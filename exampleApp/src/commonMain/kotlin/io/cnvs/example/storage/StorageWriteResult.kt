package io.cnvs.example.storage

/**
 * What one write attempt did. [wrote] is the app's own evidence about storage access: true when the
 * file landed, false when the platform refused it, and null when the attempt was inconclusive.
 */
data class StorageWriteResult(
    val message: String,
    val wrote: Boolean?,
)
