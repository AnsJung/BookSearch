package com.example.booksearch.di

import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.data.remote.createBookHttpClient
import com.example.booksearch.data.repository.BookRepositoryImpl
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 도서 검색 기능에 필요한 의존성을 Koin에 등록한다.
 */
internal fun bookModule(apiKey: String): Module = module {

    single<HttpClient> { createBookHttpClient(apiKey) }

    single<KakaoBookApi> { KakaoBookApi(get()) }

    single<BookRepository> {
        BookRepositoryImpl(get())
    }

    factory<SearchBooksUseCase> { SearchBooksUseCase(get()) }
}
