package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 플랫폼에 맞는 도서 API용 HTTP 클라이언트를 생성한다.
 */
internal expect fun createBookHttpClient(apiKey:String): HttpClient

/**
 * 도서 API용 JSON 응답 변환과 요청 타임아웃을 설정한다.
 */
internal fun HttpClientConfig<*>.configureBookClient(apiKey : String) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15000
    }

    defaultRequest{
        url(KakaoApiConfig.BASE_URL)
        header(HttpHeaders.Authorization, "KakaoAK $apiKey")
    }
}