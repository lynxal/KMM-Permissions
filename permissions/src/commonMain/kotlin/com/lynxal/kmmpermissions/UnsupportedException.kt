package com.lynxal.kmmpermissions

open class UnsupportedException(
    val permissions: Permission,
    message: String? = null
) : Exception(message)