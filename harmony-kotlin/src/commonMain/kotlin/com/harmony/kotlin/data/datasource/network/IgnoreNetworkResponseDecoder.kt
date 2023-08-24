package com.harmony.kotlin.data.datasource.network

import io.ktor.client.statement.HttpResponse

/**
 * Helper class that ignores [HttpResponse] body and just return [Unit].
 */
class IgnoreNetworkResponseDecoder internal constructor() : NetworkResponseDecoder<Unit>() {
  override suspend fun decode(httpResponse: HttpResponse) {
    return
  }
}
