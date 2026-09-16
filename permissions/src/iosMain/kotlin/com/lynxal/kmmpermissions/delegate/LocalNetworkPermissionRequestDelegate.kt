package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionDeniedException
import com.lynxal.kmmpermissions.PermissionDeniedPermanentlyException
import com.lynxal.kmmpermissions.PermissionRequestUnsupportedException
import com.lynxal.kmmpermissions.PermissionState
import com.lynxal.logging.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import platform.Foundation.NSBundle
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Network.NW_PARAMETERS_DEFAULT_CONFIGURATION
import platform.Network.NW_PARAMETERS_DISABLE_PROTOCOL
import platform.Network.nw_advertise_descriptor_create_bonjour_service
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
import platform.Network.nw_connection_cancel
import platform.Network.nw_endpoint_get_bonjour_service_name
import platform.Network.nw_error_domain_dns
import platform.Network.nw_error_get_error_code
import platform.Network.nw_error_get_error_domain
import platform.Network.nw_listener_cancel
import platform.Network.nw_listener_create
import platform.Network.nw_listener_set_advertise_descriptor
import platform.Network.nw_listener_set_new_connection_handler
import platform.Network.nw_listener_set_queue
import platform.Network.nw_listener_set_state_changed_handler
import platform.Network.nw_listener_start
import platform.Network.nw_listener_state_failed
import platform.Network.nw_listener_state_waiting
import platform.Network.nw_parameters_create_secure_tcp
import platform.Network.nw_parameters_set_include_peer_to_peer
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * iOS has no API that reports the local network authorization, so this delegate infers it:
 * it advertises a throwaway Bonjour service and browses for that same service. Seeing it means
 * the app may use the local network; a "policy denied" answer from the DNS layer means the user
 * said no. The first probe is what raises the system dialog.
 *
 * The host app must declare `NSLocalNetworkUsageDescription` and list [SERVICE_TYPE] under
 * `NSBonjourServices` in its Info.plist, otherwise the probe cannot run and the permission is
 * reported as unavailable.
 *
 * A state read never raises the dialog: before the app has asked through `requestPermission` it
 * answers UNDETERMINED. The cost of that is what iOS cannot tell us — if the app reaches the local
 * network through its own code, the system raises the dialog and this delegate cannot see the
 * answer until it probes, so drive the UI from that network result or ask through
 * `requestPermission`.
 *
 * The answer is an inference, not a status read: the iOS Simulator does not enforce the
 * restriction and reports granted, and there are iOS releases where traffic still flows after
 * a denial.
 */
@OptIn(ExperimentalForeignApi::class)
internal class LocalNetworkPermissionRequestDelegate : PermissionRequestDelegate {
    private companion object {
        const val SERVICE_TYPE = "_kmmpermissions._tcp"
        const val USAGE_DESCRIPTION_KEY = "NSLocalNetworkUsageDescription"
        const val BONJOUR_SERVICES_KEY = "NSBonjourServices"
        const val REQUESTED_ONCE_KEY = "com.lynxal.kmmpermissions.localNetworkRequested"
        const val LOG_TAG = "LocalNetworkPermission"

        // kDNSServiceErr_PolicyDenied — what the DNS layer answers once the user has said no.
        const val DNS_POLICY_DENIED = -65570

        // The request probe waits out the system dialog; the plain check only waits for discovery.
        val REQUEST_TIMEOUT = 30.seconds
        val CHECK_TIMEOUT = 3.seconds
    }

    override suspend fun requestPermission() {
        if (!isInfoPlistConfigured()) {
            throw PermissionRequestUnsupportedException(
                Permission.LOCAL_NETWORK,
                "Add $USAGE_DESCRIPTION_KEY and $SERVICE_TYPE under $BONJOUR_SERVICES_KEY to the Info.plist"
            )
        }

        NSUserDefaults.standardUserDefaults.setBool(true, REQUESTED_ONCE_KEY)

        when (val state = probeLocalNetworkAccess(REQUEST_TIMEOUT)) {
            PermissionState.GRANTED -> return

            // iOS asks once — after a denial only the Settings app can change the answer.
            PermissionState.DENIED_ALWAYS -> throw PermissionDeniedPermanentlyException(
                Permission.LOCAL_NETWORK,
                "Local network access was denied"
            )

            else -> throw PermissionDeniedException(
                Permission.LOCAL_NETWORK,
                "Could not confirm local network access, the probe ended as $state"
            )
        }
    }

