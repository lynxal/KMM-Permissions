package com.lynxal.kmmpermissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

// This helper class is inspired by MOKO Resource, so some part of the codebase might seem familiar
internal class PermissionsHelper() {
    private var requestedPermissionDetails: RequestedPermissionDetails? = null
    fun handlePermissionRequestCallback(
        permissionsResult: Map<String, Boolean>, activity: ComponentActivity
    ) {
        val requestDetails = requestedPermissionDetails ?: return
        // Reset the original request callback, so it will not trigger a cancellation exception
        // in case if another request is being made while the processing of this result is not
        // done yet, not sure if it's possible TBH
        requestedPermissionDetails = null

        if (permissionsResult.isEmpty()) {
            requestDetails.callback(
                Result.failure(
                    PermissionRequestCancelledException(requestDetails.permission)
                )
            )

            return
        }

        if (permissionsResult.values.all { it }) {
            // All permissions were granted
            requestDetails.callback(Result.success(Unit))
        } else {
            if (permissionsResult.keys.any { activity.shouldShowRequestPermissionRationale(it) }) {
                // At least one of the permissions was rejected permanently
                requestDetails.callback(
                    Result.failure(
                        PermissionDeniedPermanentlyException(
                            requestDetails.permission
                        )
                    )
                )
            } else {
                requestDetails.callback(Result.failure(PermissionDeniedException(requestDetails.permission)))
            }
        }
    }


    fun requestPermission(
        context: Context,
        permission: Permission,
        permissionLauncher: ActivityResultLauncher<Array<String>>,
        resultCallback: (Result<Unit>) -> Unit
    ) {
        val permissions = permission.toPlatformPermission()
        val permissionsToRequest = permissions.filter { platformPermission ->
            ContextCompat.checkSelfPermission(
                context, platformPermission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            resultCallback.invoke(Result.success(Unit))
            return
        }

        requestedPermissionDetails?.let {
            it.callback(Result.failure(PermissionRequestCancelledException(it.permission)))
            requestedPermissionDetails = null
        }

        requestedPermissionDetails = RequestedPermissionDetails(permission, resultCallback)

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}