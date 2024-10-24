package com.lynxal.logging

/**
 * LogLevel class mirrors Android's default Log levels, and keeps the same integer representation values
 */
enum class LogLevel(val level: Int) {
    Verbose(2), Debug(3), Info(4), Warning(5), Error(6);

    companion object
}
