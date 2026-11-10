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

/** A book from the public Kakao catalog that matches a title search. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppFunctionBook(
    /** The book title. */
    val title: String,
    /** The authors listed for the book. */
    val authors: List<String>,
    /** The publisher name. */
    val publisher: String,
    /** The ISBN supplied by Kakao. */
    val isbn: String,
    /** A public page with more information about the book. */
    val url: String,
)

/** The first page of a title search and the total number of matching books. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppFunctionBookSearchResult(
    /** The total number of matches reported by Kakao, when available. */
    val totalCount: Int?,
    /** Up to 20 books from the first page, in accuracy order. */
    val books: List<AppFunctionBook>,
)

/** Exposes the existing book search use case to Android AppFunctions callers. */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "BookSearchAppFunctionService",
    appFunctionXmlFileName = "book_search_app_function_service",
)
abstract class BaseBookSearchAppFunctionService : AppFunctionService(), KoinComponent {
    private val searchBooks: SearchBooksUseCase by inject()

    /**
     * Searches the public Kakao book catalog by book title and returns the first 20 matches.
     * An empty result means no books matched the title.
     *
     * @param title The book title or title phrase to search for.
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
