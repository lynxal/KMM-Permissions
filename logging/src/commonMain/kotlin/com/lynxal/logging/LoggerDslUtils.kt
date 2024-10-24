package com.lynxal.logging

val Logger: LoggerInterfaceImpl by lazy { LoggerInterfaceImpl() }

/**
 * A helper function simplifying the logger interface, prints a message using [LogLevel.Verbose]
 */
fun LoggerInterface.verbose(message: String, cause: Throwable? = null) = verbose {
    this.message = message
    this.cause = cause
}

/**
 * A helper function simplifying the logger interface, prints a message using [LogLevel.Debug]
 */
fun LoggerInterface.debug(message: String, cause: Throwable? = null) = debug {
    this.message = message
    this.cause = cause
}

/**
 * A helper function simplifying the logger interface, prints a message using [LogLevel.Info]
 */
fun LoggerInterface.info(message: String, cause: Throwable? = null) = info {
    this.message = message
    this.cause = cause
}

/**
 * A helper function simplifying the logger interface, prints a message using [LogLevel.Warning]
 */
fun LoggerInterface.warning(message: String, cause: Throwable? = null) = warning {
    this.message = message
    this.cause = cause
}

/**
 * A helper function simplifying the logger interface, prints a message using [LogLevel.Error]
 */
fun LoggerInterface.error(message: String, cause: Throwable? = null) = error {
    this.message = message
    this.cause = cause
}

