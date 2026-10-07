package com.example.booksearch.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.booksearch.R
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.presentation.model.BookSearchUiState
import com.example.booksearch.presentation.theme.BookAccentSubtle
import com.example.booksearch.presentation.theme.BookBackground
import com.example.booksearch.presentation.theme.BookError
import com.example.booksearch.presentation.theme.BookTextPrimary
import com.example.booksearch.presentation.theme.BookTextSecondary
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
    onSortChanged: (BookSort) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookBackground)
            .safeDrawingPadding(),
    ) {
        BookSearchBar(
            query = uiState.query,
            onQueryChanged = onQueryChanged,
            onSearch = onSearch,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        uiState.queryErrorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelLarge,
                color = BookError,
            )
        }

        if (uiState.submittedQuery == null && !uiState.isLoading) {
            PreSearchIntro()
        }
    }
}

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
                .background(BookAccentSubtle, CircleShape),
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
            color = BookTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "책 제목으로 검색해 보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = BookTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
private fun BookSearchScreenPreview() {
    BookSearchTheme {
        BookSearchScreen()
    }
}
