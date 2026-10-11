package com.example.booksearch.domain.model

/** 도서 검색 실패를 플랫폼 UI에서 구분할 수 있도록 하는 공통 오류 유형. */
enum class BookSearchFailure {
    NETWORK,
    TIMEOUT,
    AUTHENTICATION,
    PERMISSION_DENIED,
    RATE_LIMITED,
    INVALID_REQUEST,
    SERVER,
    RESPONSE_PARSING,
    UNKNOWN,
}

/** 데이터 계층에서 분류한 검색 실패를 도메인과 UI에 전달한다. */
class BookSearchException(
    val failure: BookSearchFailure,
    cause: Throwable? = null,
) : Exception(failure.name, cause)
