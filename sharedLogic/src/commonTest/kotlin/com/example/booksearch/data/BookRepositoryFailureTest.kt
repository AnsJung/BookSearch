package com.example.booksearch.data

import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.data.remote.configureBookClient
import com.example.booksearch.data.repository.BookRepositoryImpl
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.model.BookSort
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookRepositoryFailureTest {
    @Test
    fun kakaoCodesTakePriorityOverHttpStatus() = runTest {
        val cases = listOf(
            Triple(HttpStatusCode.BadRequest, """{"code":-10}""", BookSearchFailure.RATE_LIMITED),
            Triple(HttpStatusCode.BadRequest, """{"code":-1}""", BookSearchFailure.SERVER),
            Triple(HttpStatusCode.BadRequest, """{"code":-2}""", BookSearchFailure.INVALID_REQUEST),
            Triple(HttpStatusCode.BadRequest, """{"code":777,"errorType":"RequestThrottled"}""", BookSearchFailure.RATE_LIMITED),
            Triple(HttpStatusCode.BadRequest, """{"code":-401}""", BookSearchFailure.AUTHENTICATION),
        )
        cases.forEach { (status, body, expected) ->
            assertEquals(expected, failureFor(status, body))
        }
    }

    @Test
    fun malformedErrorBodyKeepsTheHttpStatusMeaning() = runTest {
        assertEquals(BookSearchFailure.SERVER, failureFor(HttpStatusCode.ServiceUnavailable, "not json"))
        assertEquals(BookSearchFailure.AUTHENTICATION, failureFor(HttpStatusCode.Unauthorized, "not json"))
        assertEquals(BookSearchFailure.PERMISSION_DENIED, failureFor(HttpStatusCode.Forbidden, "not json"))
        assertEquals(BookSearchFailure.RATE_LIMITED, failureFor(HttpStatusCode.TooManyRequests, "not json"))
        assertEquals(BookSearchFailure.UNKNOWN, failureFor(HttpStatusCode.BadRequest, "not json"))
    }

    @Test
    fun invalidSuccessfulResponseIsAParsingFailure() = runTest {
        assertEquals(BookSearchFailure.RESPONSE_PARSING, failureFor(HttpStatusCode.OK, "not json"))
    }

    @Test
    fun connectionFailureIsARecoverableNetworkFailure() = runTest {
        val engine = MockEngine { throw IOException("offline") }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            val error = assertFailsWith<BookSearchException> {
                BookRepositoryImpl(KakaoBookApi(client)).searchByTitle("책", BookSort.ACCURACY)
            }
            assertEquals(BookSearchFailure.NETWORK, error.failure)
        } finally {
            client.close()
            engine.close()
        }
    }

    private suspend fun failureFor(status: HttpStatusCode, body: String): BookSearchFailure {
        val engine = MockEngine {
            respond(body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        return try {
            assertFailsWith<BookSearchException> {
                BookRepositoryImpl(KakaoBookApi(client)).searchByTitle("책", BookSort.ACCURACY)
            }.failure
        } finally {
            client.close()
            engine.close()
        }
    }
}
