package com.example.booksearch.data.mapper

import com.example.booksearch.data.remote.dto.BookDocumentsDTO
import com.example.booksearch.domain.model.Book

/**
 * BookDocumentsDTO를 Book으로 변환하는 확장 함수
 */
fun BookDocumentsDTO.toBook(): Book{
    return Book(
        title = this.title,
        contents = this.contents ?: "",
        url = this.url ?: "",
        isbn = this.isbn ?: "",
        datetime = this.datetime ?: "",
        authors = this.authors ?: emptyList(),
        publisher = this.publisher ?: "",
        translators = this.translators ?: emptyList(),
        price = this.price,
        salePrice = this.salePrice,
        thumbnail = this.thumbnail ?: "",
        status = this.status ?: ""
    )
}