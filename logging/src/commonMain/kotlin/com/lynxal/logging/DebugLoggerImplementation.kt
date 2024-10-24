package com.lynxal.logging

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class DebugLoggerImplementation : LoggerImplementation {
    override fun log(logDetails: LogDetails, loggerExtras: LoggerExtras)
}