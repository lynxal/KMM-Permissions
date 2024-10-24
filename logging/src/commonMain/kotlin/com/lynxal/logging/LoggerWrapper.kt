package com.lynxal.logging

/**
 * A wrapper class around the LoggerInterface, used to provide a logging extras without modifying the initial LoggerInterface instance
 */
class LoggerWrapper(private val logger: LoggerInterface, override val extras: LoggerExtras) :
    LoggerInterface {
    override fun verbose(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        logger.verbose(extras, details)

    override fun debug(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        logger.debug(extras, details)

    override fun info(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        logger.info(extras, details)

    override fun warning(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        logger.warning(extras, details)

    override fun error(loggerExtras: LoggerExtras, details: LogDetails.Builder.() -> Unit) =
        logger.error(extras, details)

    override fun tag(tag: String): LoggerInterface = LoggerWrapper(logger, extras.copy(tag = tag))
    override fun add(loggerImplementation: LoggerImplementation) = logger.add(loggerImplementation)
}