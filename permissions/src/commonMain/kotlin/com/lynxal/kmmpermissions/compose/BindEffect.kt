package com.lynxal.kmmpermissions.compose

import androidx.compose.runtime.Composable
import com.lynxal.kmmpermissions.PermissionsController

@Composable
expect fun BindEffect(permissionsController: PermissionsController)
