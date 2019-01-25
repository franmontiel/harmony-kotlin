package com.mobilejazz.kotlin.core.repository.query

// Queries

open class Query

object VoidQuery : Query()

// Single object query
open class ObjectQuery<out T>(val value: T) : Query()

// Collection objects query
open class ObjectsQuery<out T>(val values: Collection<T>) : Query()

// Generic all object query supporting key value
open class AllObjectsQuery : KeyQuery<String>("all-objects-key")

open class IdQuery<out T>(val identifier: T) : KeyQuery<T>(identifier)

open class IntegerIdQuery(val id: Int) : IdQuery<Int>(id)

open class IdsQuery<out T>(val identifiers: Collection<T>) : KeyQuery<String>(identifiers.toString())

open class IntegerIdsQuery(val ids: Collection<Int>) : IdsQuery<Int>(ids)

open class PaginationQuery : Query()

open class PaginationOffsetLimitQuery(val offset: Int, val limit: Int) : PaginationQuery()

// Key value queries
open class KeyQuery<out T>(val key: T) : Query()

open class StringKeyQuery(key: String) : KeyQuery<String>(key)

// Extensions
inline fun <reified T> IdQuery<*>.asTyped(): IdQuery<T>? = (this.identifier as? T)?.let { IdQuery(it) }

inline fun <reified T> KeyQuery<*>.asTyped(): KeyQuery<T>? = (this.key as? T)?.let { KeyQuery(it) }

fun <T> KeyQuery<*>.isTyped(type: Class<T>): Boolean {
  return type.isAssignableFrom(this.key!!::class.java.componentType)
}