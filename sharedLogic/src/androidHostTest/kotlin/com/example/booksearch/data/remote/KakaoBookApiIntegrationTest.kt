package com.example.booksearch.data.remote

import com.example.booksearch.di.bookModule
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import io.ktor.client.HttpClient
import java.io.File
import java.util.Properties
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 실제 카카오 서버와 인증·역직렬화·모델 매핑을 확인하는 Android 호스트 테스트.
 * 일반 테스트에서는 제외하며, runKakaoIntegrationTest=true로 실행한다.
 */
class KakaoBookApiIntegrationTest {
    @Test
    fun searchBooksThroughUseCaseReturnsRealBooks() = runBlocking {
        val application = koinApplication { modules(bookModule(readApiKey())) }
        var client: HttpClient? = null
        try {
            client = application.koin.get<HttpClient>()
            val searchBooks = application.koin.get<SearchBooksUseCase>()

            val books = searchBooks(query = " 해리 포터 ", sort = BookSort.ACCURACY)

            // 서버 데이터는 바뀔 수 있으므로 특정 도서나 정확한 건수를 고정하지 않는다.
            assertTrue(books.isNotEmpty(), "잘 알려진 검색어의 실제 검색 결과가 비어 있습니다.")
            assertTrue(books.size <= 10, "기본 페이지 크기인 10건을 초과했습니다.")
            assertTrue(books.all { it.title.isNotBlank() }, "제목이 비어 있는 도서가 있습니다.")

            println("카카오 도서 검색 연동 성공: ${books.size}건")
            books.take(3).forEach { println("- ${it.title}") }
        } finally {
            client?.close()
            application.close()
        }
    }

    private fun readApiKey(): String {
        val projectRoot = checkNotNull(System.getProperty("booksearch.projectRoot")) {
            "Gradle에서 :sharedLogic:testAndroidHostTest -PrunKakaoIntegrationTest=true로 실행하세요."
        }
        val propertiesFile = File(projectRoot, "local.properties")
        check(propertiesFile.isFile) { "프로젝트 루트에 local.properties가 필요합니다." }
        val properties = Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }
        return properties.getProperty("KAKAO_REST_API_KEY")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("local.properties에 KAKAO_REST_API_KEY를 설정하세요.")
    }
}
