package com.harmony.kotlin.common

import platform.Foundation.NSThread

actual fun currentThreadName(): String {
  return NSThread.currentThread.name.let { threadName ->
    if (threadName.isNullOrBlank()) "Unknown" else threadName
  }
}
