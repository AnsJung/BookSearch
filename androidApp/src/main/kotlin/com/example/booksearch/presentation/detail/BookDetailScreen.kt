package com.example.booksearch.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.booksearch.R
import com.example.booksearch.domain.model.Book
import com.example.booksearch.presentation.theme.BookSearchTheme
import java.util.Locale

/** 선택한 도서의 정보를 표시한다. 목록에서 받은 [Book]을 사용하며 추가 요청은 하지 않는다. */
@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val infoRows = buildBookInfoRows(book)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.surface)
                .padding(start = 4.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.book_detail_back),
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.book_detail_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.outline)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BookHero(book)

            if (infoRows.isNotEmpty()) {
                HorizontalDivider(thickness = 1.dp, color = colors.outline)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    infoRows.forEach { (labelRes, value) -> BookInfoRow(labelRes, value) }
                }
            }

            if (book.contents.isNotBlank()) {
                HorizontalDivider(thickness = 1.dp, color = colors.outline)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.book_detail_contents_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onBackground,
                    )
                    Text(
                        text = book.contents,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookHero(book: Book) {
    val colors = MaterialTheme.colorScheme
    val authors = book.authors.filter(String::isNotBlank).joinToString(" · ")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BookCover(book.thumbnail)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
            )
            if (authors.isNotEmpty()) {
                Text(
                    text = authors,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (book.publisher.isNotBlank()) {
                Text(
                    text = book.publisher,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BookCover(thumbnail: String) {
    val colors = MaterialTheme.colorScheme
    var imageFailed by remember(thumbnail) { mutableStateOf(false) }
    val modifier = Modifier
        .size(width = 144.dp, height = 204.dp)
        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
        .background(colors.surfaceVariant)

    if (thumbnail.isBlank() || imageFailed) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_book_placeholder),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.book_detail_cover_missing),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    } else {
        AsyncImage(
            model = thumbnail,
            contentDescription = stringResource(R.string.book_detail_cover_description),
            modifier = modifier,
            contentScale = ContentScale.Fit,
            placeholder = ColorPainter(colors.surfaceVariant),
            onError = { imageFailed = true },
        )
    }
}

@Composable
private fun BookInfoRow(@StringRes labelRes: Int, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

internal fun buildBookInfoRows(book: Book): List<Pair<Int, String>> = buildList {
    formatBookDate(book.datetime)?.let { add(R.string.book_detail_published_date to it) }
    val isbnTokens = book.isbn.trim().split(Regex("\\s+")).filter(String::isNotBlank)
    isbnTokens.firstOrNull { it.replace("-", "").length == 10 }?.let { add(R.string.book_detail_isbn10 to it) }
    isbnTokens.firstOrNull { it.replace("-", "").length == 13 }?.let { add(R.string.book_detail_isbn13 to it) }
    book.price?.let { add(R.string.book_detail_regular_price to formatBookPrice(it)) }
    book.salePrice?.let { add(R.string.book_detail_sale_price to formatBookPrice(it)) }
}

internal fun formatBookDate(datetime: String): String? {
    val match = Regex("^(\\d{4})-(\\d{2})-(\\d{2})").find(datetime) ?: return null
    return "${match.groupValues[1]}. ${match.groupValues[2]}. ${match.groupValues[3]}"
}

internal fun formatBookPrice(price: Int): String = String.format(Locale.KOREA, "%,d원", price)

@Preview(name = "도서 상세", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookDetailPreview() {
    BookSearchTheme {
        BookDetailScreen(
            book = previewBook,
            onBack = {},
        )
    }
}

@Preview(name = "표지·부가 정보 없음", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookDetailMissingInfoPreview() {
    BookSearchTheme {
        BookDetailScreen(
            book = previewBook.copy(
                thumbnail = "",
                isbn = "8960777331",
                datetime = "",
                price = null,
                salePrice = 0,
                contents = "",
            ),
            onBack = {},
        )
    }
}

private val previewBook = Book(
    title = "바다의 기억을 엮는 사람들",
    contents = "오래된 항구 도시에서 만난 사람들의 이야기를 담았습니다. 잊혀 가는 기억을 따라가며 서로의 삶을 다시 바라봅니다.",
    url = "",
    isbn = "8960777331 9788960777330",
    datetime = "2026-08-21T00:00:00.000+09:00",
    authors = listOf("서은채", "윤해솔", "문지안"),
    publisher = "여름의서가",
    translators = emptyList(),
    price = 18000,
    salePrice = 16200,
    thumbnail = "",
    status = "정상판매",
)
