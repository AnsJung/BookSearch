import SharedLogic
import SwiftUI

// MARK: - 검색 결과 도서 항목
struct BookResultRow: View {
    // MARK: - 도서 데이터·선택 동작
    let book: Book
    let onSelect: () -> Void
    
    // MARK: - 표지·도서 정보·상세 아이콘
    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 16) {
                BookCoverView(thumbnail: book.thumbnail, width: 68, height: 96)

                VStack(alignment: .leading, spacing: 4) {
                    Text(book.title)
                        .bookTextStyle(.titleSmall)
                        .foregroundStyle(BookColors.textPrimary)
                        .lineLimit(2)

                    let authors = book.authors.filter { !$0.isEmpty }.joined(separator: " · ")
                    if !authors.isEmpty {
                        Text(authors)
                            .bookTextStyle(.bodySmall)
                            .foregroundStyle(BookColors.textSecondary)
                            .lineLimit(1)
                    }

                    if !book.publisher.isEmpty {
                        Text(book.publisher)
                            .bookTextStyle(.bodySmall)
                            .foregroundStyle(BookColors.textSecondary.opacity(0.8))
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Image("ic_detail")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 18, height: 18)
                    .frame(width: 48, height: 48)
                    .accessibilityHidden(true)
            }
            .padding(.leading, 20)
            .padding(.trailing, 12)
            .frame(height: 112)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(BookColors.surface)
            .overlay(alignment: .bottom) {
                Rectangle()
                    .fill(BookColors.border)
                    .frame(height: 1)
                    .padding(.trailing, 40)
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(book.title), 상세 보기")
    }
}

// MARK: - Preview
#Preview("검색 결과 아이템") {
    BookResultRow(
        book: Book(
            title: "바다의 기억을 엮는 사람들", contents: "", url: "", isbn: "",
            datetime: "", authors: ["서은채", "윤해솔"], publisher: "여름의서가",
            translators: [], price: nil, salePrice: nil, thumbnail: "", status: ""
        ),
        onSelect: {}
    )
    .bookTheme()
}
