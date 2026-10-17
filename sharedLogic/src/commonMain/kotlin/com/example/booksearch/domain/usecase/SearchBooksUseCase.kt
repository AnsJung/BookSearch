package com.example.booksearch.domain.usecase

import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchPage
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import kotlin.coroutines.cancellation.CancellationException

/** 제목 검색 결과의 지정한 페이지를 가져온다. */
class SearchBooksUseCase(private val repository: BookRepository) {
    @Throws(IllegalArgumentException::class, BookSearchException::class, CancellationException::class)
    suspend operator fun invoke(query: String, sort: BookSort, page: Int, size: Int): BookSearchPage {
        val keyword = query.trim()
        require(keyword.isNotEmpty()) { "검색어가 비어 있습니다." }
        require(page in 1..50) { "페이지는 1~50 사이여야 합니다." }
        require(size in 1..50) { "페이지 크기는 1~50 사이여야 합니다." }
        return repository.searchByTitlePage(keyword, sort, page, size)
    }
}
