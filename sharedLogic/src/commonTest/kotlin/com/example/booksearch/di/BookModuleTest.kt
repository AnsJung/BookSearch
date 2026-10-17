package com.example.booksearch.di

import com.example.booksearch.data.remote.KakaoBookApi
import com.example.booksearch.data.repository.BookRepositoryImpl
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import com.example.booksearch.domain.repository.BookRepository
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class BookModuleTest {
    @Test
    fun resolvesDependenciesWithDeclaredLifetimesAndInterfaceBinding() {
        val application = koinApplication { modules(bookModule("test-key")) }
        var client: HttpClient? = null
        try {
            val koin = application.koin
            client = koin.get<HttpClient>()
            assertSame(client, koin.get<HttpClient>())
            assertSame(koin.get<KakaoBookApi>(), koin.get<KakaoBookApi>())
            val repository = koin.get<BookRepository>()
            assertIs<BookRepositoryImpl>(repository)
            assertSame(repository, koin.get<BookRepository>())
            assertNotSame(koin.get<SearchBooksUseCase>(), koin.get<SearchBooksUseCase>())
        } finally {
            client?.close()
            application.close()
        }
    }

    @Test
    fun initializerRegistersTheSharedModuleInTheGlobalContainer() {
        var client: HttpClient? = null
        try {
            initKoin("test-key")
            val component = object : KoinComponent {}
            val koin = component.getKoin()
            client = koin.get<HttpClient>()
            assertIs<BookRepositoryImpl>(koin.get<BookRepository>())
            assertIs<SearchBooksUseCase>(koin.get<SearchBooksUseCase>())
        } finally {
            client?.close()
            stopKoin()
        }
    }
}
