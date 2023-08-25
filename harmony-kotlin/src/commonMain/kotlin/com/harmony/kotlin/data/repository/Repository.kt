package com.harmony.kotlin.data.repository

import com.harmony.kotlin.data.mapper.Mapper
import com.harmony.kotlin.data.operation.DefaultOperation
import com.harmony.kotlin.data.operation.Operation
import com.harmony.kotlin.data.query.Query

// Repositories
interface GetRepository<Q : Query, V> {
  suspend fun get(query: Q, operation: Operation = DefaultOperation): V
}

interface PutRepository<Q : Query, V> {
  suspend fun put(query: Q, value: V?, operation: Operation = DefaultOperation): V
}

interface DeleteRepository<Q : Query> {
  suspend fun delete(query: Q, operation: Operation = DefaultOperation)
}

// Extensions
fun <K, V> GetRepository<Query, K>.withMapping(mapper: Mapper<K, V>): GetRepository<Query, V> = GetRepositoryMapper(this, mapper)

fun < K, V> PutRepository<Query, K>.withMapping(toMapper: Mapper<K, V>, fromMapper: Mapper<V, K>): PutRepository<*, V> =
  PutRepositoryMapper<K,V>(this, toMapper, fromMapper)
