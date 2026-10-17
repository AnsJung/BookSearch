package com.example.booksearch.di

import androidx.lifecycle.ViewModelStore
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSearchPage
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.presentation.BookSearchViewModel
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class PresentationModuleTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        try {
            store.clear()
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun presentationModuleResolvesSharedUseCaseAndUsesInjectedRepository() = runTest {
        val repository = RecordingRepository()
        initKoin("test-key")
        // 실제 네트워크를 사용하지 않고 공통 모듈의 Repository만 교체한다.
        loadKoinModules(module { single<BookRepository> { repository } })
        loadKoinModules(presentationModule)
        val koin = GlobalContext.get()
        val viewModel = koin.get<BookSearchViewModel>()
        store.put("search", viewModel)

        // Koin 정의는 factory로 동작하며, 화면에서의 보관은 ViewModelStore가 담당한다.
        val anotherViewModel = koin.get<BookSearchViewModel>()
        store.put("anotherSearch", anotherViewModel)
        assertNotSame(viewModel, anotherViewModel)

        viewModel.onQueryChanged(" 코틀린 ")
        viewModel.onSortChanged(BookSort.LATEST)
        viewModel.search()
        viewModel.books.asSnapshot()

        assertEquals(listOf("코틀린" to BookSort.LATEST), repository.requests)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
    }

    private class RecordingRepository : BookRepository {
        val requests = mutableListOf<Pair<String, BookSort>>()

        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> {
            requests += query to sort
            return emptyList()
        }

        override suspend fun searchByTitlePage(
            query: String, sort: BookSort, page: Int, size: Int,
        ): BookSearchPage {
            requests += query to sort
            return BookSearchPage(emptyList(), isEnd = true)
        }
    }
}
