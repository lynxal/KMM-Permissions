package com.lynxal.logging

import platform.Foundation.NSLog

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class DebugLoggerImplementation : LoggerImplementation {
    actual override fun log(logDetails: LogDetails, loggerExtras: LoggerExtras) {
        NSLog("%s - %s : %s", logDetails.logLevel.name, loggerExtras.tag, formatMessage(logDetails))
    }

    private fun formatMessage(logDetails: LogDetails): String {
        return listOfNotNull(
            logDetails.message,
            getStackTraceString(logDetails.cause),
            getArgumentsString(logDetails.payload)
        ).filter { it.isNotEmpty() }.joinToString(separator = "\n")
    }

    private fun getStackTraceString(throwable: Throwable?): String {
        return throwable?.stackTraceToString() ?: ""
    }

    private fun getArgumentsString(args: Map<String, String>): String {
        return if (args.isNotEmpty()) {
            args.map { "\'${it.key}\': \'${it.value}\'" }.joinToString(separator = ",").let {
                "Arguments {$it}"
            }
        } else {
            ""
        }
    }
}