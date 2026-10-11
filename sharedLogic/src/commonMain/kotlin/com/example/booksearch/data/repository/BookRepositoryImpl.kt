package com.example.booksearch.data.repository

import com.example.booksearch.data.mapper.toBook
import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.data.remote.classifyKakaoError
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException

/**
 * 도서 검색 Repository 구현체.
 */
internal class BookRepositoryImpl(
    private val kakaoBookApi: KakaoBookApi
) : BookRepository {
    override suspend fun searchByTitle(
        query: String,
        sort: BookSort
    ): List<Book> {
        return try {
            kakaoBookApi.searchBooks(query, sort).documents.map { it.toBook() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (classified: BookSearchException) {
            throw classified
        } catch (response: ResponseException) {
            throw BookSearchException(classifyKakaoError(response), response)
        } catch (timeout: HttpRequestTimeoutException) {
            throw BookSearchException(BookSearchFailure.TIMEOUT, timeout)
        } catch (timeout: ConnectTimeoutException) {
            throw BookSearchException(BookSearchFailure.TIMEOUT, timeout)
        } catch (timeout: SocketTimeoutException) {
            throw BookSearchException(BookSearchFailure.TIMEOUT, timeout)
        } catch (network: IOException) {
            throw BookSearchException(BookSearchFailure.NETWORK, network)
        } catch (invalidResponse: IllegalArgumentException) {
            throw BookSearchException(BookSearchFailure.RESPONSE_PARSING, invalidResponse)
        } catch (unknown: Exception) {
            throw BookSearchException(BookSearchFailure.UNKNOWN, unknown)
        }
    }
}
