package com.lynxal.logging

/**
 * Logger class purely written in kotlin. It's using the same approach as used in Timber.
 * If you wish to have a basic Logging functionality plant a DebugTree, otherwise
 * provide a custom tree implementation.
 */
open class LoggerInterfaceImpl : LoggerInterface {
    private val loggerImplementations: MutableSet<LoggerImplementation> = mutableSetOf()
    var minLevel: LogLevel = LogLevel.Debug

    override val extras: LoggerExtras = LoggerExtras()
    override fun verbose(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        log(LogDetails.Builder().apply {
            details()
        }.build(LogLevel.Verbose), loggerExtras)

    override fun debug(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        log(LogDetails.Builder().apply {
            details()
        }.build(LogLevel.Debug), loggerExtras)

    override fun info(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        log(LogDetails.Builder().apply {
            details()
        }.build(LogLevel.Info), loggerExtras)

    override fun warning(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        log(LogDetails.Builder().apply {
            details()
        }.build(LogLevel.Warning), loggerExtras)

    override fun error(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        log(LogDetails.Builder().apply {
            details()
        }.build(LogLevel.Error), loggerExtras)


    override fun tag(tag: String): LoggerInterface {
        return LoggerWrapper(this, extras.copy(tag = tag))
    }

    override fun add(loggerImplementation: LoggerImplementation) {
        loggerImplementations.add(loggerImplementation)
    }

    private fun log(logDetails: LogDetails, loggerExtras: LoggerExtras) {
        if (logDetails.logLevel.level >= minLevel.level) {
            loggerImplementations.forEach {
                it.log(
                    logDetails = logDetails, loggerExtras = loggerExtras
                )
            }
        }
    }
}