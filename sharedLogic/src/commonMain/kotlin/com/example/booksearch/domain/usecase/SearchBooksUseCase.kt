package com.example.booksearch.domain.usecase

import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * 도서 검색 UseCase
 * - 검색어를 기반으로 도서 정보를 조회한다.
 */
class SearchBooksUseCase(
    private val repository: BookRepository,
) {
    @Throws(BookSearchException::class, IllegalArgumentException::class, CancellationException::class)
    suspend operator fun invoke(
        query: String,
        sort: BookSort,
    ): List<Book> {
        val keyword = query.trim()
        require(keyword.isNotEmpty()) { "검색어가 비어 있습니다." }

        return repository.searchByTitle(keyword, sort)
    }
}
