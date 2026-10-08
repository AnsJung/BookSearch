package com.example.booksearch.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.booksearch.presentation.BookSearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookSearchRoute(
    viewModel: BookSearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BookSearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::search,
        onRetry = viewModel::retry,
        onSortChanged = viewModel::onSortChanged,
        onQueryErrorDismiss = viewModel::dismissQueryError,
    )
}
