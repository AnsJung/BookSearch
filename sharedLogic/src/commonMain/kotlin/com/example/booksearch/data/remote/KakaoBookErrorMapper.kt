package com.example.booksearch.data.remote

import com.example.booksearch.domain.model.BookSearchFailure
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

/** 카카오 오류 본문의 코드가 있으면 우선 적용하고, 없으면 HTTP 상태로 분류한다. */
internal suspend fun classifyKakaoError(error: ResponseException): BookSearchFailure {
    val body = try {
        error.response.bodyAsText()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
    val details = body?.let {
        try {
            Json.parseToJsonElement(it).jsonObject
        } catch (_: Exception) {
            null
        }
    }
    val code = (details?.get("code") as? JsonPrimitive)?.intOrNull
    val errorType = (details?.get("errorType") as? JsonPrimitive)?.content

    return when {
        errorType == "RequestThrottled" || code in setOf(-10, -11) -> BookSearchFailure.RATE_LIMITED
        code in setOf(-401, -903) -> BookSearchFailure.AUTHENTICATION
        code in setOf(-3, -4, -5, -6, -12, -13) -> BookSearchFailure.PERMISSION_DENIED
        code in setOf(-2, -8) -> BookSearchFailure.INVALID_REQUEST
        code in setOf(-1, -7, -9798) -> BookSearchFailure.SERVER
        error.response.status.value == 401 -> BookSearchFailure.AUTHENTICATION
        error.response.status.value == 403 -> BookSearchFailure.PERMISSION_DENIED
        error.response.status.value == 429 -> BookSearchFailure.RATE_LIMITED
        error.response.status.value in 500..599 -> BookSearchFailure.SERVER
        else -> BookSearchFailure.UNKNOWN
    }
}
