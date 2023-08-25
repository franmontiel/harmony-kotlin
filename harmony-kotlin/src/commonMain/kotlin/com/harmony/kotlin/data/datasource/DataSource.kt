package com.harmony.kotlin.data.datasource

import com.harmony.kotlin.data.mapper.Mapper
import com.harmony.kotlin.data.query.Query
import com.harmony.kotlin.data.repository.GetRepository
import com.harmony.kotlin.data.repository.PutRepository
import com.harmony.kotlin.data.repository.SingleDeleteDataSourceRepository
import com.harmony.kotlin.data.repository.SingleGetDataSourceRepository
import com.harmony.kotlin.data.repository.SinglePutDataSourceRepository
import com.harmony.kotlin.data.repository.withMapping

// DataSources
interface GetDataSource<Q : Query, V> {
  suspend fun get(query: Query): V
}

interface PutDataSource<Q : Query, V> {
  suspend fun put(query: Q, value: V?): V
}

interface DeleteDataSource<Q : Query> {
  suspend fun delete(query: Q)
}

// Extensions to create
fun <Q : Query, V> GetDataSource<Q, V>.toGetRepository() = SingleGetDataSourceRepository<Q>(this)

fun <K, V> GetDataSource<Query, K>.toGetRepository(mapper: Mapper<K, V>): GetRepository<Query,V> = toGetRepository().withMapping(mapper)

fun <V> PutDataSource<V>.toPutRepository() = SinglePutDataSourceRepository(this)

fun <K, V> PutDataSource<K>.toPutRepository(toMapper: Mapper<K, V>, fromMapper: Mapper<V, K>): PutRepository<V> =
  toPutRepository().withMapping(toMapper, fromMapper)

fun DeleteDataSource.toDeleteRepository() = SingleDeleteDataSourceRepository(this)
