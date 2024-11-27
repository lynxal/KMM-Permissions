package com.lynxal.kmmpermissions.delegate

import com.lynxal.logging.Logger
import com.lynxal.logging.debug
import com.lynxal.kmmpermissions.BluetoothResettingException
import com.lynxal.kmmpermissions.BluetoothTurnedOffException
import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionDeniedPermanentlyException
import com.lynxal.kmmpermissions.PermissionState
import com.lynxal.kmmpermissions.PermissionRequestUnsupportedException
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class BluetoothPermissionRequestDelegate(
    private val permission: Permission
) : PermissionRequestDelegate {
    override suspend fun requestPermission() {
        Logger.debug("Requesting bluetooth message, CBManagerState authorization is ${CBManager.authorization}")
        val state: CBManagerState =
            if (CBManager.authorization == CBManagerAuthorizationNotDetermined) {
            suspendCoroutine { continuation ->
                CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        continuation.resume(central.state)
                    }
                }, null)
            }
        } else {
                CBCentralManager().state
        }

        Logger.debug("CBManagerState state is $state")

        when(state) {
            CBManagerStatePoweredOn -> return
            CBManagerStateUnauthorized -> throw PermissionDeniedPermanentlyException(permission)
            CBManagerStatePoweredOff -> throw BluetoothTurnedOffException("Bluetooth is powered off")
            CBManagerStateResetting -> throw BluetoothResettingException("Bluetooth is restarting")
            CBManagerStateUnsupported -> throw PermissionRequestUnsupportedException(
                permission,
                "Bluetooth is not supported on this device"
            )

            CBManagerStateUnknown -> throw RuntimeException("Bluetooth state is unknown, this should not happen normally")
            else -> throw RuntimeException("Unknown bluetooth state $state")
        }
    }

    override suspend fun requestPermissionState(): PermissionState {
        Logger.debug("Checking bluetooth permission state")
        return when (val state: CBManagerAuthorization = CBManager.authorization) {
            CBManagerAuthorizationAllowedAlways -> PermissionState.GRANTED
            CBManagerAuthorizationRestricted -> PermissionState.UNAVAILABLE
            CBManagerAuthorizationNotDetermined -> PermissionState.UNDETERMINED
            CBManagerAuthorizationDenied -> PermissionState.DENIED_ALWAYS
            else -> error("Unknown bluetooth state $state, (CBManagerAuthorization)")
        }
    }
}