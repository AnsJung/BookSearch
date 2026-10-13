import SharedLogic
import SwiftUI

/// 검색 결과의 Book을 그대로 사용하므로 상세 화면에서 추가 요청을 하지 않는다.
struct BookDetailScreen: View {
    // MARK: - 도서 데이터·뒤로 가기 동작
    let book: Book
    let onBack: () -> Void

    // MARK: - 상세 화면 구성
    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 4) {
                Button(action: onBack) {
                    Image("ic_back")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                        .frame(width: 48, height: 48)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("뒤로 가기")

                Text("도서 정보")
                    .bookTextStyle(.titleMedium)
                    .foregroundStyle(BookColors.textPrimary)
                Spacer()
            }
            .padding(.leading, 4)
            .padding(.trailing, 20)
            .frame(height: 56)

            BookColors.border.frame(height: 1)

            ScrollView {
                VStack(spacing: 20) {
                    bookOverview

                    let infoRows = bookInfoRows
                    if !infoRows.isEmpty {
                        BookColors.border.frame(height: 1)
                        VStack(spacing: 6) {
                            ForEach(infoRows.indices, id: \.self) { index in
                                HStack(alignment: .top, spacing: 16) {
                                    Text(infoRows[index].0)
                                        .foregroundStyle(BookColors.textSecondary)
                                    Spacer(minLength: 0)
                                    Text(infoRows[index].1)
                                        .foregroundStyle(BookColors.textPrimary)
                                        .multilineTextAlignment(.trailing)
                                }
                                .bookTextStyle(.bodyMedium)
                            }
                        }
                    }

                    if !book.contents.isEmpty {
                        BookColors.border.frame(height: 1)
                        VStack(alignment: .leading, spacing: 8) {
                            Text("책 소개")
                                .bookTextStyle(.titleMedium)
                                .foregroundStyle(BookColors.textPrimary)
                            Text(book.contents)
                                .bookTextStyle(.bodyMedium)
                                .foregroundStyle(BookColors.textSecondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 48)
            }
        }
        .background(BookColors.surface)
        .navigationBarHidden(true)
    }

    // MARK: - 표지·제목·저자·출판사
    private var bookOverview: some View {
        VStack(spacing: 16) {
            BookCoverView(thumbnail: book.thumbnail, width: 144, height: 204)

            VStack(spacing: 6) {
                Text(book.title)
                    .bookTextStyle(.titleLarge)
                    .foregroundStyle(BookColors.textPrimary)
                    .multilineTextAlignment(.center)

                let authors = book.authors.filter { !$0.isEmpty }.joined(separator: " · ")
                if !authors.isEmpty {
                    Text(authors)
                        .bookTextStyle(.bodyLarge)
                        .foregroundStyle(BookColors.textSecondary)
                        .multilineTextAlignment(.center)
                }

                if !book.publisher.isEmpty {
                    Text(book.publisher)
                        .bookTextStyle(.bodySmall)
                        .foregroundStyle(BookColors.textSecondary.opacity(0.8))
                        .multilineTextAlignment(.center)
                }
            }
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - 출간일·ISBN·가격 정보 구성
    private var bookInfoRows: [(String, String)] {
        var rows: [(String, String)] = []
        if let date = formattedDate(book.datetime) { rows.append(("출간일", date))
        }
        let isbnTokens = book.isbn.split(whereSeparator: \.isWhitespace).map(String.init)
        for (label, length) in [("ISBN10", 10), ("ISBN13", 13)] {
            if let value = isbnTokens.first(where: { $0.replacingOccurrences(of: "-", with: "").count == length }) {
                rows.append((label, value))
            }
        }
        if let price = book.price { rows.append(("정가", formattedPrice(price.intValue)))
        }
        if let salePrice = book.salePrice { rows.append(("판매가", formattedPrice(salePrice.intValue)))
        }
        return rows
    }

    // MARK: - 출간일 표시 형식
    private func formattedDate(_ value: String) -> String? {
        let prefix = String(value.prefix(10))
        let parts = prefix.split(separator: "-")
        guard parts.count == 3, parts[0].count == 4, parts[1].count == 2, parts[2].count == 2,
              parts.allSatisfy({ $0.allSatisfy(\.isNumber) }) else { return nil }
        return "\(parts[0]). \(parts[1]). \(parts[2])"
    }

    // MARK: - 가격 표시 형식
    private func formattedPrice(_ value: Int) -> String {
        value.formatted(.number.locale(Locale(identifier: "ko_KR"))) + "원"
    }
}

// MARK: - Preview
#Preview("도서 상세") {
    BookDetailScreen(
        book: Book(
            title: "바다의 기억을 엮는 사람들", contents: "오래된 항구 도시에서 만난 사람들의 이야기입니다.",
            url: "", isbn: "8960777331 9788960777330", datetime: "2026-08-21T00:00:00.000+09:00",
            authors: ["서은채", "윤해솔"], publisher: "여름의서가", translators: [],
            price: KotlinInt(int: 18000), salePrice: KotlinInt(int: 16200), thumbnail: "", status: ""
        ),
        onBack: {}
    )
    .bookTheme()
}
