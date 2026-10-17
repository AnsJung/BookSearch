package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE

internal actual fun createBookHttpClient(apiKey: String, enableHttpLogging: Boolean): HttpClient = HttpClient(Darwin) {
    configureBookClient(apiKey, enableHttpLogging, Logger.SIMPLE)
}
