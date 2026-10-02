package com.example.booksearch.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookDocumentsDTO(
    val title: String,
    val contents: String? = null,
    val url: String? = null,
    val isbn: String? = null,
    val datetime: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val translators: List<String>? = null,
    val price: Int? = null,
    @SerialName("sale_price")
    val salePrice: Int? = null,
    val thumbnail: String? = null,
    val status: String? = null
)
