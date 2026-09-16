package io.cnvs.example.localnetwork

/**
 * What one discovery run saw. [reachedNetwork] is the app's own evidence about local network
 * access: true when services answered, false when the platform blocked the discovery, and null
 * when the run was inconclusive — a quiet network looks the same as a blocked one on its own.
 */
data class LocalNetworkDiscovery(
    val message: String,
    val reachedNetwork: Boolean?,
)
