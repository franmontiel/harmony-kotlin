package com.mobilejazz.harmony.kotlin.core.repository.operation

// Operations

open class Operation

object DefaultOperation : Operation()

// Data stream will only use the main data source
object MainOperation : Operation()

// Data stream will use the main data source and then sync result with the cache data source
object MainSyncOperation: Operation()

// Data stream will only use the cache data source
object CacheOperation : Operation()

// Data stream will use the cache data source and sync with the main data source
object CacheSyncOperation: Operation()

