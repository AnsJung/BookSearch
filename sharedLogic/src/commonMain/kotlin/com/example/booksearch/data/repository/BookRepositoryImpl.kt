package com.example.booksearch.data.repository

import com.example.booksearch.data.mapper.toBook
import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository

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
        return kakaoBookApi.searchBooks(query, sort)
            .documents
            .map { it.toBook() }
    }
}