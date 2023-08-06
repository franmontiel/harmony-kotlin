package com.harmony.kotlin.data.datasource.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat

actual class SerializedNetworkResponseDecoder<T> actual constructor(private val stringFormat: StringFormat, private val serializer: KSerializer<T>) :
  NetworkResponseDecoder<T>() {
  actual override suspend fun decode(httpResponse: HttpResponse): T {
    return stringFormat.decodeFromString(serializer, httpResponse.bodyAsText())
  }
}
