package com.example.booksearch.domain.model

/**
 * 도서 정보를 나타내는 데이터 클래스
 */
data class Book(
    val title: String,
    val contents: String,
    val url: String,
    val isbn: String,
    val datetime: String,
    val authors: List<String>,
    val publisher: String,
    val translators: List<String>,
    val price: Int?,
    val salePrice: Int?,
    val thumbnail: String,
    val status: String
)
