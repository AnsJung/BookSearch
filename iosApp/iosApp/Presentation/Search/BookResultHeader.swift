import SharedLogic
import SwiftUI

// MARK: - 검색 결과 헤더
struct BookResultHeader: View {
    
    // MARK: - 검색 결과·정렬 상태
    let query: String
    let resultCount: Int
    let sort: BookSort
    let onSortChanged: (BookSort) -> Void
    @State private var isMenuOpen = false
    
    // MARK: - 결과 수와 정렬 버튼
    var body: some View {
        HStack(spacing: 8) {
            HStack(spacing: 4) {
                Text("‘\(query)’")
                    .bookTextStyle(.labelLarge)
                    .foregroundStyle(BookColors.textPrimary)
                    .lineLimit(1)
                    .layoutPriority(1)
                Text("표시된 도서 \(resultCount)권")
                    .bookTextStyle(.bodySmall)
                    .foregroundStyle(BookColors.textSecondary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Button {
                isMenuOpen.toggle()
            } label: {
                HStack(spacing: 5) {
                    Text(sort.name == BookSort.latest.name ? "발간일순" : "정확도순")
                        .bookTextStyle(.labelMedium)
                    Image(isMenuOpen ? "ic_sort_up" : "ic_sort_down")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 12, height: 12)
                }
                .foregroundStyle(isMenuOpen ? BookColors.accent : BookColors.textPrimary)
                .frame(width: 96, height: 48)
                .background(isMenuOpen ? BookColors.accentSubtle : BookColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay {
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(isMenuOpen ? BookColors.accent : BookColors.border, lineWidth: 1)
                }
            }
            .buttonStyle(.plain)
            .overlay(alignment: .topTrailing) {
                if isMenuOpen {
                    sortMenu
                        .offset(y: 68)
                        .zIndex(2)
                }
            }
        }
        .padding(.horizontal, 20)
        .frame(height: 50)
        .frame(maxWidth: .infinity)
        .background(BookColors.surface)
        .overlay(alignment: .top) { BookColors.border.frame(height: 1) }
        .overlay(alignment: .bottom) { BookColors.border.frame(height: 1) }
        .zIndex(2)
    }

    // MARK: - 정렬 메뉴
    private var sortMenu: some View {
        VStack(spacing: 0) {
            sortOption("정확도순", value: .accuracy)
            sortOption("발간일순", value: .latest)
        }
        .padding(4)
        .frame(width: 132)
        .background(BookColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(BookColors.border, lineWidth: 1))
    }
    
    // MARK: - 정렬 항목
    private func sortOption(_ label: String, value: BookSort) -> some View {
        let isSelected = sort.name == value.name
        return Button {
            isMenuOpen = false
            if !isSelected { onSortChanged(value) }
        } label: {
            HStack {
                Text(label)
                    .bookTextStyle(isSelected ? .labelLarge : .bodySmall)
                    .foregroundStyle(isSelected ? BookColors.accent : BookColors.textSecondary)
                Spacer()
                if isSelected {
                    Image("ic_sort_selected")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 16, height: 16)
                }
            }
            .padding(.horizontal, 12)
            .frame(height: 48)
            .background(isSelected ? BookColors.accentSubtle : BookColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 6))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

// MARK: - Preview
#Preview("검색 결과 헤더") {
    VStack {
        BookResultHeader(query: "바다의 기억", resultCount: 20, sort: .accuracy, onSortChanged: { _ in })
        Spacer()
    }
    .background(BookColors.background)
    .bookTheme()
}
