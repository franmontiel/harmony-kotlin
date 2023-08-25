package com.harmony.kotlin.domain.interactor.either

import com.harmony.kotlin.common.either.Either
import com.harmony.kotlin.common.either.eitherOf
import com.harmony.kotlin.data.operation.DefaultOperation
import com.harmony.kotlin.data.operation.Operation
import com.harmony.kotlin.data.query.Query
import com.harmony.kotlin.data.query.VoidQuery
import com.harmony.kotlin.data.repository.DeleteRepository
import com.harmony.kotlin.data.repository.GetRepository
import com.harmony.kotlin.data.repository.PutRepository
import com.harmony.kotlin.error.HarmonyException
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class GetInteractor<Q : Query, M>(val coroutineContext: CoroutineContext, val getRepository: GetRepository<Q, M>) {

  suspend inline operator fun <reified E : HarmonyException> invoke(query: Q, operation: Operation = DefaultOperation): Either<E, M> =
    withContext(coroutineContext) {
      eitherOf { getRepository.get(query, operation) }
    }
}

class PutInteractor<Q : Query, M>(val coroutineContext: CoroutineContext, val putRepository: PutRepository<Q, M>) {

  suspend inline operator fun <reified E : HarmonyException> invoke(m: M? = null, query: Q, operation: Operation = DefaultOperation): Either<E, M> =
    withContext(coroutineContext) {
      eitherOf { putRepository.put(query, m, operation) }
    }
}

class DeleteInteractor<Q : Query>(val coroutineContext: CoroutineContext, val deleteRepository: DeleteRepository<Q>) {

  suspend inline operator fun <reified E : HarmonyException> invoke(query: Q, operation: Operation = DefaultOperation): Either<E, Unit> =
    withContext(coroutineContext) {
      eitherOf { deleteRepository.delete(query, operation) }
    }
}

fun <Q : Query, V> GetRepository<Q, V>.toGetInteractor(coroutineContext: CoroutineContext) = GetInteractor(coroutineContext, this)

fun <Q : Query, V> PutRepository<Q, V>.toPutInteractor(coroutineContext: CoroutineContext) = PutInteractor(coroutineContext, this)

fun <Q : Query> DeleteRepository<Q>.toDeleteInteractor(coroutineContext: CoroutineContext) = DeleteInteractor(coroutineContext, this)
