package com.example.booksearch.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.booksearch.R
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.presentation.dialog.AppMessageDialog
import com.example.booksearch.presentation.model.BookSearchUiState
import com.example.booksearch.presentation.theme.BookSearchTheme
import com.example.booksearch.presentation.theme.preSearchTitle
import kotlinx.coroutines.flow.flowOf

/**
 * 메인 화면 컴포저블
 */
@Composable
internal fun BookSearchScreen(
    searchInputState: BookSearchUiState = remember { BookSearchUiState() },
    books: LazyPagingItems<Book>,
    searchBodyState: SearchContentState,
    onQueryChanged: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSortChanged: (BookSort) -> Unit = {},
    onQueryErrorDismiss: () -> Unit = {},
    onBookSelected: (Book) -> Unit = {},
    resultListState: LazyListState = rememberLazyListState(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BookSearchBar(
            query = searchInputState.query,
            onQueryChanged = onQueryChanged,
            onSearch = onSearch,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        when (val state = searchBodyState) {
            SearchContentState.Loading -> SearchLoadingSkeleton(
                query = searchInputState.submittedQuery ?: searchInputState.query.trim(),
            )
            is SearchContentState.Error -> SearchError(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = onRetry,
            )
            SearchContentState.PreSearch -> PreSearchIntro()
            SearchContentState.Empty -> EmptyResult()
            SearchContentState.Results -> {
                SearchResult(
                    query = requireNotNull(searchInputState.submittedQuery),
                    books = books,
                    totalCount = searchInputState.totalCount,
                    sort = searchInputState.sort,
                    onSortChanged = onSortChanged,
                    onBookSelected = onBookSelected,
                    onRetry = onRetry,
                    listState = resultListState,
                )
            }
        }
    }

    searchInputState.queryErrorMessage?.let { message ->
        AppMessageDialog(
            message = message,
            onDismiss = onQueryErrorDismiss,
        )
    }
}

@Composable
private fun previewBooks(books: List<Book> = emptyList()): LazyPagingItems<Book> =
    remember(books) { flowOf(PagingData.from(books)) }.collectAsLazyPagingItems()

/**
 * 검색 오류 화면
 */
@Composable
private fun SearchError(message: String, canRetry: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 126.dp, start = 36.dp, end = 36.dp)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_search_intro),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.search_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (canRetry) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.width(160.dp).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/**
 * 검색 전 화면
 */
@Composable
private fun PreSearchIntro() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 162.dp, start = 28.dp, end = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_search_intro),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.search_intro_title),
            style = MaterialTheme.typography.preSearchTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.search_intro_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 검색 결과 없음 화면
 */
@Composable
private fun EmptyResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 118.dp, start = 36.dp, end = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_search_intro),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = stringResource(R.string.search_empty_title),
            style = MaterialTheme.typography.preSearchTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.search_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "검색 전", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun PreSearchBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(books = previewBooks(), searchBodyState = SearchContentState.PreSearch)
    }
}

@Preview(name = "검색 중", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun LoadingBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            searchInputState = BookSearchUiState(
                query = "바다의 기억",
                submittedQuery = "바다의 기억",
            ),
            books = previewBooks(),
            searchBodyState = SearchContentState.Loading,
        )
    }
}

@Preview(name = "검색 오류", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun SearchErrorBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            searchInputState = BookSearchUiState(
                query = "코틀린",
                submittedQuery = "코틀린",
            ),
            books = previewBooks(),
            searchBodyState = SearchContentState.Error("인터넷 연결을 확인한 뒤 다시 시도해 주세요.", true),
        )
    }
}

@Preview(name = "검색어 입력 안내", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun QueryErrorBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            searchInputState = BookSearchUiState(queryErrorMessage = "검색어를 입력해주세요."),
            books = previewBooks(),
            searchBodyState = SearchContentState.PreSearch,
        )
    }
}

@Preview(name = "검색 결과 없음", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun EmptyBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            searchInputState = BookSearchUiState(
                query = "없는 책",
                submittedQuery = "없는 책",
            ),
            books = previewBooks(),
            searchBodyState = SearchContentState.Empty,
        )
    }
}

@Preview(name = "검색 결과", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun SearchResultBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            searchInputState = BookSearchUiState(
                query = "바다의 기억",
                submittedQuery = "바다의 기억",
                totalCount = 1,
            ),
            books = previewBooks(
                listOf(
                    Book(
                        title = "바다의 기억을 엮는 사람들",
                        contents = "",
                        url = "",
                        isbn = "",
                        datetime = "",
                        authors = listOf("서은채", "윤해솔", "문지안"),
                        publisher = "여름의서가",
                        translators = emptyList(),
                        price = null,
                        salePrice = null,
                        thumbnail = "",
                        status = "",
                    ),
                ),
            ),
            searchBodyState = SearchContentState.Results,
        )
    }
}
