package com.example.booksearch.domain.model

/** 검색 결과 한 페이지와 다음 페이지 존재 여부. */
data class BookSearchPage(
    val books: List<Book>,
    val isEnd: Boolean,
)
