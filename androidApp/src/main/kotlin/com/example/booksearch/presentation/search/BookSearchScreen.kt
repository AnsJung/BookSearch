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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booksearch.R
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.presentation.dialog.AppMessageDialog
import com.example.booksearch.presentation.model.BookSearchUiState
import com.example.booksearch.presentation.theme.BookSearchTheme
import com.example.booksearch.presentation.theme.PreSearchTitleStyle

/**
 * 메인 화면 컴포저블
 */
@Composable
fun BookSearchScreen(
    uiState: BookSearchUiState = remember { BookSearchUiState() },
    onQueryChanged: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSortChanged: (BookSort) -> Unit = {},
    onQueryErrorDismiss: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BookSearchBar(
            query = uiState.query,
            onQueryChanged = onQueryChanged,
            onSearch = onSearch,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        when {
            uiState.isLoading -> SearchLoadingSkeleton(
                query = uiState.submittedQuery ?: uiState.query.trim(),
            )
            uiState.errorMessage != null -> SearchError(uiState.errorMessage, onRetry)
            uiState.submittedQuery == null -> PreSearchIntro()
            uiState.books.isEmpty() -> EmptyResult()
            else -> {
                SearchResult(
                    query = uiState.submittedQuery,
                    books = uiState.books,
                    sort = uiState.sort,
                    onSortChanged = onSortChanged,
                )
            }
        }
    }

    uiState.queryErrorMessage?.let { message ->
        AppMessageDialog(
            message = message,
            onDismiss = onQueryErrorDismiss,
        )
    }
}

/**
 * 검색 오류 화면
 */
@Composable
private fun SearchError(message: String, onRetry: () -> Unit) {
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
            text = "도서를 불러오지 못했어요",
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
            Text("다시 시도", style = MaterialTheme.typography.labelLarge)
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
            text = "어떤 책을 찾고 있나요?",
            style = PreSearchTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "책 제목으로 검색해 보세요.",
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
            text = "검색 결과가 없어요",
            style = PreSearchTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "검색어를 바꾸거나 책 제목을 확인해 보세요.",
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
        BookSearchScreen()
    }
}

@Preview(name = "검색 중", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun LoadingBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            uiState = BookSearchUiState(
                query = "바다의 기억",
                submittedQuery = "바다의 기억",
                isLoading = true,
            ),
        )
    }
}

@Preview(name = "검색 오류", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun SearchErrorBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            uiState = BookSearchUiState(
                query = "코틀린",
                submittedQuery = "코틀린",
                errorMessage = "인터넷 연결을 확인한 뒤 다시 시도해 주세요.",
            ),
        )
    }
}

@Preview(name = "검색어 입력 안내", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun QueryErrorBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            uiState = BookSearchUiState(queryErrorMessage = "검색어를 입력해주세요."),
        )
    }
}

@Preview(name = "검색 결과 없음", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun EmptyBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            uiState = BookSearchUiState(
                query = "없는 책",
                submittedQuery = "없는 책",
            ),
        )
    }
}

@Preview(name = "검색 결과", showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun SearchResultBookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen(
            uiState = BookSearchUiState(
                query = "바다의 기억",
                submittedQuery = "바다의 기억",
                books = listOf(
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
        )
    }
}
