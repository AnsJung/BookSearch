import NukeUI
import SwiftUI

// MARK: - 공통 도서 표지

/// 검색 목록과 상세 화면에서 같은 이미지·대체 이미지 정책을 사용한다.
struct BookCoverView: View {
    // MARK: - 이미지 주소·표지 크기
    let thumbnail: String
    let width: CGFloat
    let height: CGFloat

    // MARK: - 이미지 URL 검증
    private var imageURL: URL? {
        guard let url = URL(string: thumbnail),
              ["http", "https"].contains(url.scheme?.lowercased() ?? ""),
              url.host != nil else { return nil }
        return url
    }

    // MARK: - 이미지 로딩 상태별 콘텐츠
    var body: some View {
        Group {
            if let imageURL {
                LazyImage(url: imageURL) { state in
                    if let image = state.image {
                        image
                            .resizable()
                            .scaledToFit()
                    } else if state.error != nil {
                        placeholder
                    } else {
                        BookColors.disabledContainer
                    }
                }
            } else {
                placeholder
            }
        }
        .frame(width: width, height: height)
        .background(BookColors.disabledContainer)
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .accessibilityLabel("도서 표지")
    }

    // MARK: - 표지 없음·로딩 실패 시 대체 콘텐츠
    private var placeholder: some View {
        VStack(spacing: 8) {
            Image("ic_book_placeholder")
                .resizable()
                .scaledToFit()
                .foregroundStyle(BookColors.disabledContent)
                .frame(width: 28, height: 28)
            if width >= 100 {
                Text("표지 정보 없음")
                    .bookTextStyle(.labelSmall)
                    .foregroundStyle(BookColors.textSecondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(BookColors.disabledContainer)
    }
}

// MARK: - Preview
#Preview("도서 표지") {
    HStack(spacing: 20) {
        BookCoverView(thumbnail: "", width: 68, height: 96)
        BookCoverView(thumbnail: "", width: 144, height: 204)
    }
    .padding()
    .bookTheme()
}
