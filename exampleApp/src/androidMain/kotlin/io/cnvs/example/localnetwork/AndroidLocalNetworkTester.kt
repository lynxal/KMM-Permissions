package io.cnvs.example.localnetwork

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Discovers services with NsdManager, the framework mDNS client Android gates behind the local
 * network permission.
 */
class AndroidLocalNetworkTester(private val appContext: Context) : LocalNetworkTester {
    private companion object {
        val DISCOVERY_WINDOW = 4.seconds
    }

    override suspend fun discoverServices(): LocalNetworkDiscovery = coroutineScope {
        val discoveryManager = appContext.getSystemService(Context.NSD_SERVICE) as NsdManager

        val found = LocalNetworkTester.SERVICE_TYPES
            .map { serviceType -> async { serviceType to discover(discoveryManager, serviceType) } }
            .awaitAll()

        val failure = found.firstNotNullOfOrNull { (_, outcome) -> outcome.failure }
        val names = found.flatMap { (serviceType, outcome) ->
            outcome.serviceNames.map { "$it ($serviceType)" }
        }

        when {
            failure != null -> LocalNetworkDiscovery("Local network is blocked: $failure", false)
            names.isEmpty() -> LocalNetworkDiscovery(
                "Discovery ran for $DISCOVERY_WINDOW and nothing answered",
                null
            )

            else -> LocalNetworkDiscovery("Local network reached: ${names.joinToString()}", true)
        }
    }

    private suspend fun discover(
        discoveryManager: NsdManager,
        serviceType: String,
    ): DiscoveryOutcome {
        val serviceNames = mutableSetOf<String>()
        val startFailure = CompletableDeferred<String>()
        val discoveryListener = buildDiscoveryListener(serviceNames, startFailure)

        try {
            // NsdManager wants the trailing dot of a fully qualified service type.
            discoveryManager.discoverServices(
                "$serviceType.",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
        } catch (e: SecurityException) {
            return DiscoveryOutcome(emptySet(), e.message)
        }

        delay(DISCOVERY_WINDOW)
        runCatching { discoveryManager.stopServiceDiscovery(discoveryListener) }

        return DiscoveryOutcome(
            serviceNames,
            if (startFailure.isCompleted) startFailure.getCompleted() else null
        )
    }

    private fun buildDiscoveryListener(
        serviceNames: MutableSet<String>,
        startFailure: CompletableDeferred<String>,
    ): NsdManager.DiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            startFailure.complete("discovery of $serviceType could not start, error $errorCode")
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

        override fun onDiscoveryStarted(serviceType: String) = Unit

        override fun onDiscoveryStopped(serviceType: String) = Unit

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            serviceNames.add(serviceInfo.serviceName)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit
    }

    private data class DiscoveryOutcome(val serviceNames: Set<String>, val failure: String?)
}
