package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionDeniedPermanentlyException
import com.lynxal.kmmpermissions.PermissionState
import com.lynxal.kmmpermissions.mainContinuation
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.suspendCoroutine

class NotificationPermissionRequestDelegate : PermissionRequestDelegate {
    override suspend fun requestPermission() {
        val status = suspendCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler(mainContinuation { settings: UNNotificationSettings? ->
                    continuation.resumeWith(
                        Result.success(
                            settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined
                        )
                    )
                })
        }

        when (status) {
            UNAuthorizationStatusAuthorized -> return

            UNAuthorizationStatusNotDetermined -> {
                val isSuccess = suspendCoroutine<Boolean> { continuation ->
                    UNUserNotificationCenter.currentNotificationCenter()
                        .requestAuthorizationWithOptions(UNAuthorizationOptionSound.or(
                                UNAuthorizationOptionAlert
                            ).or(UNAuthorizationOptionBadge), mainContinuation { isOk, error ->
                            if (isOk && error == null) {
                                continuation.resumeWith(Result.success(true))
                            } else {
                                continuation.resumeWith(Result.success(false))
                            }
                        })
                }

                if(isSuccess) {
                    requestPermission()
                } else {
                    error("notifications permission request failed")
                }
            }

            UNAuthorizationStatusDenied -> throw PermissionDeniedPermanentlyException(Permission.NOTIFICATIONS)
            else -> error("unknown push notification authorization status $status")
        }

    }

    override suspend fun requestPermissionState(): PermissionState {
        val status = suspendCoroutine<UNAuthorizationStatus> { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler(mainContinuation { settings: UNNotificationSettings? ->
                    continuation.resumeWith(
                        Result.success(
                            settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined
                        )
                    )
                })
        }

        return when (status) {
            UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral -> PermissionState.GRANTED

            UNAuthorizationStatusNotDetermined -> PermissionState.UNDETERMINED
            UNAuthorizationStatusDenied -> PermissionState.DENIED_ALWAYS
            else -> error("unknown push notification authorization status $status")
        }
    }
}