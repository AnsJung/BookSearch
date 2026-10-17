package com.example.booksearch.presentation.paging

import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.example.booksearch.data.paging.BookPagingSource
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** SwiftUI에 Paging의 현재 항목과 초기·추가 로딩 상태를 전달한다. */
data class BookPagingState(
    val books: List<Book> = emptyList(),
    val totalCount: Int? = null,
    val isRefreshing: Boolean = true,
    val isAppending: Boolean = false,
    val refreshError: BookPagingError? = null,
    val appendError: BookPagingError? = null,
    val endReached: Boolean = false,
)

data class BookPagingError(
    val failure: BookSearchFailure,
    val canRetry: Boolean,
)

/** PagingDataPresenter를 iOS 화면에서 사용하기 위한 얇은 브리지. */
class BookPagingBridge(private val searchBooksPage: SearchBooksUseCase) {
    private var session: CoroutineScope? = null
    private var presenter: PagingDataPresenter<Book>? = null
    private var requestId = 0L

    fun start(query: String, sort: BookSort, onUpdate: (BookPagingState) -> Unit) {
        close()
        val currentRequest = ++requestId
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        var totalCount: Int? = null
        val currentPresenter = object : PagingDataPresenter<Book>() {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<Book>) = Unit
        }
        session = scope
        presenter = currentPresenter

        fun publish() {
            if (currentRequest != requestId) return
            val states = currentPresenter.loadStateFlow.value
            onUpdate(
                BookPagingState(
                    books = currentPresenter.snapshot().items,
                    totalCount = totalCount,
                    isRefreshing = states == null || states.refresh is LoadState.Loading,
                    isAppending = states?.append is LoadState.Loading,
                    refreshError = (states?.refresh as? LoadState.Error)?.error.toPagingError(),
                    appendError = (states?.append as? LoadState.Error)?.error.toPagingError(),
                    endReached = (states?.append as? LoadState.NotLoading)?.endOfPaginationReached == true,
                ),
            )
        }

        onUpdate(BookPagingState())
        scope.launch { currentPresenter.loadStateFlow.collect { publish() } }
        scope.launch { currentPresenter.onPagesUpdatedFlow.collect { publish() } }
        scope.launch {
            Pager(
                config = PagingConfig(
                    pageSize = BookPagingSource.PAGE_SIZE,
                    initialLoadSize = BookPagingSource.PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    BookPagingSource(searchBooksPage, query, sort) { count ->
                        scope.launch {
                            if (currentRequest == requestId && totalCount != count) {
                                totalCount = count
                                publish()
                            }
                        }
                    }
                },
            ).flow.collectLatest { currentPresenter.collectFrom(it) }
        }
    }

    /** 화면에 나타난 항목을 읽어 Paging에 다음 페이지 선행 로딩 힌트를 보낸다. */
    fun onItemVisible(index: Int) {
        val currentPresenter = presenter ?: return
        if (index in 0 until currentPresenter.snapshot().items.size) currentPresenter[index]
    }

    fun retry() {
        presenter?.retry()
    }

    fun close() {
        requestId++
        session?.cancel()
        session = null
        presenter = null
    }
}

private fun Throwable?.toPagingError(): BookPagingError? {
    this ?: return null
    val failure = (this as? BookSearchException)?.failure ?: BookSearchFailure.UNKNOWN
    return BookPagingError(
        failure = failure,
        canRetry = failure == BookSearchFailure.NETWORK ||
            failure == BookSearchFailure.TIMEOUT || failure == BookSearchFailure.SERVER,
    )
}
