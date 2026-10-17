package com.example.booksearch.presentation

import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchPage
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.domain.usecase.SearchBooksPageUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSearchViewModelTest {
    @Test
    fun typingPreservesRawInputWithoutSubmittingASearch() {
        val repository = RecordingBookRepository()
        val viewModel = BookSearchViewModel(SearchBooksPageUseCase(repository))

        // 조합 중인 글자와 공백도 입력 단계에서는 그대로 유지해야 한다.
        for (input in listOf("ㅎ", "해", " 해리 포터 ", "   ", "")) {
            viewModel.onQueryChanged(input)

            assertEquals(input, viewModel.uiState.value.query)
            assertEquals(null, viewModel.uiState.value.submittedQuery)
            assertTrue("입력 변경만으로 검색 요청이 발생했습니다.", repository.queries.isEmpty())
        }
    }

    private class RecordingBookRepository : BookRepository {
        val queries = mutableListOf<String>()

        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> {
            queries += query
            return emptyList()
        }

        override suspend fun searchByTitlePage(
            query: String, sort: BookSort, page: Int, size: Int,
        ): BookSearchPage {
            queries += query
            return BookSearchPage(emptyList(), isEnd = true)
        }
    }
}
