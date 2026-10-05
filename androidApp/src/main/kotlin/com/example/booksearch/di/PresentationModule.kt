package com.example.booksearch.di

import com.example.booksearch.presentation.BookSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel<BookSearchViewModel> {
        BookSearchViewModel(get())
    }
}