package com.example.booksearch.di

import org.koin.core.context.startKoin

/**
 * 플랫폼에서 전달받은 Kakao API Key와 로깅 옵션으로 Koin을 시작한다.
 */
fun initKoin(apiKey: String, enableHttpLogging: Boolean = false) {
    startKoin {
        modules(
            bookModule(apiKey, enableHttpLogging),
        )
    }
}
