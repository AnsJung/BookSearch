package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createBookHttpClient(apiKey: String): HttpClient =
    HttpClient(OkHttp) {
        configureBookClient(apiKey)
    }
