package com.lynxal.logging

interface LoggerImplementation {
    fun log(
        logDetails: LogDetails, loggerExtras: LoggerExtras
    )
}