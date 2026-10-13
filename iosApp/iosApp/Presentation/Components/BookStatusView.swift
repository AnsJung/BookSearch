import SwiftUI

// MARK: - 공통 상태 안내
/// 검색 전·결과 없음·오류 상태에서 공통으로 쓰는 안내 영역.
struct BookStatusView: View {
    // MARK: - 안내 문구·아이콘·버튼 설정
    let title: String
    let message: String
    var iconName = "ic_search_intro"
    var titleStyle: BookTypography = .preSearchTitle
    var actionTitle: String? = nil
    var onAction: (() -> Void)? = nil

    // MARK: - 상태 안내 콘텐츠
    var body: some View {
        VStack(spacing: 12) {
            Image(iconName)
                .resizable()
                .scaledToFit()
                .frame(width: 26, height: 26)
                .frame(width: 56, height: 56)
                .background(BookColors.accentSubtle, in: Circle())

            Text(title)
                .bookTextStyle(titleStyle)
                .foregroundStyle(BookColors.textPrimary)
                .multilineTextAlignment(.center) // ?

            Text(message)
                .bookTextStyle(.bodyMedium)
                .foregroundStyle(BookColors.textSecondary)
                .multilineTextAlignment(.center)

            if let actionTitle, let onAction {
                Button(actionTitle, action: onAction)
                    .buttonStyle(BookOutlineButtonStyle())
                    .frame(width: 160)
                    .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 28)
    }
}

// MARK: - Preview
#Preview("안내 상태") {
    VStack(spacing: 48) {
        BookStatusView(title: "어떤 책을 찾고 있나요?", message: "책 제목으로 검색해 보세요.")
        BookStatusView(
            title: "도서를 불러오지 못했어요",
            message: "인터넷 연결을 확인한 뒤 다시 시도해 주세요.",
            titleStyle: .titleMedium,
            actionTitle: "다시 시도",
            onAction: {}
        )
    }
    .background(BookColors.background)
    .bookTheme()
}
