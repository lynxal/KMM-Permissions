package com.lynxal.logging

import android.os.Build
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class DebugLoggerImplementation : LoggerImplementation {
    companion object {
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
        private const val MAX_TAG_LENGTH = 23
    }

    private val defaultTag: String
        get() = Throwable().stackTrace
            .first { !it.className.startsWith("com.lynxal.logging") }
            .let(::createStackElementTag)

    actual override fun log(logDetails: LogDetails, loggerExtras: LoggerExtras) {
        Log.println(
            logDetails.logLevel.level,
            loggerExtras.tag.ifBlank { defaultTag },
            formatMessage(logDetails)
        )
    }

    private fun formatMessage(logDetails: LogDetails): String {
        return listOfNotNull(
            logDetails.message,
            getStackTraceString(logDetails.cause),
            getArgumentsString(logDetails.payload)
        ).filter { it.isNotEmpty() }.joinToString(separator = "\n")
    }

    private fun getStackTraceString(throwable: Throwable?): String {
        if (throwable == null)
            return ""

        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
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

    /**
     * Extract the tag which should be used for the message from the `element`. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     *
     * Note: This will not be called if a [manual tag][.tag] was specified.
     */
    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className.substringAfterLast('.')
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        // Tag length limit was removed in API 26.
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }
    }
}