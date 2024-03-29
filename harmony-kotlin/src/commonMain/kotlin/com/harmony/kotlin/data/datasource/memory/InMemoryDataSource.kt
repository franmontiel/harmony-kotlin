package com.harmony.kotlin.data.datasource.memory

import com.harmony.kotlin.data.datasource.DeleteDataSource
import com.harmony.kotlin.data.datasource.GetDataSource
import com.harmony.kotlin.data.datasource.PutDataSource
import com.harmony.kotlin.data.query.AllObjectsQuery
import com.harmony.kotlin.data.query.KeyQuery
import com.harmony.kotlin.data.query.Query
import com.harmony.kotlin.error.DataNotFoundException
import com.harmony.kotlin.error.notSupportedQuery
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryDataSource<V> : GetDataSource<V>, PutDataSource<V>, DeleteDataSource {

  private val objects: MutableMap<String, V> = mutableMapOf()
  private val mutex = Mutex()

  override suspend fun get(query: Query): V =
    mutex.withLock {
      when (query) {
        is KeyQuery -> {
          objects[query.key].run {
            this ?: throw DataNotFoundException()
          }
        }

        else -> notSupportedQuery()
      }
    }

  override suspend fun put(query: Query, value: V?): V =
    mutex.withLock {
      when (query) {
        is KeyQuery -> {
          value?.let {
            objects.put(query.key, value).run { value }
          } ?: throw IllegalArgumentException("InMemoryDataSource: value must be not null")
        }

        else -> notSupportedQuery()
      }
    }

  override suspend fun delete(query: Query) {
    mutex.withLock {
      when (query) {
        is AllObjectsQuery -> {
          objects.clear()
        }

        is KeyQuery -> {
          clearAll(query.key)
        }

        else -> notSupportedQuery()
      }
    }
  }

  private fun clearAll(key: String) {
    objects.remove(key)
  }
}
