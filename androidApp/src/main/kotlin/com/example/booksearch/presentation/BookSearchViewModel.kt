package com.example.booksearch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.booksearch.data.paging.BookPagingSource
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import com.example.booksearch.presentation.model.BookSearchUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BookSearchViewModel(
    private val searchBooksPage: SearchBooksUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookSearchUiState())
    val uiState: StateFlow<BookSearchUiState> = _uiState.asStateFlow()

    private val searchRequests = MutableStateFlow<SearchRequest?>(null)
    private var nextRequestId = 0L

    val books: Flow<PagingData<Book>> = searchRequests.flatMapLatest { request ->
        if (request == null) {
            flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = BookPagingSource.PAGE_SIZE,
                    initialLoadSize = BookPagingSource.PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    BookPagingSource(searchBooksPage, request.query, request.sort) { count ->
                        viewModelScope.launch {
                            _uiState.update { state ->
                                if (state.requestId == request.id) state.copy(totalCount = count) else state
                            }
                        }
                    }
                },
            ).flow
        }
    }.cachedIn(viewModelScope)

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query, queryErrorMessage = null) }
    }

    fun dismissQueryError() {
        _uiState.update { it.copy(queryErrorMessage = null) }
    }

    fun onSortChanged(sort: BookSort) {
        if (sort == _uiState.value.sort) return
        _uiState.update { it.copy(sort = sort) }
        val submittedQuery = _uiState.value.submittedQuery ?: return
        submit(submittedQuery, sort)
    }

    fun search() {
        val state = _uiState.value
        submit(state.query.trim(), state.sort)
    }

    private fun submit(query: String, sort: BookSort) {
        if (query.isBlank()) {
            _uiState.update { it.copy(queryErrorMessage = "검색어를 입력해주세요.") }
            return
        }
        val requestId = ++nextRequestId
        _uiState.update {
            it.copy(submittedQuery = query, requestId = requestId, totalCount = null, queryErrorMessage = null)
        }
        searchRequests.value = SearchRequest(query, sort, requestId)
    }

    private data class SearchRequest(val query: String, val sort: BookSort, val id: Long)
}
