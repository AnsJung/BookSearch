package com.example.booksearch.presentation.detail

import com.example.booksearch.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Test

class BookDetailFormatterTest {
    @Test
    fun `날짜는 시간대 변환 없이 응답의 날짜 부분을 표시한다`() {
        assertEquals("2026. 08. 21", formatBookDate("2026-08-21T23:30:00.000-05:00"))
    }

    @Test
    fun `ISBN 종류를 구분하고 누락된 정보는 생략하며 0원은 표시한다`() {
        val rows = buildBookInfoRows(
            Book(
                title = "테스트 도서",
                contents = "",
                url = "",
                isbn = "8960777331 9788960777330",
                datetime = "",
                authors = emptyList(),
                publisher = "",
                translators = emptyList(),
                price = null,
                salePrice = 0,
                thumbnail = "",
                status = "",
            ),
        )

        assertEquals(
            listOf(
                "ISBN10" to "8960777331",
                "ISBN13" to "9788960777330",
                "판매가" to "0원",
            ),
            rows,
        )
        assertEquals("18,000원", formatBookPrice(18000))
    }
}
