package com.example.booksearch.presentation.model

import com.example.booksearch.domain.model.BookSort

/**
 * 도서 검색 화면의 UI 상태를 나타내는 데이터 클래스
 */
data class BookSearchUiState(
    val query: String = "",
    val submittedQuery: String? = null,
    val sort: BookSort = BookSort.ACCURACY,
    val requestId: Long = 0L,
    val totalCount: Int? = null,
    val queryErrorMessage: String? = null,
)
