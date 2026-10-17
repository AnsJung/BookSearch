package com.example.booksearch.presentation.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.usecase.SearchBooksPageUseCase
import kotlin.coroutines.cancellation.CancellationException

/** 카카오 도서 검색의 페이지 번호를 Paging의 키로 사용한다. */
internal class BookPagingSource(
    private val searchBooksPage: SearchBooksPageUseCase,
    private val query: String,
    private val sort: BookSort,
) : PagingSource<Int, Book>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val result = searchBooksPage(query, sort, page, PAGE_SIZE)
            LoadResult.Page(
                data = result.books,
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (result.isEnd || result.books.isEmpty() || page == LAST_PAGE) {
                    null
                } else {
                    page + 1
                },
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val FIRST_PAGE = 1
        private const val LAST_PAGE = 50
    }
}
