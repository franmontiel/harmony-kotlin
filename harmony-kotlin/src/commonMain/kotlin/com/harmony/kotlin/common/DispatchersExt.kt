package com.harmony.kotlin.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

/**
 * A [CoroutineDispatcher] that is backed by a single background thread.
 * This is an extension val that is not part of the default kotlinx.coroutines library.
 */
val Dispatchers.SingleThread: CoroutineDispatcher by lazy {
  newFixedThreadPoolContext(1, "Dispatchers.SingleThread")
}
