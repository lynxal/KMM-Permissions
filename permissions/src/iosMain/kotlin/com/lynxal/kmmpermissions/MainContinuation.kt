package com.lynxal.kmmpermissions

import platform.Foundation.NSThread

internal inline fun <T1> mainContinuation(
    noinline block: (T1) -> Unit
): (T1) -> Unit = { arg1 ->
    if (NSThread.isMainThread()) {
        block(arg1)
    } else {
        MainLoopDispatcher.run {
            block(arg1)
        }
    }
}
internal inline fun <T1, T2> mainContinuation(
    noinline block: (T1, T2) -> Unit
): (T1, T2) -> Unit = { arg1, arg2 ->
    if (NSThread.isMainThread()) {
        block(arg1, arg2)
    } else {
        MainLoopDispatcher.run {
            block(arg1, arg2)
        }
    }
}

