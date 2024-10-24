package com.lynxal.logging

data class LogDetails(
    val logLevel: LogLevel,
    val message: String,
    val cause: Throwable? = null,
    val payload: Map<String, String> = emptyMap()
) {
    data class Builder internal constructor(
        var message: String = "",
        var cause: Throwable? = null,
        var payload: Map<String, String> = emptyMap()
    ) {
        internal fun build(logLevel: LogLevel) = LogDetails(logLevel, message, cause, payload)
    }
}