    override suspend fun requestPermissionState(): PermissionState {
        if (!isInfoPlistConfigured()) return PermissionState.UNAVAILABLE

        // A check never prompts. Probing is what raises the dialog, so until the app has asked
        // through requestPermission the honest answer is "not asked yet" — the same answer Android
        // gives in that situation. Once the system has answered, probing is free of dialogs and the
        // check reads the live answer.
        if (!NSUserDefaults.standardUserDefaults.boolForKey(REQUESTED_ONCE_KEY)) {
            return PermissionState.UNDETERMINED
        }

        return probeLocalNetworkAccess(CHECK_TIMEOUT)
    }

    private fun isInfoPlistConfigured(): Boolean {
        val bundle = NSBundle.mainBundle
        val hasUsageDescription = bundle.objectForInfoDictionaryKey(USAGE_DESCRIPTION_KEY) is String
        val bonjourServices = bundle.objectForInfoDictionaryKey(BONJOUR_SERVICES_KEY) as? List<*>
        val advertisesServiceType = bonjourServices?.any { it == SERVICE_TYPE } == true

        if (!hasUsageDescription || !advertisesServiceType) {
            Logger.tag(LOG_TAG).error(
                "Info.plist is missing $USAGE_DESCRIPTION_KEY or the $SERVICE_TYPE entry in $BONJOUR_SERVICES_KEY"
            )
            return false
        }
        return true
    }

    private suspend fun probeLocalNetworkAccess(timeout: Duration): PermissionState {
        val serviceName = NSUUID().UUIDString
        val queue = dispatch_get_main_queue()
        val probeResult = CompletableDeferred<PermissionState>()

        val listener = nw_listener_create(
            nw_parameters_create_secure_tcp(
                NW_PARAMETERS_DISABLE_PROTOCOL,
                NW_PARAMETERS_DEFAULT_CONFIGURATION
            )
        )
        val browserParameters = nw_parameters_create_secure_tcp(
            NW_PARAMETERS_DISABLE_PROTOCOL,
            NW_PARAMETERS_DEFAULT_CONFIGURATION
        )
        nw_parameters_set_include_peer_to_peer(browserParameters, true)
        val browser = nw_browser_create(
            nw_browse_descriptor_create_bonjour_service(SERVICE_TYPE, null),
            browserParameters
        )

        try {
            nw_listener_set_advertise_descriptor(
                listener,
                nw_advertise_descriptor_create_bonjour_service(serviceName, SERVICE_TYPE, null)
            )
            // A listener refuses to start without one; nothing ever connects to this service.
            nw_listener_set_new_connection_handler(listener) { connection ->
                nw_connection_cancel(connection)
            }
            nw_listener_set_queue(listener, queue)
            nw_listener_set_state_changed_handler(listener) { state, error ->
                if (state == nw_listener_state_failed || state == nw_listener_state_waiting) {
                    reportState(state, error, isTerminal = state == nw_listener_state_failed, probeResult)
                }
            }

            nw_browser_set_queue(browser, queue)
            nw_browser_set_state_changed_handler(browser) { state, error ->
                if (state == nw_browser_state_failed || state == nw_browser_state_waiting) {
                    reportState(state, error, isTerminal = state == nw_browser_state_failed, probeResult)
                }
            }
            nw_browser_set_browse_results_changed_handler(browser) { _, newResult, _ ->
                val endpoint = nw_browse_result_copy_endpoint(newResult)
                val foundName = endpoint?.let { nw_endpoint_get_bonjour_service_name(it)?.toKString() }
                if (foundName == serviceName) {
                    probeResult.complete(PermissionState.GRANTED)
                }
            }

            nw_listener_start(listener)
            nw_browser_start(browser)

            val state = withTimeoutOrNull(timeout) { probeResult.await() }
            if (state == null) {
                Logger.tag(LOG_TAG)
                    .debug("Local network probe found nothing within $timeout, the state stays unknown")
            }
            return state ?: PermissionState.UNKNOWN
        } finally {
            nw_browser_cancel(browser)
            nw_listener_cancel(listener)
        }
    }

    /**
     * A denial does not fail the browser or the listener — they go on waiting, carrying the DNS
     * "policy denied" error. Only that error is an answer; any other waiting state is a network
     * that is not ready yet, so the probe keeps waiting for one.
     */
    private fun reportState(
        state: UInt,
        error: NSObject?,
        isTerminal: Boolean,
        probeResult: CompletableDeferred<PermissionState>,
    ) {
        val domain = error?.let { nw_error_get_error_domain(it) }
        val code = error?.let { nw_error_get_error_code(it) }
        Logger.tag(LOG_TAG).debug("Local network probe state $state, error domain $domain code $code")

        if (domain == nw_error_domain_dns && code == DNS_POLICY_DENIED) {
            probeResult.complete(PermissionState.DENIED_ALWAYS)
        } else if (isTerminal) {
            probeResult.complete(PermissionState.UNKNOWN)
        }
    }
}
