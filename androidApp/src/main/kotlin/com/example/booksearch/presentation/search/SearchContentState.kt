package com.example.booksearch.presentation.search

import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure

/** 첫 페이지 로드 상태에 따른 검색 본문. */
internal sealed interface SearchContentState {
    data object PreSearch : SearchContentState
    data object Loading : SearchContentState
    data object Empty : SearchContentState
    data object Results : SearchContentState
    data class Error(val message: String, val canRetry: Boolean) : SearchContentState
}

internal fun Throwable.toSearchError(): SearchContentState.Error {
    val failure = (this as? BookSearchException)?.failure ?: BookSearchFailure.UNKNOWN
    return SearchContentState.Error(failure.userMessage(), failure.canRetry())
}

private fun BookSearchFailure.userMessage(): String = when (this) {
    BookSearchFailure.NETWORK -> "인터넷 연결을 확인한 뒤 다시 시도해 주세요."
    BookSearchFailure.TIMEOUT -> "응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요."
    BookSearchFailure.SERVER -> "서비스에 일시적인 문제가 있어요. 잠시 후 다시 시도해 주세요."
    BookSearchFailure.AUTHENTICATION, BookSearchFailure.PERMISSION_DENIED -> "현재 도서 검색 서비스를 이용할 수 없어요."
    BookSearchFailure.RATE_LIMITED -> "검색 이용량이 많아 잠시 후 다시 이용해 주세요."
    BookSearchFailure.INVALID_REQUEST -> "검색 요청을 처리할 수 없어요."
    BookSearchFailure.RESPONSE_PARSING -> "도서 정보를 처리하지 못했어요."
    BookSearchFailure.UNKNOWN -> "잠시 후 다시 시도해 주세요."
}

private fun BookSearchFailure.canRetry(): Boolean = when (this) {
    BookSearchFailure.NETWORK, BookSearchFailure.TIMEOUT, BookSearchFailure.SERVER -> true
    else -> false
}
