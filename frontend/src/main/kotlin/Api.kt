package com.code_labeler

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

private const val endpointsUrl = "http://0.0.0.0:8080/"
private const val endpointPort = 8080

suspend fun apiRequest(id: String): String? {
    val client = HttpClient(Js)
    val response: HttpResponse = client.get(endpointsUrl + id) {
        port = endpointPort
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
    }
    client.close()
    if (response.status != HttpStatusCode.OK) return null
    return response.receive()
}