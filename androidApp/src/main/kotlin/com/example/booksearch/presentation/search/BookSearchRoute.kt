package com.example.booksearch.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.booksearch.domain.model.Book
import com.example.booksearch.presentation.BookSearchViewModel
import com.example.booksearch.presentation.detail.BookDetailScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookSearchRoute(
    viewModel: BookSearchViewModel = koinViewModel(),
) {
    val searchInputState by viewModel.uiState.collectAsStateWithLifecycle()
    val books = viewModel.books.collectAsLazyPagingItems()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val resultListState = rememberLazyListState()
    val searchBodyState = when {
        searchInputState.submittedQuery == null -> SearchContentState.PreSearch
        books.loadState.refresh is LoadState.Loading -> SearchContentState.Loading
        books.loadState.refresh is LoadState.Error ->
            (books.loadState.refresh as LoadState.Error).error.toSearchError()
        books.itemCount == 0 -> SearchContentState.Empty
        else -> SearchContentState.Results
    }

    LaunchedEffect(searchInputState.requestId) {
        if (searchInputState.requestId > 0) resultListState.scrollToItem(0)
    }

    BackHandler(enabled = selectedBook != null) { selectedBook = null }
    val book = selectedBook
    if (book != null) {
        BookDetailScreen(book = book, onBack = { selectedBook = null })
    } else {
        BookSearchScreen(
            searchInputState = searchInputState,
            books = books,
            searchBodyState = searchBodyState,
            onQueryChanged = viewModel::onQueryChanged,
            onSearch = {
                val sameQuery = searchInputState.query.trim() == searchInputState.submittedQuery
                if (!sameQuery || books.loadState.refresh !is LoadState.Loading) {
                    viewModel.search()
                }
            },
            onRetry = books::retry,
            onSortChanged = viewModel::onSortChanged,
            onQueryErrorDismiss = viewModel::dismissQueryError,
            onBookSelected = { selectedBook = it },
            resultListState = resultListState,
        )
    }
}
