import SwiftUI

// MARK: - 공통 메시지 팝업

/// 검색어 입력 오류처럼 확인 후 닫는 메시지를 표시한다.
struct BookMessageDialog: View {
    // MARK: - 메시지·확인 버튼 설정
    let message: String
    var confirmText = "확인"
    let onDismiss: () -> Void

    // MARK: - 딤 배경과 팝업 콘텐츠
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 24) {
                Text(message)
                    .bookTextStyle(.bodyLarge)
                    .foregroundStyle(BookColors.textPrimary)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                Button(confirmText, action: onDismiss)
                    .buttonStyle(BookPrimaryButtonStyle())
            }
            .padding(24)
            .frame(maxWidth: 360)
            .background(BookColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 24)
        }
        .accessibilityAddTraits(.isModal)  
    }
}

// MARK: - Preview

#Preview("공통 메시지") {
    BookColors.background
        .ignoresSafeArea()
        .overlay {
            BookMessageDialog(message: "검색어를 입력해 주세요.", onDismiss: {})
        }
        .bookTheme()
}
