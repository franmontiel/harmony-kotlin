package com.mobilejazz.kotlin.core.repository.datasource

import com.mobilejazz.kotlin.core.repository.error.QueryNotSupportedException
import com.mobilejazz.kotlin.core.repository.query.IdQuery
import com.mobilejazz.kotlin.core.repository.query.IdsQuery
import com.mobilejazz.kotlin.core.repository.query.Query
import com.mobilejazz.kotlin.core.threading.extensions.Future

interface DataSource {

  fun notSupportedQuery(): Nothing = throw QueryNotSupportedException("Query not supported")
}

// DataSources
interface GetDataSource<V> : DataSource {
  fun get(query: Query): Future<V>

  fun getAll(query: Query): Future<List<V>>
}

interface PutDataSource<V> : DataSource {
  fun put(query: Query, value: V?): Future<V>

  fun putAll(query: Query, value: List<V>? = emptyList()): Future<List<V>>
}

interface DeleteDataSource : DataSource {
  fun delete(query: Query): Future<Unit>

  fun deleteAll(query: Query): Future<Unit>
}

// Extensions
fun <K, V> GetDataSource<V>.get(id: K): Future<V> = get(IdQuery(id))

fun <K, V> GetDataSource<V>.getAll(ids: List<K>): Future<List<V>> = getAll(IdsQuery(ids))

fun <K, V> PutDataSource<V>.put(id: K, value: V?): Future<V> = put(IdQuery(id), value)

fun <K, V> PutDataSource<V>.putAll(ids: List<K>, values: List<V>?) = putAll(IdsQuery(ids), values)

fun <K> DeleteDataSource.delete(id: K): Future<Unit> = delete(IdQuery(id))

fun <K> DeleteDataSource.deleteAll(ids: List<K>): Future<Unit> = deleteAll(IdsQuery(ids))
