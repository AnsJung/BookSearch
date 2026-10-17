package com.example.booksearch.presentation

import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchPage
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.domain.usecase.SearchBooksPageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookSearchViewModelSearchTest {
    private val store = ViewModelStore()

    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    @Test
    fun scrollingLoadsTheNextKakaoPage() = runTest {
        val repository = RecordingRepository()
        val viewModel = BookSearchViewModel(SearchBooksPageUseCase(repository))
        store.put("search", viewModel)
        viewModel.onQueryChanged(" 코틀린 ")
        viewModel.search()

        val books = viewModel.books.asSnapshot { scrollTo(index = 19) }

        assertEquals((1..22).map { "책 $it" }, books.map(Book::title))
        assertEquals(listOf(1, 2), repository.requests.map(Request::page))
        assertEquals(listOf(20, 20), repository.requests.map(Request::size))
        assertEquals("코틀린", repository.requests.first().query)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
    }

    @Test
    fun changingSortUsesSubmittedQueryAndPreservesNewInput() = runTest {
        val repository = RecordingRepository()
        val viewModel = BookSearchViewModel(SearchBooksPageUseCase(repository))
        store.put("search", viewModel)
        viewModel.onQueryChanged("코틀린")
        viewModel.search()
        viewModel.books.asSnapshot()
        viewModel.onQueryChanged("입력 중")

        viewModel.onSortChanged(BookSort.LATEST)
        viewModel.books.asSnapshot()

        assertEquals(listOf(BookSort.ACCURACY, BookSort.LATEST), repository.requests.map(Request::sort))
        assertEquals(listOf("코틀린", "코틀린"), repository.requests.map(Request::query))
        assertEquals("입력 중", viewModel.uiState.value.query)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
    }

    @Test
    fun blankQueryKeepsPreviousSearchAndShowsDismissibleDialog() = runTest {
        val repository = RecordingRepository()
        val viewModel = BookSearchViewModel(SearchBooksPageUseCase(repository))
        store.put("search", viewModel)
        viewModel.onQueryChanged("코틀린")
        viewModel.search()
        viewModel.books.asSnapshot()

        viewModel.onQueryChanged("   ")
        viewModel.search()
        assertNotNull(viewModel.uiState.value.queryErrorMessage)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
        assertEquals(1, repository.requests.size)

        viewModel.dismissQueryError()
        assertNull(viewModel.uiState.value.queryErrorMessage)
    }

    private data class Request(val query: String, val sort: BookSort, val page: Int, val size: Int)

    private inner class RecordingRepository : BookRepository {
        val requests = mutableListOf<Request>()

        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> =
            searchByTitlePage(query, sort, 1, 20).books

        override suspend fun searchByTitlePage(
            query: String, sort: BookSort, page: Int, size: Int,
        ): BookSearchPage {
            requests += Request(query, sort, page, size)
            return when (page) {
                1 -> BookSearchPage((1..20).map { book(it) }, isEnd = false)
                2 -> BookSearchPage((21..22).map { book(it) }, isEnd = true)
                else -> error("예상하지 못한 페이지: $page")
            }
        }
    }

    private fun book(number: Int) = Book(
        title = "책 $number", contents = "", url = "", isbn = "", datetime = "",
        authors = emptyList(), publisher = "", translators = emptyList(),
        price = null, salePrice = null, thumbnail = "", status = "",
    )
}
