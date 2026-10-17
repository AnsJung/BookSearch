package com.example.booksearch.data

import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.data.remote.configureBookClient
import com.example.booksearch.data.repository.BookRepositoryImpl
import com.example.booksearch.domain.model.BookSort
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookRepositoryTest {
    @Test
    fun mapsDocumentsAndPreservesMissingPriceVersusZero() = runTest {
        val engine = MockEngine { request ->
            assertEquals("KakaoAK test-key", request.headers[HttpHeaders.Authorization])
            assertEquals("title", request.url.parameters["target"])
            assertEquals("latest", request.url.parameters["sort"])
            respond("""{
                "meta":{"total_count":2,"pageable_count":2,"is_end":true},
                "documents":[
                    {"title":"첫 책","contents":"소개","isbn":"8996991341 9788996991342"},
                    {"title":"둘째 책","authors":["저자"],"price":0,"sale_price":0}
                ]
            }""", headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            val repository = BookRepositoryImpl(KakaoBookApi(client))
            val books = repository.searchByTitle("책", BookSort.LATEST)
            assertEquals(listOf("첫 책", "둘째 책"), books.map { it.title })
            assertEquals("소개", books[0].contents)
            assertEquals("8996991341 9788996991342", books[0].isbn)
            assertEquals(emptyList(), books[0].authors)
            assertEquals(emptyList(), books[0].translators)
            assertNull(books[0].price)
            assertNull(books[0].salePrice)
            assertEquals(listOf("저자"), books[1].authors)
            assertEquals(0, books[1].price)
            assertEquals(0, books[1].salePrice)
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    fun emptyDocumentsReturnsAnEmptyBookList() = runTest {
        val engine = MockEngine {
            respond("""{"meta":{"total_count":0,"pageable_count":0,"is_end":true},"documents":[]}""",
                headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            val repository = BookRepositoryImpl(KakaoBookApi(client))
            assertEquals(emptyList(), repository.searchByTitle("책", BookSort.ACCURACY))
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    fun pageSearchKeepsKakaoEndFlag() = runTest {
        val engine = MockEngine { request ->
            assertEquals("2", request.url.parameters["page"])
            respond("""{
                "meta":{"total_count":40,"pageable_count":40,"is_end":false},
                "documents":[{"title":"다음 책"}]
            }""", headers = JSON_HEADERS)
        }
        val client = HttpClient(engine) { configureBookClient("test-key") }
        try {
            val page = BookRepositoryImpl(KakaoBookApi(client))
                .searchByTitlePage("책", BookSort.ACCURACY, page = 2, size = 20)
            assertEquals(listOf("다음 책"), page.books.map { it.title })
            assertEquals(false, page.isEnd)
        } finally {
            client.close()
            engine.close()
        }
    }

    companion object {
        private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
