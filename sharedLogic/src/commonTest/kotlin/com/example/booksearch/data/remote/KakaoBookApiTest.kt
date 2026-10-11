package com.example.booksearch.data.remote

import com.example.booksearch.domain.model.BookSort
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KakaoBookApiTest {
    @Test
    fun sendsGetWithAuthorizationAndSafelyEncodedQuery() = runTest {
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("https", request.url.protocol.name)
            assertEquals("dapi.kakao.com", request.url.host)
            assertEquals("/v3/search/book", request.url.encodedPath)
            assertEquals("KakaoAK test-key", request.headers[HttpHeaders.Authorization])
            assertEquals("코틀린 & KMP + 입문", request.url.parameters["query"])
            respond(EMPTY_RESPONSE, headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            KakaoBookApi(client).searchBooks("코틀린 & KMP + 입문", BookSort.ACCURACY)
            assertEquals(1, calls)
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    fun mapsBothSortOptionsToKakaoValues() = runTest {
        for ((sort, expected) in listOf(BookSort.ACCURACY to "accuracy", BookSort.LATEST to "latest")) {
            val engine = MockEngine { request ->
                assertEquals(expected, request.url.parameters["sort"])
                respond(EMPTY_RESPONSE, headers = JSON_HEADERS)
            }
            val client = HttpClient(engine) { configureBookClient("test-key") }
            try {
                KakaoBookApi(client).searchBooks("책", sort)
            } finally {
                client.close()
                engine.close()
            }
        }
    }

    @Test
    fun searchesTitlesOnTheFirstPageWithTwentyResults() = runTest {
        val engine = MockEngine { request ->
            assertEquals("title", request.url.parameters["target"])
            assertEquals("1", request.url.parameters["page"])
            assertEquals("20", request.url.parameters["size"])
            respond(EMPTY_RESPONSE, headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            KakaoBookApi(client).searchBooks("책", BookSort.ACCURACY)
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    fun deserializesResponseUsingSharedJsonConfiguration() = runTest {
        val engine = MockEngine {
            respond("""{
                "meta":{"total_count":1,"pageable_count":1,"is_end":true,"future_meta":42},
                "documents":[{"title":"코틀린","sale_price":0,"future_book":{}}],
                "future_root":[]
            }""", headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            val response = KakaoBookApi(client).searchBooks("책", BookSort.ACCURACY)
            assertEquals(1, response.meta.totalCount)
            assertEquals(true, response.meta.isEnd)
            val document = response.documents.single()
            assertEquals("코틀린", document.title)
            assertEquals(0, document.salePrice)
            assertNull(document.price)
            assertNull(document.authors)
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestTimesOutAfterFifteenSecondsWithoutRetry() = runTest {
        var calls = 0
        val engine = MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler {
                calls++
                delay(30_000L)
                respond(EMPTY_RESPONSE, headers = JSON_HEADERS)
            }
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            assertFailsWith<HttpRequestTimeoutException> {
                KakaoBookApi(client).searchBooks("책", BookSort.ACCURACY)
            }
            assertEquals(15_000L, testScheduler.currentTime)
            assertEquals(1, calls)
        } finally {
            client.close()
            engine.close()
        }
    }

    companion object {
        private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
        private const val EMPTY_RESPONSE =
            """{"meta":{"total_count":0,"pageable_count":0,"is_end":true},"documents":[]}"""
    }
}
