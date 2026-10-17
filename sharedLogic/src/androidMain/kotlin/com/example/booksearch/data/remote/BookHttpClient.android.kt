package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.ANDROID

internal actual fun createBookHttpClient(apiKey: String, enableHttpLogging: Boolean): HttpClient =
    HttpClient(OkHttp) {
        configureBookClient(apiKey, enableHttpLogging, Logger.ANDROID)
    }
