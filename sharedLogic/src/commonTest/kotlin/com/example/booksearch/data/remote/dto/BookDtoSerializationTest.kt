package com.example.booksearch.data.remote.dto

import com.example.booksearch.data.mapper.toBook
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BookDtoSerializationTest {
    // API 명세서의 JSON 정책으로 DTO 계약을 검증한다. 실제 HTTP 클라이언트 설정은 별도 검증한다.
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun longContentsArePreservedThroughDtoAndDomainMapping() {
        val contents = "도서 소개 문장. ".repeat(40)
        val book = decode(
            responseWith("""{"title":"책","contents":"$contents"}""")
        ).documents.single().toBook()

        assertEquals(contents, book.contents)
    }

    @Test
    fun decodesKakaoFieldNamesAndDocumentValues() {
        val response = decode("""
            {
              "meta":{"total_count":12,"pageable_count":10,"is_end":false},
              "documents":[{
                "title":"코틀린","contents":"소개","url":"https://example.com/book",
                "isbn":"8996991341 9788996991342","datetime":"2026-09-01T00:00:00.000+09:00",
                "authors":["저자"],"publisher":"출판사","translators":["번역자"],
                "price":20000,"sale_price":18000,"thumbnail":"https://example.com/cover.jpg",
                "status":"정상판매"
              }]
            }
        """.trimIndent())
        assertEquals(12, response.meta.totalCount)
        assertEquals(10, response.meta.pageableCount)
        assertEquals(false, response.meta.isEnd)
        val book = response.documents.single()
        assertEquals("코틀린", book.title)
        assertEquals("소개", book.contents)
        assertEquals("https://example.com/book", book.url)
        assertEquals("8996991341 9788996991342", book.isbn)
        assertEquals("2026-09-01T00:00:00.000+09:00", book.datetime)
        assertEquals(listOf("저자"), book.authors)
        assertEquals("출판사", book.publisher)
        assertEquals(listOf("번역자"), book.translators)
        assertEquals(20000, book.price)
        assertEquals(18000, book.salePrice)
        assertEquals("https://example.com/cover.jpg", book.thumbnail)
        assertEquals("정상판매", book.status)
    }

    @Test
    fun missingOptionalFieldsUseNullDefaults() {
        val book = decode(responseWith("""{"title":"책"}""")).documents.single()
        assertNull(book.contents)
        assertNull(book.url)
        assertNull(book.isbn)
        assertNull(book.datetime)
        assertNull(book.authors)
        assertNull(book.publisher)
        assertNull(book.translators)
        assertNull(book.price)
        assertNull(book.salePrice)
        assertNull(book.thumbnail)
        assertNull(book.status)
    }

    @Test
    fun optionalFieldsAcceptExplicitNull() {
        val explicitNull = decode(responseWith("""{
            "title":"책","contents":null,"url":null,"isbn":null,"datetime":null,
            "authors":null,"publisher":null,"translators":null,"price":null,
            "sale_price":null,"thumbnail":null,"status":null
        }""")).documents.single()
        val omitted = decode(responseWith("""{"title":"책"}""")).documents.single()
        assertEquals(omitted, explicitNull)
    }

    @Test
    fun emptyDocumentsIsAValidResponse() {
        assertEquals(emptyList(), decode(EMPTY_RESPONSE).documents)
    }

    @Test
    fun unknownJsonFieldsCanBeIgnored() {
        val body = """{
            "meta":{"total_count":1,"pageable_count":1,"is_end":true,"future_meta":42},
            "documents":[{"title":"책","future_book":{}}],"future_root":[]
        }"""
        assertEquals("책", decode(body).documents.single().title)
    }

    @Test
    fun missingRequiredFieldsAreRejected() {
        val invalid = listOf(
            "{}",
            """{"meta":{"total_count":0,"pageable_count":0,"is_end":true}}""",
            """{"documents":[]}""",
            EMPTY_RESPONSE.replace("\"total_count\":0,", ""),
            EMPTY_RESPONSE.replace("\"pageable_count\":0,", ""),
            EMPTY_RESPONSE.replace(",\"is_end\":true", ""),
            responseWith("{}"),
        )
        invalid.forEach { body ->
            assertFailsWith<SerializationException>(body) { decode(body) }
        }
    }

    @Test
    fun requiredFieldsRejectExplicitNull() {
        val invalid = listOf(
            """{"meta":null,"documents":[]}""",
            EMPTY_RESPONSE.replace("\"documents\":[]", "\"documents\":null"),
            EMPTY_RESPONSE.replace("\"total_count\":0", "\"total_count\":null"),
            EMPTY_RESPONSE.replace("\"pageable_count\":0", "\"pageable_count\":null"),
            EMPTY_RESPONSE.replace("\"is_end\":true", "\"is_end\":null"),
            responseWith("""{"title":null}"""),
        )
        invalid.forEach { body ->
            assertFailsWith<SerializationException>(body) { decode(body) }
        }
    }

    @Test
    fun incompatibleJsonTypesAreRejected() {
        val invalid = listOf(
            responseWith("""{"title":42}"""),
            responseWith("""{"title":"책","contents":42}"""),
            responseWith("""{"title":"책","authors":[42]}"""),
            responseWith("""{"title":"책","translators":{}}"""),
            responseWith("""{"title":"책","price":true}"""),
            responseWith("""{"title":"책","sale_price":1.5}"""),
            EMPTY_RESPONSE.replace("\"is_end\":true", "\"is_end\":42"),
        )
        invalid.forEach { body ->
            assertFailsWith<SerializationException>(body) { decode(body) }
        }
    }

    // 완성 후 개선: 기본 역직렬화가 허용하는 문자열 숫자·Boolean까지 엄격하게 거부한다.
    @Ignore
    @Test
    fun quotedNumbersAndBooleansAreRejectedAfterStrictValidationIsAdded() {
        val invalid = listOf(
            responseWith("""{"title":"책","price":"1000"}"""),
            responseWith("""{"title":"책","sale_price":"1000"}"""),
            EMPTY_RESPONSE.replace("\"total_count\":0", "\"total_count\":\"0\""),
            EMPTY_RESPONSE.replace("\"pageable_count\":0", "\"pageable_count\":\"0\""),
            EMPTY_RESPONSE.replace("\"is_end\":true", "\"is_end\":\"true\""),
        )
        invalid.forEach { body ->
            assertFailsWith<SerializationException>(body) { decode(body) }
        }
    }

    @Test
    fun dtoPreservesValuesForTheMapperToNormalize() {
        val book = decode(responseWith("""{
            "title":"  책  ","contents":"  ","authors":[" 저자 "," "],
            "translators":[],"price":-1,"sale_price":0,"datetime":"invalid-date"
        }""")).documents.single()
        assertEquals("  책  ", book.title)
        assertEquals("  ", book.contents)
        assertEquals(listOf(" 저자 ", " "), book.authors)
        assertEquals(emptyList(), book.translators)
        assertEquals(-1, book.price)
        assertEquals(0, book.salePrice)
        assertEquals("invalid-date", book.datetime)
    }

    private fun decode(body: String): BookSearchResponseDTO = json.decodeFromString(body)

    private fun responseWith(document: String): String =
        """{"meta":{"total_count":1,"pageable_count":1,"is_end":true},"documents":[$document]}"""

    companion object {
        private const val EMPTY_RESPONSE =
            """{"meta":{"total_count":0,"pageable_count":0,"is_end":true},"documents":[]}"""
    }
}
