package com.harmony.kotlin.common

actual fun currentThreadName(): String {
  return Thread.currentThread().name
}
