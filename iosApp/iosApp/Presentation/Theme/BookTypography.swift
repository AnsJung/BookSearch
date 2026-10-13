import SwiftUI

/// Android의 BookTypography에 대응하는 Inter 텍스트 스타일.
enum BookTypography {
    case titleLarge
    case titleMedium
    case titleSmall
    case preSearchTitle
    case bodyLarge
    case bodyMedium
    case bodySmall
    case labelLarge
    case labelMedium
    case labelSmall

    private var fontName: String {
        switch self {
        case .titleLarge, .titleMedium, .preSearchTitle: "Inter-Bold"
        case .bodyLarge, .bodyMedium, .bodySmall: "Inter-Regular"
        case .titleSmall, .labelLarge, .labelMedium: "Inter-SemiBold"
        case .labelSmall: "Inter-Medium"
        }
    }

    private var size: CGFloat {
        switch self {
        case .titleLarge: 22
        case .titleMedium: 18
        case .titleSmall: 16
        case .preSearchTitle: 20
        case .bodyLarge: 16
        case .bodyMedium: 15
        case .bodySmall: 13
        case .labelLarge: 13
        case .labelMedium: 12
        case .labelSmall: 11
        }
    }

    var font: Font { .custom(fontName, size: size) }

    /// 글꼴의 기본 행 간격에 추가할 줄 간격. 절대 행 높이를 지정하는 값은 아니다.
    var lineSpacing: CGFloat {
        switch self {
        case .titleLarge, .titleMedium: 7
        case .preSearchTitle: 4
        case .bodyLarge, .bodyMedium: 8
        case .labelLarge: 5
        case .labelSmall: 4
        // 직접 지정하던 글꼴에서 옮긴 스타일은 기존의 기본 줄 간격을 유지한다.
        case .titleSmall, .bodySmall, .labelMedium: 0
        }
    }
}

extension View {
    func bookTextStyle(_ style: BookTypography) -> some View {
        font(style.font)
            .lineSpacing(style.lineSpacing)
    }

    func bookTheme() -> some View {
        tint(BookColors.accent)
            .preferredColorScheme(.light)
    }
}
