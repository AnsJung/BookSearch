import SwiftUI

// MARK: - 검색 로딩 스켈레톤
struct BookLoadingSkeleton: View {
    let query: String
    @State private var shimmer = false
    
    // MARK: - 로딩 화면 구성
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("‘\(query)’ 검색 중")
                    .bookTextStyle(.bodySmall)
                    .foregroundStyle(BookColors.textSecondary)
                    .lineLimit(1)
                Spacer()
                ProgressView()
                    .tint(BookColors.accent)
                    .scaleEffect(0.7)
            }
            .padding(.horizontal, 20)
            .frame(height: 50)
            .overlay(alignment: .top) { BookColors.border.frame(height: 1) }
            .overlay(alignment: .bottom) { BookColors.border.frame(height: 1) }

            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(0..<8, id: \.self) { _ in
                        HStack(spacing: 16) {
                            shimmerBlock(width: 68, height: 96)
                            VStack(alignment: .leading, spacing: 8) {
                                shimmerBlock(height: 14)
                                shimmerBlock(width: 180, height: 10)
                                shimmerBlock(width: 120, height: 10)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            Color.clear.frame(width: 48, height: 48)
                        }
                        .padding(.leading, 20)
                        .padding(.trailing, 12)
                        .frame(height: 112)
                        .overlay(alignment: .bottom) {
                            BookColors.border.frame(height: 1).padding(.trailing, 40)
                        }
                    }
                }
            }
            .scrollDisabled(true)
        }
        .background(BookColors.surface)
        .onAppear {
            // 화면이 나타나면 밝은 띠의 이동을 반복한다.
            withAnimation(.linear(duration: 1.4).repeatForever(autoreverses: false)) {
                shimmer = true
            }
        }
    }
    
    // MARK: - 시머 블록
    private func shimmerBlock(width: CGFloat? = nil, height: CGFloat) -> some View {
        Rectangle()
            .fill(BookColors.disabledContainer)
            .overlay {
                LinearGradient(
                    colors: [.clear, BookColors.surface.opacity(0.9), .clear],
                    startPoint: .leading,
                    endPoint: .trailing
                )
                .frame(width: 80)
                .offset(x: shimmer ? 220 : -220)
            }
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil)
            .clipShape(RoundedRectangle(cornerRadius: width == 68 ? 4 : 3))
    }
}

// MARK: - Preview
#Preview("검색 로딩") {
    BookLoadingSkeleton(query: "바다의 기억")
        .bookTheme()
}
