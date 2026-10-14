import SwiftUI

// MARK: - 도서 검색 바
struct BookSearchBar: View {
    // MARK: - 검색어·검색 동작·입력 포커스
    @Binding var query: String
    let onSearch: () -> Void
    @FocusState private var isFocused: Bool
    
    // MARK: - 검색 입력창과 검색 버튼
    var body: some View {
        HStack(spacing: 8) {
            HStack(spacing: 12) {
                Image("ic_search")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 20, height: 20)
                    .accessibilityHidden(true)

                TextField("책 제목을 검색하세요", text: $query)
                    .bookTextStyle(.bodyMedium)
                    .foregroundStyle(BookColors.textPrimary)
                    .tint(BookColors.accent)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .submitLabel(.search)
                    .focused($isFocused)
                    .onSubmit(onSearch)

                if !query.isEmpty {
                    Button {
                        query = ""
                    } label: {
                        Image("ic_clear")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 14, height: 14)
                            .frame(width: 40, height: 40)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("검색어 지우기")
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 52)
            .background(BookColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay {
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isFocused ? BookColors.accent : BookColors.border, lineWidth: 1)
            }

            Button(action: onSearch) {
                Text("검색")
                    .bookTextStyle(.labelLarge)
                    .foregroundStyle(BookColors.surface)
                    .frame(width: 62, height: 52)
                    .background(BookColors.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }
}

// MARK: - Preview
#Preview("검색 바") {
    BookSearchBar(query: .constant(""), onSearch: {})
        .background(BookColors.background)
        .bookTheme()
}
