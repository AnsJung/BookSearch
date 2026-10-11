package com.example.booksearch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import com.example.booksearch.presentation.model.BookSearchUiState
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BookSearchViewModel(
    private val searchBooksUseCase: SearchBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookSearchUiState>(BookSearchUiState())
    val uiState: StateFlow<BookSearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var activeSearchCondition: Pair<String, BookSort>? = null

    /**
     * 검색어 변경 시 호출되는 함수
     */
    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                queryErrorMessage = null
            )
        }
    }

    /** 검색어 입력 안내 팝업을 닫는다. */
    fun dismissQueryError() {
        _uiState.update { it.copy(queryErrorMessage = null) }
    }

    /**
     * 정렬 방식 변경 시 호출되는 함수
     */
    fun onSortChanged(sort: BookSort) {
        if (sort == _uiState.value.sort) return
        _uiState.update {
            it.copy(
                sort = sort
            )
        }
        val submittedQuery = _uiState.value.submittedQuery ?: return
        executeSearch(query = submittedQuery, sort = sort)
    }

    /**
     * 검색 버튼 클릭 시 호출되는 함수
     */
    fun search() {
        val state = _uiState.value
        executeSearch(
            query = state.query.trim(), sort = state.sort
        )
    }

    /** 실패한 검색 조건으로 다시 요청한다. */
    fun retry() {
        val state = _uiState.value
        if (state.isLoading || !state.canRetry) return
        val submittedQuery = state.submittedQuery ?: return
        executeSearch(query = submittedQuery, sort = state.sort)
    }

    /**
     * 도서 검색을 수행하는 함수
     */
    private fun executeSearch(query: String, sort: BookSort) {
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    queryErrorMessage = "검색어를 입력해주세요."
                )
            }
            return
        }

        val condition = query to sort
        if (searchJob?.isActive == true && activeSearchCondition == condition) { // 동일 단어 검색 방지
            return
        }

        searchJob?.cancel() // 이전 검색 취소
        activeSearchCondition = condition
        _uiState.update {
            it.copy(
                submittedQuery = query,
                books = emptyList(),
                isLoading = true,
                errorMessage = null,
                canRetry = false,
                queryErrorMessage = null
            )
        }
        searchJob = viewModelScope.launch {
            try {
                val books = searchBooksUseCase(query, sort)
                ensureActive()
                _uiState.update {
                    it.copy(
                        books = books
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                val failure = (e as? BookSearchException)?.failure ?: BookSearchFailure.UNKNOWN
                _uiState.update {
                    it.copy(
                        errorMessage = failure.userMessage(),
                        canRetry = failure.canRetry(),
                    )
                }
            } finally {
                if (isActive) {
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}

private fun BookSearchFailure.userMessage(): String = when (this) {
    BookSearchFailure.NETWORK -> "인터넷 연결을 확인한 뒤 다시 시도해 주세요."
    BookSearchFailure.TIMEOUT -> "응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요."
    BookSearchFailure.SERVER -> "서비스에 일시적인 문제가 있어요. 잠시 후 다시 시도해 주세요."
    BookSearchFailure.AUTHENTICATION, BookSearchFailure.PERMISSION_DENIED -> "현재 도서 검색 서비스를 이용할 수 없어요."
    BookSearchFailure.RATE_LIMITED -> "검색 이용량이 많아 잠시 후 다시 이용해 주세요."
    BookSearchFailure.INVALID_REQUEST -> "검색 요청을 처리할 수 없어요."
    BookSearchFailure.RESPONSE_PARSING -> "도서 정보를 처리하지 못했어요."
    BookSearchFailure.UNKNOWN -> "잠시 후 다시 시도해 주세요."
}

private fun BookSearchFailure.canRetry(): Boolean = when (this) {
    BookSearchFailure.NETWORK, BookSearchFailure.TIMEOUT, BookSearchFailure.SERVER -> true
    else -> false
}
