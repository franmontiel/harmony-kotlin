package com.harmony.kotlin.data.query

// Queries

open class Query

object VoidQuery : Query()

// Single object query
open class ObjectQuery<out T>(val value: T) : Query()

// Collection objects query
open class ObjectsQuery<out T>(val values: Collection<T>) : Query()

// Generic all object query supporting key value
open class AllObjectsQuery : Query()

// Key value queries
open class KeyQuery(val key: String) : Query()
