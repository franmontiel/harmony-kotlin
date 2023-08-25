package com.harmony.kotlin.domain.interactor

import com.harmony.kotlin.data.operation.DefaultOperation
import com.harmony.kotlin.data.operation.Operation
import com.harmony.kotlin.data.query.Query
import com.harmony.kotlin.data.query.VoidQuery
import com.harmony.kotlin.data.repository.DeleteRepository
import com.harmony.kotlin.data.repository.GetRepository
import com.harmony.kotlin.data.repository.PutRepository
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class GetInteractor<Q : Query, M>(private val coroutineContext: CoroutineContext, private val getRepository: GetRepository<Q, M>) {

  suspend operator fun invoke(query: Q, operation: Operation = DefaultOperation): M =
    withContext(coroutineContext) {
      getRepository.get(query, operation)
    }
}

class PutInteractor<Q : Query, M>(private val coroutineContext: CoroutineContext, private val putRepository: PutRepository<Q, M>) {

  suspend operator fun invoke(m: M? = null, query: Q, operation: Operation = DefaultOperation): M =
    withContext(coroutineContext) {
      putRepository.put(query, m, operation)
    }
}

class DeleteInteractor<Q : Query>(private val coroutineContext: CoroutineContext, private val deleteRepository: DeleteRepository<Q>) {

  suspend operator fun invoke(query: Q, operation: Operation = DefaultOperation) =
    withContext(coroutineContext) {
      deleteRepository.delete(query, operation)
    }
}

fun <Q : Query, V> GetRepository<Q, V>.toGetInteractor(coroutineContext: CoroutineContext) = GetInteractor(coroutineContext, this)

fun <Q : Query, V> PutRepository<Q, V>.toPutInteractor(coroutineContext: CoroutineContext) = PutInteractor(coroutineContext, this)

fun <Q : Query> DeleteRepository<Q>.toDeleteInteractor(coroutineContext: CoroutineContext) = DeleteInteractor(coroutineContext, this)
