package io.cnvs.example.localnetwork

/**
 * A real local network operation, so the example app can show what the permission actually buys.
 * It looks for printers and for the other devices that announce themselves over mDNS — exactly the
 * traffic the local network permission gates: with the permission the discovery reaches the
 * network, without it the platform blocks it.
 */
interface LocalNetworkTester {
    suspend fun discoverServices(): LocalNetworkDiscovery

    companion object {
        /** Printers first, then the service types most home and office networks carry. */
        val SERVICE_TYPES = listOf("_ipp._tcp", "_printer._tcp", "_airplay._tcp", "_http._tcp")
    }
}
