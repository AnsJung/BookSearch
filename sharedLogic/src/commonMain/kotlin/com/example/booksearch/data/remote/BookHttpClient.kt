package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun createBookHttpClient(): HttpClient

internal fun HttpClientConfig<*>.configureBookClient() {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })

    }
}