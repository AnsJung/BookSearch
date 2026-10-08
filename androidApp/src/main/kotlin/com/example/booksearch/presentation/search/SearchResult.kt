package com.example.booksearch.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.booksearch.R
import com.example.booksearch.domain.model.Book
import com.example.booksearch.domain.model.BookSort
import com.example.booksearch.presentation.theme.BookSearchTheme

/**
 * 검색 결과 화면 컴포저블
 */
@Composable
fun SearchResult(
    query: String,
    books: List<Book>,
    sort: BookSort = BookSort.ACCURACY,
    onSortChanged: (BookSort) -> Unit = {},
) {
    var isSortMenuOpen by remember { mutableStateOf(false) }

    SearchResultHeader(
        query = query,
        resultCount = books.size,
        sort = sort,
        isMenuOpen = isSortMenuOpen,
        onMenuOpenChange = { isSortMenuOpen = it },
        onSortChanged = onSortChanged,
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        items(books.size) { index ->
            val book = books[index]
            SearchResultItem(book = book)
        }
    }
}

private fun BookSort.displayLabel(): String = when (this) {
    BookSort.ACCURACY -> "정확도순"
    BookSort.LATEST -> "발간일순"
}

/**
 * 검색 결과 헤더 컴포저블
 */
@Composable
fun SearchResultHeader(
    query: String,
    resultCount: Int,
    sort: BookSort,
    isMenuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onSortChanged: (BookSort) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    HorizontalDivider(thickness = 1.dp, color = colors.outline)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(colors.surface)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "‘$query’",
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "표시된 도서 ${resultCount}권",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }

        SortSelector(
            sort = sort,
            isMenuOpen = isMenuOpen,
            onMenuOpenChange = onMenuOpenChange,
            onSortChanged = onSortChanged,
        )
    }
    HorizontalDivider(thickness = 1.dp, color = colors.outline)
}

/**
 * 정렬 옵션 선택 컴포저블
 */
@Composable
private fun SortSelector(
    sort: BookSort,
    isMenuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onSortChanged: (BookSort) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box {
        Row(
            modifier = Modifier
                .size(width = 96.dp, height = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isMenuOpen) colors.primaryContainer else colors.surface)
                .border(
                    width = 1.dp,
                    color = if (isMenuOpen) colors.primary else colors.outline,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable(role = Role.Button) { onMenuOpenChange(!isMenuOpen) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = sort.displayLabel(),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = if (isMenuOpen) colors.primary else colors.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.width(5.dp))
            Image(
                painter = painterResource(if (isMenuOpen) R.drawable.ic_sort_up else R.drawable.ic_sort_down),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        }

        if (isMenuOpen) {
            SortOptionsPopup(
                sort = sort,
                onDismiss = { onMenuOpenChange(false) },
                onSortChanged = { selectedSort ->
                    onMenuOpenChange(false)
                    if (selectedSort != sort) onSortChanged(selectedSort)
                },
            )
        }
    }
}

/**
 * 정렬 옵션 팝업 컴포저블
 */
@Composable
private fun SortOptionsPopup(
    sort: BookSort,
    onDismiss: () -> Unit,
    onSortChanged: (BookSort) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val menuOffset = with(LocalDensity.current) { (48.dp + 20.dp).roundToPx() }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, menuOffset),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = Modifier.width(132.dp),
            shape = RoundedCornerShape(8.dp),
            color = colors.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, colors.outline),
        ) {
            Column(Modifier.padding(4.dp).selectableGroup()) {
                BookSort.entries.forEach { option ->
                    val selected = option == sort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) colors.primaryContainer else colors.surface)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                onSortChanged(option)
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = option.displayLabel(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color = if (selected) colors.primary else colors.onSurfaceVariant,
                        )
                        if (selected) {
                            Image(
                                painter = painterResource(R.drawable.ic_sort_selected),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "검색 결과 목록", showBackground = true, widthDp = 412)
@Composable
private fun SearchResultPreview() {
    BookSearchTheme {
        var selectedSort by remember { mutableStateOf(BookSort.ACCURACY) }
        SearchResult(
            query = "바다의 기억",
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
            sort = selectedSort,
            onSortChanged = { selectedSort = it },
        )
    }
}

@Preview(name = "정렬 메뉴 펼침", showBackground = true, widthDp = 412, heightDp = 220)
@Composable
private fun SearchResultSortOpenPreview() {
    BookSearchTheme {
        var selectedSort by remember { mutableStateOf(BookSort.ACCURACY) }
        var isMenuOpen by remember { mutableStateOf(true) }
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchResultHeader(
                query = "바다의 기억",
                resultCount = 5,
                sort = selectedSort,
                isMenuOpen = isMenuOpen,
                onMenuOpenChange = { isMenuOpen = it },
                onSortChanged = { selectedSort = it },
            )
        }
    }
}
