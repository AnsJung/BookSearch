import SwiftUI

/// 앱의 의미별 색상. 실제 색상 값은 Assets.xcassets의 Color Set에서 관리한다.
enum BookColors {
    static let background = Color("BookBackground")
    static let surface = Color("BookSurface")
    static let textPrimary = Color("BookTextPrimary")
    static let textSecondary = Color("BookTextSecondary")
    static let border = Color("BookBorder")
    static let accent = Color("AccentColor")
    static let accentSubtle = Color("BookAccentSubtle")
    static let error = Color("BookError")
    static let disabledContainer = Color("BookDisabledContainer")
    static let disabledContent = Color("BookDisabledContent")
}
