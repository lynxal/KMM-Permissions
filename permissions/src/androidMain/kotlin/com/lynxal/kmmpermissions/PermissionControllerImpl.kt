package com.lynxal.kmmpermissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.lynxal.logging.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PermissionControllerImpl(
    private val appContext: Context,
) : PermissionsController {
    companion object {
        private const val KEY_NUM_REJECTED = "rejected_times"
    }

    private val mutex = Mutex()
    private val permissionsHelper = PermissionsHelper()
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences("PermissionsController", Context.MODE_PRIVATE)

    @Volatile
    private var activity: WeakReference<Activity?> = WeakReference(null)

    private val permissionsLauncher = mutableStateOf<ActivityResultLauncher<Array<String>>?>(null)

    // Local network access is a runtime permission only from Android 17 on, and only for apps that
    // target it. An app targeting an older SDK keeps the implicit grant, and Android documents that
    // it must not ask for the permission at runtime.
    private val isLocalNetworkAccessImplicitlyGranted: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
                appContext.applicationInfo.targetSdkVersion < Build.VERSION_CODES.CINNAMON_BUN

    // WRITE_EXTERNAL_STORAGE only has an effect while the app still sees the legacy view of
    // external storage. From Android 10 on, an app writing through the MediaStore owns the files it
    // creates and needs no storage permission at all, and the platform ignores the old one
    // (developer.android.com/training/data-storage/shared/media, whose own manifest snippet caps
    // the permission at maxSdkVersion="29"). An app targeting 29 can keep the legacy view on a much
    // newer device, so what is read here is the view itself, not the API level.
    private val isWriteStorageHandledByScopedStorage: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !Environment.isExternalStorageLegacy()

    override suspend fun requestPermission(permission: Permission) {
        if (permission == Permission.LOCAL_NETWORK && isLocalNetworkAccessImplicitlyGranted) {
            Logger.tag("PermissionsController")
                .debug("Local network access is implicitly granted, not requesting it")
            return
        }

        if (permission == Permission.WRITE_STORAGE && isWriteStorageHandledByScopedStorage) {
            Logger.tag("PermissionsController")
                .debug("Scoped storage is in effect, not requesting write storage access")
            return
        }

        mutex.withLock {
            suspendCoroutine { continuation ->
                val activityLocal = activity.get()
                val launcherLocal = permissionsLauncher.value
                requireNotNull(activityLocal)
                requireNotNull(launcherLocal)
                permissionsHelper.requestPermission(
                    activityLocal, permission, launcherLocal
                ) {
                    onPermissionState(permission)
                    continuation.resume(it)
                }
            }
        }
    }

    override suspend fun requestPermissionState(permission: Permission): PermissionState {
        val platformPermissions = permission.toPlatformPermission()

        if (permission == Permission.NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(appContext).areNotificationsEnabled()
                .let { enabled ->
                    if (enabled) PermissionState.GRANTED
                    else PermissionState.DENIED_ALWAYS
                }
        }

        // Granted means the app can write the media it owns through the MediaStore, which is what
        // the platform allows without a permission here. Writing to an arbitrary path outside the
        // MediaStore still fails, and no permission changes that.
        if (permission == Permission.WRITE_STORAGE && isWriteStorageHandledByScopedStorage)
            return PermissionState.GRANTED

        if (permission == Permission.LOCAL_NETWORK && isLocalNetworkAccessImplicitlyGranted)
            return PermissionState.GRANTED

        val permissionsStatus = platformPermissions.map {
            Triple(
                it,
                ContextCompat.checkSelfPermission(appContext, it),
                activity.get()?.shouldShowRequestPermissionRationale(it)
            )
        }
        if (permissionsStatus.all { it.second == PackageManager.PERMISSION_GRANTED }) return PermissionState.GRANTED

        val rejectedTwice = permissionsStatus.find { (platformPermission, _, _) ->
            sharedPreferences.getInt("${platformPermission}_$KEY_NUM_REJECTED", 0) > 1
        } != null

        return if (rejectedTwice) {
            PermissionState.DENIED
        } else {
            PermissionState.UNDETERMINED
        }
    }

    override suspend fun isGranted(permission: Permission): Boolean =
        requestPermissionState(permission) == PermissionState.GRANTED

    override suspend fun openAppSettings() {
        appContext.startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", appContext.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    @Composable
    override fun Register() {
        val activity = LocalActivity.current as ComponentActivity
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissionsHelper.handlePermissionRequestCallback(
                    it, activity
                )
            }

        DisposableEffect(activity) {
            this@PermissionControllerImpl.activity = WeakReference(activity)
            permissionsLauncher.value = launcher

            onDispose {
                this@PermissionControllerImpl.activity = WeakReference(null)
                permissionsLauncher.value = null
            }
        }
    }

    private fun onPermissionState(permission: Permission) {
        permission.toPlatformPermission().forEach { platformPermission ->
            val status = ContextCompat.checkSelfPermission(appContext, platformPermission)
            val ssr = activity.get()?.shouldShowRequestPermissionRationale(platformPermission)

            val deniedNumKey = "${platformPermission}_$KEY_NUM_REJECTED"
            val numDenied = sharedPreferences.getInt(deniedNumKey, 0)

            val updatedNumDenied = if (status == PackageManager.PERMISSION_GRANTED) {
                // When the permission is granted, and user manually revokes it
                // the system will let us to ask for the permission only once
                1
            } else {
                if (ssr == true) {
                    // Denied Once
                    Logger.tag("PermissionsController").debug("$platformPermission denied once")
                    1
                } else if (numDenied > 0 && ssr == false) {
                    // Denied Twice
                    Logger.tag("PermissionsController").debug("$platformPermission denied twice")
                    2
                } else if (numDenied == 0) {
                    // initial state, consider unchecked
                    Logger.tag("PermissionsController").debug("$platformPermission not checked yet")
                    0
                } else {
                    Logger.tag("PermissionsController")
                        .debug("$platformPermission should not happen, numDenied: $numDenied, ssr: $ssr")
                    numDenied
                }
            }

            if (numDenied != updatedNumDenied) {
                sharedPreferences.edit { putInt(deniedNumKey, updatedNumDenied) }
            }
        }
    }
}
