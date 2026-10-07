package com.example.booksearch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksearch.domain.model.BookSort
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
        if (state.isLoading || state.errorMessage == null) return
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
                _uiState.update {
                    it.copy(
                        errorMessage = "잠시 후 다시 시도해 주세요."
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
