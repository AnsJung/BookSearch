package com.example.booksearch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun createBookHttpClient(): HttpClient = HttpClient(Darwin) {
    configureBookClient()
}
