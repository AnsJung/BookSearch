package com.example.booksearch.di

import com.example.booksearch.domain.usecase.SearchBooksUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchBooksUseCaseProvider : KoinComponent {
    val searchBooksUseCase: SearchBooksUseCase by inject()
}
