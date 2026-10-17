package com.example.booksearch.data.remote

import com.example.booksearch.data.remote.dto.BookSearchResponseDTO
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException

/**
 * 카카오 도서 검색 API를 호출하는 클라이언트.
 */
internal class KakaoBookApi(
    private val client: HttpClient,
) {
    /**
     * 검색어와 정렬 조건으로 도서를 검색하고 응답 DTO를 반환한다.
     */
    suspend fun searchBooks(
        query: String,
        sort: BookSort,
        page: Int = 1,
        size: Int = 20,
    ): BookSearchResponseDTO {
        return client.get(KakaoApiConfig.BOOK_SEARCH_PATH) {
            parameter("query", query)
            parameter("sort", sort.name.lowercase())
            parameter("target", "title")
            parameter("page", page)
            parameter("size", size)
        }
            .let { response ->
                try {
                    response.body<BookSearchResponseDTO>()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (network: IOException) {
                    throw network
                } catch (error: Exception) {
                    throw BookSearchException(BookSearchFailure.RESPONSE_PARSING, error)
                }
            }
    }
}
