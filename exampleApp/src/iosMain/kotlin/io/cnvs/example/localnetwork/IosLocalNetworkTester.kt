package io.cnvs.example.localnetwork

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.delay
import platform.Network.NW_PARAMETERS_DEFAULT_CONFIGURATION
import platform.Network.NW_PARAMETERS_DISABLE_PROTOCOL
import platform.Network.nw_browse_descriptor_create_bonjour_service
import platform.Network.nw_browse_result_copy_endpoint
import platform.Network.nw_browser_cancel
import platform.Network.nw_browser_create
import platform.Network.nw_browser_set_browse_results_changed_handler
import platform.Network.nw_browser_set_queue
import platform.Network.nw_browser_set_state_changed_handler
import platform.Network.nw_browser_start
import platform.Network.nw_browser_state_failed
import platform.Network.nw_browser_state_waiting
import platform.Network.nw_endpoint_get_bonjour_service_name
import platform.Network.nw_error_get_error_code
import platform.Network.nw_error_get_error_domain
import platform.Network.nw_parameters_create_secure_tcp
import platform.Network.nw_parameters_set_include_peer_to_peer
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Duration.Companion.seconds

/**
 * Browses the network for printers and the other devices that advertise themselves over Bonjour.
 * Without local network access iOS answers the browse with a policy error instead of results,
 * which is the point of the test. Every service type browsed here is listed under
 * `NSBonjourServices` in the example app's Info.plist — iOS answers nothing for a type that is not.
 */
@OptIn(ExperimentalForeignApi::class)
class IosLocalNetworkTester : LocalNetworkTester {
    private companion object {
        val DISCOVERY_WINDOW = 4.seconds
    }

    override suspend fun discoverServices(): LocalNetworkDiscovery {
        val serviceNames = mutableSetOf<String>()
        val failures = mutableSetOf<String>()
        val browsers = LocalNetworkTester.SERVICE_TYPES.map { serviceType ->
            startBrowser(serviceType, serviceNames, failures)
        }

        try {
            delay(DISCOVERY_WINDOW)
        } finally {
            browsers.forEach { nw_browser_cancel(it) }
        }

        return when {
            failures.isNotEmpty() -> LocalNetworkDiscovery(
                "Local network is blocked: ${failures.joinToString()}",
                false
            )

            serviceNames.isEmpty() -> LocalNetworkDiscovery(
                "Discovery ran for $DISCOVERY_WINDOW and nothing answered",
                null
            )

            else -> LocalNetworkDiscovery(
                "Local network reached: ${serviceNames.joinToString()}",
                true
            )
        }
    }

    private fun startBrowser(
        serviceType: String,
        serviceNames: MutableSet<String>,
        failures: MutableSet<String>,
    ): platform.darwin.NSObject? {
        val parameters = nw_parameters_create_secure_tcp(
            NW_PARAMETERS_DISABLE_PROTOCOL,
            NW_PARAMETERS_DEFAULT_CONFIGURATION
        )
        nw_parameters_set_include_peer_to_peer(parameters, true)

        val browser = nw_browser_create(
            nw_browse_descriptor_create_bonjour_service(serviceType, null),
            parameters
        )
        nw_browser_set_queue(browser, dispatch_get_main_queue())
        nw_browser_set_state_changed_handler(browser) { state, error ->
            // A denial leaves the browser waiting with an error rather than failing it.
            if (state == nw_browser_state_failed || state == nw_browser_state_waiting) {
                val domain = error?.let { nw_error_get_error_domain(it) }
                val code = error?.let { nw_error_get_error_code(it) }
                if (error != null) {
                    failures.add("$serviceType stopped with error domain $domain code $code")
                }
            }
        }
        nw_browser_set_browse_results_changed_handler(browser) { _, newResult, _ ->
            val endpoint = nw_browse_result_copy_endpoint(newResult)
            val name = endpoint?.let { nw_endpoint_get_bonjour_service_name(it)?.toKString() }
            if (name != null) {
                serviceNames.add("$name ($serviceType)")
            }
        }
        nw_browser_start(browser)

        return browser
    }
}
