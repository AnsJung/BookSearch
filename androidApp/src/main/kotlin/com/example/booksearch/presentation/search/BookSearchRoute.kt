package com.example.booksearch.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.booksearch.presentation.BookSearchViewModel
import com.example.booksearch.presentation.detail.BookDetailScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookSearchRoute(
    viewModel: BookSearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedBookIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val resultListState = rememberLazyListState()
    val selectedBook = selectedBookIndex?.let(uiState.books::getOrNull)

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) resultListState.scrollToItem(0)
    }

    BackHandler(enabled = selectedBook != null) { selectedBookIndex = null }
    if (selectedBook != null) {
        BookDetailScreen(book = selectedBook, onBack = { selectedBookIndex = null })
    } else {
        BookSearchScreen(
            uiState = uiState,
            onQueryChanged = viewModel::onQueryChanged,
            onSearch = viewModel::search,
            onRetry = viewModel::retry,
            onSortChanged = viewModel::onSortChanged,
            onQueryErrorDismiss = viewModel::dismissQueryError,
            onBookSelected = { selectedBookIndex = it },
            resultListState = resultListState,
        )
    }
}
