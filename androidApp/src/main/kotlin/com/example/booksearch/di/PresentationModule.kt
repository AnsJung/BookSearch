package com.example.booksearch.di

import com.example.booksearch.presentation.BookSearchViewModel
import com.example.booksearch.domain.usecase.SearchBooksPageUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel<BookSearchViewModel> {
        BookSearchViewModel(get<SearchBooksPageUseCase>())
    }
}
