package com.example.booksearch

import android.app.Application
import com.example.booksearch.di.initKoin

class BookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(BuildConfig.KAKAO_REST_API_KEY)
    }
}