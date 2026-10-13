import SwiftUI

// MARK: - 기본 채움 버튼 스타일
struct BookPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        BookPrimaryButtonBody(configuration: configuration)
    }

    // MARK: - 활성화·누름 상태를 반영하는 버튼 콘텐츠
    private struct BookPrimaryButtonBody: View {
        let configuration: ButtonStyle.Configuration
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            configuration.label
                .bookTextStyle(.labelLarge)
                .foregroundStyle(isEnabled ? BookColors.surface : BookColors.disabledContent)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(isEnabled ? BookColors.accent : BookColors.disabledContainer)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .opacity(configuration.isPressed ? 0.85 : 1)
        }
    }
}

// MARK: - 테두리 버튼 스타일
struct BookOutlineButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .bookTextStyle(.labelLarge)
            .foregroundStyle(BookColors.accent)
            .frame(maxWidth: .infinity)
            .frame(height: 48)
            .background(BookColors.surface)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(BookColors.accent, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .opacity(configuration.isPressed ? 0.7 : 1)
    }
}

// MARK: - Preview
#Preview("공통 버튼") {
    VStack(spacing: 16) {
        Button("확인") {}.buttonStyle(BookPrimaryButtonStyle())
        Button("다시 시도") {}.buttonStyle(BookOutlineButtonStyle())
        Button("비활성") {}.buttonStyle(BookPrimaryButtonStyle()).disabled(true)
    }
    .padding(24)
    .background(BookColors.background)
    .bookTheme()
}
