package com.example.booksearch.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.booksearch.R
import com.example.booksearch.domain.model.Book
import com.example.booksearch.presentation.theme.BookSearchTheme

/** 검색 결과 목록의 도서 한 권을 표시한다. */
@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    book: Book,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val authors = book.authors.filter(String::isNotBlank).joinToString(" · ")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(colors.surface)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = book.thumbnail.takeIf(String::isNotBlank),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 68.dp, height = 96.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surfaceVariant),
                placeholder = ColorPainter(colors.surfaceVariant),
                error = ColorPainter(colors.surfaceVariant),
                fallback = ColorPainter(colors.surfaceVariant),
                contentScale = ContentScale.Fit,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                    ),
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (authors.isNotEmpty()) {
                    Text(
                        text = authors,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (book.publisher.isNotBlank()) {
                    Text(
                        text = book.publisher,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_detail),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(end = 40.dp),
            thickness = 1.dp,
            color = colors.outline,
        )
    }
}

@Preview(name = "도서 목록 아이템", showBackground = true, widthDp = 412)
@Composable
private fun SearchResultItemPreview() {
    BookSearchTheme {
        SearchResultItem(
            book = Book(
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
        )
    }
}
