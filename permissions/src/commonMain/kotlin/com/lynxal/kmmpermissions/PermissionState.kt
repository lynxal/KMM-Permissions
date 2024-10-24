package com.lynxal.kmmpermissions

enum class PermissionState {
    UNKNOWN,
    UNAVAILABLE,
    UNDETERMINED,
    GRANTED,
    DENIED,
    DENIED_ALWAYS;

    fun isDenied(): Boolean {
        return this == DENIED || this == DENIED_ALWAYS || this == UNAVAILABLE
    }
}