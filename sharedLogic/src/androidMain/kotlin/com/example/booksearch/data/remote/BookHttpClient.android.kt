package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createBookHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        configureBookClient()
    }
