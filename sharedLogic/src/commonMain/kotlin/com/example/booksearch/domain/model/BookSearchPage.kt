package com.example.booksearch.domain.model

/** 검색 결과 한 페이지, 다음 페이지 존재 여부와 전체 검색 건수. */
data class BookSearchPage(
    val books: List<Book>,
    val isEnd: Boolean,
    val totalCount: Int? = null,
)
