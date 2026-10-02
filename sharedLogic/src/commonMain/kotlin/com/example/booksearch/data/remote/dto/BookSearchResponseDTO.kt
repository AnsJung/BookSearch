package com.example.booksearch.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * @property meta 검색 결과에 대한 메타 정보
 * @property documents 검색 결과 문서 리스트
 */
@Serializable
internal data class BookSearchResponseDTO(
    val meta : BookMetaDTO,
    val documents : List<BookDocumentsDTO>
)