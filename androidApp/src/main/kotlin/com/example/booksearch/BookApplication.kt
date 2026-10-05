package com.example.booksearch

import android.app.Application
import com.example.booksearch.di.initKoin
import com.example.booksearch.di.presentationModule
import org.koin.core.context.loadKoinModules

class BookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(BuildConfig.KAKAO_REST_API_KEY)
        loadKoinModules(presentationModule)
    }
}