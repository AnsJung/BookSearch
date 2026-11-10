package com.example.booksearch.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

/** 카카오 공개 도서 검색 결과에서 외부 호출자에게 전달할 책 정보다. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppFunctionBook(
    /** 도서 제목이다. */
    val title: String,
    /** 저자 목록이다. */
    val authors: List<String>,
    /** 출판사 이름이다. */
    val publisher: String,
    /** 카카오가 제공한 ISBN이다. */
    val isbn: String,
    /** 책의 추가 정보를 확인할 수 있는 공개 페이지 주소다. */
    val url: String,
)

/** 제목 검색의 첫 페이지 목록과 전체 검색 결과 수를 함께 전달한다. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppFunctionBookSearchResult(
    /** 카카오가 제공한 전체 검색 결과 수이며, 제공되지 않을 수 있다. */
    val totalCount: Int?,
    /** 정확도순 첫 페이지에서 가져온 최대 20권이다. */
    val books: List<AppFunctionBook>,
)

/** 기존 도서 검색 유스케이스를 Android AppFunctions 호출에 연결한다. */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "BookSearchAppFunctionService",
    appFunctionXmlFileName = "book_search_app_function_service",
)
abstract class BaseBookSearchAppFunctionService : AppFunctionService(), KoinComponent {
    private val searchBooks: SearchBooksUseCase by inject()

    /**
     * 카카오 공개 도서 목록을 제목으로 검색하고 첫 페이지의 책을 최대 20권 반환한다.
     * 일치하는 책이 없으면 빈 목록을 반환한다.
     *
     * @param title 검색할 도서 제목 또는 제목의 일부
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun searchBooksByTitle(title: String): AppFunctionBookSearchResult =
        withContext(Dispatchers.IO) {
            val query = title.trim()
            if (query.isEmpty()) {
                throw AppFunctionInvalidArgumentException("A book title is required.")
            }

            try {
                val page = searchBooks(query, BookSort.ACCURACY, page = 1, size = 20)
                AppFunctionBookSearchResult(
                    totalCount = page.totalCount,
                    books = page.books.map(Book::toAppFunctionBook),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                throw AppFunctionAppUnknownException("Book search is temporarily unavailable.")
            }
        }
}

private fun Book.toAppFunctionBook(): AppFunctionBook = AppFunctionBook(
    title = title,
    authors = authors,
    publisher = publisher,
    isbn = isbn,
    url = url,
)
