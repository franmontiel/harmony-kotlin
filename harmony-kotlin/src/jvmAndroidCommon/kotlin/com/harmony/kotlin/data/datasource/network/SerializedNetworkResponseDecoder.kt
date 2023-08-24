package com.harmony.kotlin.data.datasource.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

actual class SerializedNetworkResponseDecoder<T> actual constructor(private val stringFormat: StringFormat, private val serializer: KSerializer<T>) :
  NetworkResponseDecoder<T>() {
  actual override suspend fun decode(httpResponse: HttpResponse): T {
    return if (stringFormat is Json) {
      stringFormat.decodeFromStream(serializer, httpResponse.bodyAsChannel().toInputStream())
    } else {
      stringFormat.decodeFromString(serializer, httpResponse.bodyAsText())
    }
  }
}
