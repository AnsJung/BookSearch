package com.example.booksearch.domain.repository

import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort

/**
 * 도서 검색 API를 호출하는 Repository
 * - 검색어를 기반으로 도서 정보를 조회한다.
 */
interface BookRepository {
    suspend fun searchByTitle(
        query: String,
        sort: BookSort,
    ): List<Book>
}
