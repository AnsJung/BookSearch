package com.example.booksearch.presentation

import androidx.lifecycle.ViewModelStore
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.model.BookSearchException
import com.example.booksearch.domain.model.BookSearchFailure
import com.example.booksearch.domain.repository.BookRepository
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookSearchViewModelSearchTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        // Android의 메인 디스패처를 테스트 스케줄러로 교체한다.
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun successfulSearchUsesSubmittedConditionsAndPreservesEditedInput() = runTest {
        val response = CompletableDeferred<List<Book>>()
        val repository = RecordingRepository { response.await() }
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged(" 코틀린 ")

        viewModel.search()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
        runCurrent()
        assertEquals(listOf("코틀린" to BookSort.ACCURACY), repository.requests)

        viewModel.onQueryChanged("다음 검색어")
        val books = listOf(book("코틀린 학습"))
        response.complete(books)
        advanceUntilIdle()

        assertEquals(books, viewModel.uiState.value.books)
        assertEquals("다음 검색어", viewModel.uiState.value.query)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun emptyResponseIsACompletedSearchWithoutAnError() = runTest {
        val repository = RecordingRepository { emptyList() }
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("결과가 없는 검색어")

        viewModel.search()
        advanceUntilIdle()

        assertEquals(1, repository.requests.size)
        assertTrue(viewModel.uiState.value.books.isEmpty())
        assertEquals("결과가 없는 검색어", viewModel.uiState.value.submittedQuery)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun blankSearchDoesNotReplaceExistingResultsAndTypingClearsInputError() = runTest {
        val books = listOf(book("기존 결과"))
        val repository = RecordingRepository { books }
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("첫 검색")
        viewModel.search()
        advanceUntilIdle()

        viewModel.onQueryChanged("   ")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(1, repository.requests.size)
        assertEquals(books, viewModel.uiState.value.books)
        assertEquals("첫 검색", viewModel.uiState.value.submittedQuery)
        assertTrue(!viewModel.uiState.value.queryErrorMessage.isNullOrBlank())

        viewModel.onQueryChanged(" 새 입력 ")
        assertNull(viewModel.uiState.value.queryErrorMessage)
        assertEquals(" 새 입력 ", viewModel.uiState.value.query)
        assertEquals(books, viewModel.uiState.value.books)
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun blankSearchDoesNotInterruptAnInFlightRequest() = runTest {
        val response = CompletableDeferred<List<Book>>()
        val repository = RecordingRepository { response.await() }
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("진행 중인 검색")
        viewModel.search()
        runCurrent()

        viewModel.onQueryChanged(" ")
        viewModel.search()
        runCurrent()

        assertEquals(1, repository.requests.size)
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("진행 중인 검색", viewModel.uiState.value.submittedQuery)

        val books = listOf(book("늦게 도착한 정상 결과"))
        response.complete(books)
        advanceUntilIdle()

        assertEquals(books, viewModel.uiState.value.books)
        assertEquals(" ", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun duplicateInFlightSearchIsIgnoredButCompletedConditionsCanBeSubmittedAgain() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged(" 코틀린 ")
        viewModel.search()
        viewModel.search()
        runCurrent()

        viewModel.onQueryChanged("코틀린")
        viewModel.search()
        runCurrent()
        assertEquals(listOf("코틀린" to BookSort.ACCURACY), repository.requests)
        assertTrue(viewModel.uiState.value.isLoading)

        repository.respond(0, listOf(book("첫 결과")))
        advanceUntilIdle()
        viewModel.search()
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertTrue(viewModel.uiState.value.books.isEmpty())
        assertTrue(viewModel.uiState.value.isLoading)

        val updatedBooks = listOf(book("다시 검색한 결과"))
        repository.respond(1, updatedBooks)
        advanceUntilIdle()
        assertEquals(updatedBooks, viewModel.uiState.value.books)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun replacingSearchCancelsPreviousRequestWithoutClearingNewLoadingState() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("첫 검색")
        viewModel.search()
        runCurrent()

        viewModel.onQueryChanged("새 검색")
        viewModel.search()
        runCurrent()

        assertEquals(listOf(0), repository.cancelledRequests)
        assertEquals(listOf("첫 검색", "새 검색"), repository.requests.map { it.first })
        assertEquals("새 검색", viewModel.uiState.value.submittedQuery)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        val books = listOf(book("새 결과"))
        repository.respond(1, books)
        advanceUntilIdle()
        assertEquals(books, viewModel.uiState.value.books)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun lateSuccessFromCancelledRequestCannotReplaceLatestResult() = runTest {
        // 일부 하위 구현이 취소를 무시하고 결과를 늦게 반환하는 상황을 재현한다.
        val repository = ControlledRepository(ignoreCancellationFor = setOf(0))
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("첫 검색")
        viewModel.search()
        runCurrent()
        viewModel.onQueryChanged("새 검색")
        viewModel.search()
        runCurrent()

        val latestBooks = listOf(book("최신 결과"))
        repository.respond(1, latestBooks)
        runCurrent()
        repository.respond(0, listOf(book("늦게 도착한 이전 결과")))
        advanceUntilIdle()

        assertEquals(latestBooks, viewModel.uiState.value.books)
        assertEquals("새 검색", viewModel.uiState.value.submittedQuery)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun lateFailureFromCancelledRequestDoesNotChangeLatestLoadingOrError() = runTest {
        val repository = ControlledRepository(ignoreCancellationFor = setOf(0))
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("첫 검색")
        viewModel.search()
        runCurrent()
        viewModel.onQueryChanged("새 검색")
        viewModel.search()
        runCurrent()

        repository.fail(0, IllegalStateException("이전 요청의 내부 오류"))
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("새 검색", viewModel.uiState.value.submittedQuery)

        val latestBooks = listOf(book("정상 결과"))
        repository.respond(1, latestBooks)
        advanceUntilIdle()
        assertEquals(latestBooks, viewModel.uiState.value.books)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun cancelledOldRequestCannotOverwriteNewRequestWithTheSameConditions() = runTest {
        // A → B → A로 검색해도 첫 A의 응답이 마지막 A를 덮어쓰면 안 된다.
        val repository = ControlledRepository(ignoreCancellationFor = setOf(0))
        val viewModel = createViewModel(repository)
        for (query in listOf("A", "B", "A")) {
            viewModel.onQueryChanged(query)
            viewModel.search()
            runCurrent()
        }

        val latestBooks = listOf(book("마지막 A 결과"))
        repository.respond(2, latestBooks)
        runCurrent()
        repository.respond(0, listOf(book("첫 A 결과")))
        advanceUntilIdle()

        assertEquals(listOf(1), repository.cancelledRequests)
        assertEquals(latestBooks, viewModel.uiState.value.books)
        assertEquals("A", viewModel.uiState.value.submittedQuery)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun failedSearchShowsGenericMessageWithoutExposingExceptionDetails() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("실패하는 검색")
        viewModel.search()
        runCurrent()
        repository.fail(0, IllegalStateException("서버 내부 경로와 디버깅 정보"))
        advanceUntilIdle()

        assertEquals("잠시 후 다시 시도해 주세요.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.canRetry)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.books.isEmpty())
        assertEquals("실패하는 검색", viewModel.uiState.value.submittedQuery)

        viewModel.search()
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isLoading)
        repository.respond(1, emptyList())
        advanceUntilIdle()
    }

    @Test
    fun retryUsesFailedSearchConditionsEvenWhenInputHasChanged() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("실패한 검색")
        viewModel.search()
        runCurrent()
        repository.fail(0, BookSearchException(BookSearchFailure.NETWORK))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canRetry)
        assertEquals("인터넷 연결을 확인한 뒤 다시 시도해 주세요.", viewModel.uiState.value.errorMessage)

        viewModel.onQueryChanged("아직 제출하지 않은 입력")
        viewModel.retry()
        runCurrent()

        assertEquals(
            listOf(
                "실패한 검색" to BookSort.ACCURACY,
                "실패한 검색" to BookSort.ACCURACY,
            ),
            repository.requests,
        )
        assertEquals("아직 제출하지 않은 입력", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.isLoading)
        repository.respond(1, emptyList())
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.canRetry)
    }

    @Test
    fun failureTypesControlMessageAndRetryAvailability() = runTest {
        val cases = listOf(
            Triple(BookSearchFailure.TIMEOUT, "응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요.", true),
            Triple(BookSearchFailure.SERVER, "서비스에 일시적인 문제가 있어요. 잠시 후 다시 시도해 주세요.", true),
            Triple(BookSearchFailure.AUTHENTICATION, "현재 도서 검색 서비스를 이용할 수 없어요.", false),
            Triple(BookSearchFailure.PERMISSION_DENIED, "현재 도서 검색 서비스를 이용할 수 없어요.", false),
            Triple(BookSearchFailure.RATE_LIMITED, "검색 이용량이 많아 잠시 후 다시 이용해 주세요.", false),
            Triple(BookSearchFailure.INVALID_REQUEST, "검색 요청을 처리할 수 없어요.", false),
            Triple(BookSearchFailure.RESPONSE_PARSING, "도서 정보를 처리하지 못했어요.", false),
        )
        cases.forEach { (failure, message, canRetry) ->
            val repository = ControlledRepository()
            val viewModel = createViewModel(repository)
            viewModel.onQueryChanged("검색어")
            viewModel.search()
            runCurrent()
            repository.fail(0, BookSearchException(failure))
            advanceUntilIdle()

            assertEquals(failure.name, message, viewModel.uiState.value.errorMessage)
            assertEquals(failure.name, canRetry, viewModel.uiState.value.canRetry)
            viewModel.retry()
            runCurrent()
            assertEquals(failure.name, if (canRetry) 2 else 1, repository.requests.size)
            if (canRetry) {
                repository.respond(1, emptyList())
                advanceUntilIdle()
            }
        }
    }

    @Test
    fun clearingViewModelCancelsPendingSearchWithoutShowingAnError() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("진행 중인 검색")
        viewModel.search()
        runCurrent()

        store.clear()
        runCurrent()

        assertEquals(listOf(0), repository.cancelledRequests)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.books.isEmpty())
    }

    @Test
    fun changingSortBeforeFirstSearchOnlyChangesSelection() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        assertEquals(BookSort.ACCURACY, viewModel.uiState.value.sort)
        viewModel.onQueryChanged(" 아직 제출하지 않은 검색어 ")

        viewModel.onSortChanged(BookSort.LATEST)
        runCurrent()

        assertTrue(repository.requests.isEmpty())
        assertEquals(BookSort.LATEST, viewModel.uiState.value.sort)
        assertEquals(" 아직 제출하지 않은 검색어 ", viewModel.uiState.value.query)
        assertNull(viewModel.uiState.value.submittedQuery)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.search()
        runCurrent()
        assertEquals(listOf("아직 제출하지 않은 검색어" to BookSort.LATEST), repository.requests)
        repository.respond(0, emptyList())
        advanceUntilIdle()
    }

    @Test
    fun changingSortAfterSearchUsesSubmittedQueryAndPreservesUnsubmittedInput() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged(" 코틀린 ")
        viewModel.search()
        runCurrent()
        repository.respond(0, listOf(book("정확도순 결과")))
        advanceUntilIdle()
        viewModel.onQueryChanged(" 스위프트 ")

        viewModel.onSortChanged(BookSort.LATEST)
        assertEquals(" 스위프트 ", viewModel.uiState.value.query)
        assertEquals("코틀린", viewModel.uiState.value.submittedQuery)
        assertEquals(BookSort.LATEST, viewModel.uiState.value.sort)
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.books.isEmpty())
        runCurrent()

        assertEquals(
            listOf("코틀린" to BookSort.ACCURACY, "코틀린" to BookSort.LATEST),
            repository.requests,
        )
        val latestBooks = listOf(book("발간일순 결과"))
        repository.respond(1, latestBooks)
        advanceUntilIdle()
        assertEquals(latestBooks, viewModel.uiState.value.books)
        assertEquals(" 스위프트 ", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun selectingCurrentSortDoesNotRestartPendingOrCompletedSearch() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("코틀린")
        viewModel.search()
        runCurrent()

        viewModel.onSortChanged(BookSort.ACCURACY)
        runCurrent()
        assertEquals(1, repository.requests.size)
        assertTrue(repository.cancelledRequests.isEmpty())
        assertTrue(viewModel.uiState.value.isLoading)

        val books = listOf(book("기존 결과"))
        repository.respond(0, books)
        advanceUntilIdle()
        viewModel.onSortChanged(BookSort.ACCURACY)
        advanceUntilIdle()
        assertEquals(1, repository.requests.size)
        assertEquals(books, viewModel.uiState.value.books)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun changingSortDuringSearchCancelsOldSortAndKeepsNewSearchLoading() = runTest {
        val repository = ControlledRepository()
        val viewModel = createViewModel(repository)
        viewModel.onQueryChanged("코틀린")
        viewModel.search()
        runCurrent()
        viewModel.onQueryChanged("아직 제출하지 않은 다른 검색어")

        viewModel.onSortChanged(BookSort.LATEST)
        runCurrent()

        assertEquals(listOf(0), repository.cancelledRequests)
        assertEquals(
            listOf("코틀린" to BookSort.ACCURACY, "코틀린" to BookSort.LATEST),
            repository.requests,
        )
        assertEquals(BookSort.LATEST, viewModel.uiState.value.sort)
        assertEquals("아직 제출하지 않은 다른 검색어", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        viewModel.onSortChanged(BookSort.LATEST)
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertEquals(listOf(0), repository.cancelledRequests)

        val books = listOf(book("새 정렬 결과"))
        repository.respond(1, books)
        advanceUntilIdle()
        assertEquals(books, viewModel.uiState.value.books)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class ControlledRepository(
        private val ignoreCancellationFor: Set<Int> = emptySet(),
    ) : BookRepository {
        val requests = mutableListOf<Pair<String, BookSort>>()
        val cancelledRequests = mutableListOf<Int>()
        private val responses = mutableListOf<CompletableDeferred<List<Book>>>()

        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> {
            val index = requests.size
            requests += query to sort
            val response = CompletableDeferred<List<Book>>()
            responses += response
            return try {
                if (index in ignoreCancellationFor) {
                    withContext(NonCancellable) { response.await() }
                } else {
                    response.await()
                }
            } catch (e: CancellationException) {
                cancelledRequests += index
                throw e
            }
        }

        fun respond(index: Int, books: List<Book>) {
            responses[index].complete(books)
        }

        fun fail(index: Int, error: Exception) {
            responses[index].completeExceptionally(error)
        }
    }

    private fun createViewModel(repository: BookRepository): BookSearchViewModel =
        BookSearchViewModel(SearchBooksUseCase(repository)).also {
            store.put("search", it)
        }

    private class RecordingRepository(
        private val response: suspend () -> List<Book>,
    ) : BookRepository {
        val requests = mutableListOf<Pair<String, BookSort>>()

        override suspend fun searchByTitle(query: String, sort: BookSort): List<Book> {
            requests += query to sort
            return response()
        }
    }

    private fun book(title: String) = Book(
        title = title,
        contents = "",
        url = "",
        isbn = "",
        datetime = "",
        authors = emptyList(),
        publisher = "",
        translators = emptyList(),
        price = null,
        salePrice = null,
        thumbnail = "",
        status = "",
    )
}
