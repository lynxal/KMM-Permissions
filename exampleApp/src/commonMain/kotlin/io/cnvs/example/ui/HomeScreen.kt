package io.cnvs.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lynxal.kmmpermissions.Permission
import kmmpermissions.exampleapp.generated.resources.Res
import kmmpermissions.exampleapp.generated.resources.label_choose_permission_section
import kmmpermissions.exampleapp.generated.resources.label_permissions_bluetooth_advertise
import kmmpermissions.exampleapp.generated.resources.label_permissions_bluetooth_connect
import kmmpermissions.exampleapp.generated.resources.label_permissions_bluetooth_le
import kmmpermissions.exampleapp.generated.resources.label_permissions_bluetooth_scan
import kmmpermissions.exampleapp.generated.resources.label_permissions_camera
import kmmpermissions.exampleapp.generated.resources.label_permissions_coarse_location
import kmmpermissions.exampleapp.generated.resources.label_permissions_fine_location
import kmmpermissions.exampleapp.generated.resources.label_permissions_gallery
import kmmpermissions.exampleapp.generated.resources.label_permissions_notifications
import kmmpermissions.exampleapp.generated.resources.label_permissions_read_storage
import kmmpermissions.exampleapp.generated.resources.label_permissions_write_storage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {
    companion object {
        private val permissionOptions: Map<StringResource, Permission> = mapOf(
            Res.string.label_permissions_fine_location to Permission.FINE_LOCATION,
            Res.string.label_permissions_coarse_location to Permission.COARSE_LOCATION,
            Res.string.label_permissions_bluetooth_le to Permission.BLUETOOTH_LE,
            Res.string.label_permissions_bluetooth_scan to Permission.BLUETOOTH_SCAN,
            Res.string.label_permissions_bluetooth_advertise to Permission.BLUETOOTH_ADVERTISE,
            Res.string.label_permissions_bluetooth_connect to Permission.BLUETOOTH_CONNECT,
            Res.string.label_permissions_notifications to Permission.NOTIFICATIONS,
            Res.string.label_permissions_read_storage to Permission.READ_STORAGE,
            Res.string.label_permissions_write_storage to Permission.WRITE_STORAGE,
            Res.string.label_permissions_gallery to Permission.GALLERY,
            Res.string.label_permissions_camera to Permission.CAMERA,
        )
    }


    @Composable
    override fun Content() {
        Scaffold { contentPadding ->
            Surface(modifier = Modifier.padding(contentPadding)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Text(
                        modifier = Modifier.padding(
                            top = 24.dp,
                            bottom = 24.dp,
                            start = 12.dp,
                            end = 12.dp
                        )
                            .align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        text = stringResource(Res.string.label_choose_permission_section)
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        val navigator = LocalNavigator.current ?: return@Surface

                        permissionOptions.forEach { entry ->
                            Button(onClick = {
                                navigator.push(PermissionScreen(entry.value.ordinal))
                            }) {
                                Text(stringResource(entry.key))
                            }
                        }
                    }
                }
            }
        }
    }
}