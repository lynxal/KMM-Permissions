package com.lynxal.kmmpermissions.compose

import androidx.compose.runtime.Composable
import com.lynxal.kmmpermissions.PermissionsController

@Composable
actual fun BindEffect(permissionsController: PermissionsController) {
    permissionsController.Register()
}
