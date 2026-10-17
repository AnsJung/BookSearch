package com.example.booksearch.presentation

import androidx.paging.PagingSource
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.model.BookSearchPage
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.domain.usecase.SearchBooksPageUseCase
import com.example.booksearch.presentation.search.BookPagingSource
import com.example.booksearch.presentation.search.toSearchError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BookPagingSourceTest {
    @Test
    fun isEndAndKakaoPageLimitStopFurtherRequests() = runTest {
        val repository = FakeRepository { page ->
            BookSearchPage(listOf(book()), isEnd = page == 2)
        }
        val source = BookPagingSource(SearchBooksPageUseCase(repository), "책", BookSort.ACCURACY)

        val first = source.load(refresh(null)) as PagingSource.LoadResult.Page
        val second = source.load(append(2)) as PagingSource.LoadResult.Page
        val lastAllowed = source.load(append(50)) as PagingSource.LoadResult.Page

        assertEquals(2, first.nextKey)
        assertEquals(1, second.prevKey)
        assertNull(second.nextKey)
        assertNull(lastAllowed.nextKey)
        assertEquals(listOf(1, 2, 50), repository.pages)
    }

    @Test
    fun failureBecomesLoadResultErrorForComposeLoadState() = runTest {
        val failure = BookSearchException(BookSearchFailure.NETWORK)
        val repository = FakeRepository { throw failure }
        val source = BookPagingSource(SearchBooksPageUseCase(repository), "책", BookSort.ACCURACY)

        val result = source.load(refresh(null))

        assertTrue(result is PagingSource.LoadResult.Error)
        assertSame(failure, (result as PagingSource.LoadResult.Error).throwable)
    }

    @Test
    fun onlyRecoverableFailuresShowRetryAction() {
        assertTrue(BookSearchException(BookSearchFailure.NETWORK).toSearchError().canRetry)
        assertTrue(BookSearchException(BookSearchFailure.TIMEOUT).toSearchError().canRetry)
        assertTrue(BookSearchException(BookSearchFailure.SERVER).toSearchError().canRetry)
        assertEquals(false, BookSearchException(BookSearchFailure.AUTHENTICATION).toSearchError().canRetry)
    }

    private class FakeRepository(
        private val response: (Int) -> BookSearchPage,
    ) : BookRepository {
        val pages = mutableListOf<Int>()
        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> =
            searchByTitlePage(query, sort, 1, 20).books

        override suspend fun searchByTitlePage(
            query: String, sort: BookSort, page: Int, size: Int,
        ): BookSearchPage {
            pages += page
            return response(page)
        }
    }

    private fun refresh(page: Int?) = PagingSource.LoadParams.Refresh(page, 20, false)
    private fun append(page: Int) = PagingSource.LoadParams.Append(page, 20, false)

    private fun book() = Book(
        title = "책", contents = "", url = "", isbn = "", datetime = "",
        authors = emptyList(), publisher = "", translators = emptyList(),
        price = null, salePrice = null, thumbnail = "", status = "",
    )
}
