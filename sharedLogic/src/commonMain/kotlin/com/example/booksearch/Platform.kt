package com.example.booksearch

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform