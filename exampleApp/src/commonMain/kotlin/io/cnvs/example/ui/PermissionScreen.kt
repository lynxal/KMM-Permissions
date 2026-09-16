package io.cnvs.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionException
import com.lynxal.kmmpermissions.PermissionState
import com.lynxal.kmmpermissions.compose.BindEffect
import com.lynxal.logging.Logger
import io.cnvs.example.app.appInfoInstance
import kotlinx.coroutines.launch

class PermissionScreen(permissionOrdinal: Int) : Screen {
    val permission: Permission = Permission.entries[permissionOrdinal]

    @Composable
    override fun Content() {
        val permissionController = remember { appInfoInstance.createPermissionController() }
        val localNetworkTester = remember { appInfoInstance.createLocalNetworkTester() }
        var permissionState by remember { mutableStateOf(PermissionState.UNKNOWN) }
        var localNetworkTestResult by remember { mutableStateOf("") }
        var coroutineScope = rememberCoroutineScope()
        BindEffect(permissionController)

        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp)) {
                    val localNavigator = LocalNavigator.current
                    Button(onClick = { localNavigator?.pop() }) {
                        Text("Back")
                    }
                }
            }) { contentPadding ->
            Surface(modifier = Modifier.padding(contentPadding)) {
                Column(Modifier.fillMaxWidth().systemBarsPadding()) {
                    Text(
                        modifier = Modifier.padding(
                            top = 24.dp, bottom = 24.dp, start = 12.dp, end = 12.dp
                        ).align(CenterHorizontally),
                        textAlign = TextAlign.Center,
                        text = permission.name
                    )

                    Text("Permission: ${permissionState.name}")

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier.weight(1f), onClick = {
                                coroutineScope.launch {
                                    permissionState =
                                        permissionController.requestPermissionState(permission)
                                }
                            }) {
                            Text("Check Permission")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier.weight(1f), onClick = {
                                coroutineScope.launch {
                                    try {
                                        permissionController.requestPermission(permission)
                                    } catch (e: PermissionException) {
                                        Logger.error("Failed to request Bluetooth permissions", e)
                                    }
                                    permissionState =
                                        permissionController.requestPermissionState(permission)
                                }
                            }) {
                            Text("Request Permission")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier.weight(1f), onClick = {
                                coroutineScope.launch {
                                    permissionController.openAppSettings()
                                }
                            }) {
                            Text("Open Settings")
                        }
                    }

                    // Local network access is only visible through a real network operation, so this
                    // screen offers one: looking for printers and the other devices that announce
                    // themselves over mDNS.
                    if (permission == Permission.LOCAL_NETWORK) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                modifier = Modifier.weight(1f), onClick = {
                                    coroutineScope.launch {
                                        localNetworkTestResult = "Looking for printers and other devices…"
                                        val discovery = localNetworkTester.discoverServices()
                                        localNetworkTestResult = discovery.message

                                        // On iOS the system raises its own dialog when the app
                                        // touches the network, and nothing reports the answer back
                                        // to the library — so what the discovery saw is the most
                                        // direct evidence this screen has.
                                        when (discovery.reachedNetwork) {
                                            true -> permissionState = PermissionState.GRANTED
                                            false -> permissionState = PermissionState.DENIED_ALWAYS
                                            null -> Unit
                                        }
                                    }
                                }) {
                                Text("Discover Devices")
                            }
                        }

                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = localNetworkTestResult
                        )
                    }
                }
            }
        }
    }
}