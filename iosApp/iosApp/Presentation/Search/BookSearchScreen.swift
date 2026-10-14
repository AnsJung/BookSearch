import SharedLogic
import SwiftUI

/// 검색 상태에 따라 검색 전·로딩·오류·결과 화면을 전환한다.
struct BookSearchScreen: View {
    // 화면이 소유하는 @Observable 모델. 읽은 속성이 바뀌면 관련 UI가 갱신된다.
    @State private var model: BookSearchViewModel

    // 상세 화면으로 이동할 책의 인덱스를 보관하는 화면 전용 상태.
    @State private var navigationPath: [Int] = []

    init(useCase: SearchBooksUseCase) {
        _model = State(initialValue: BookSearchViewModel(useCase: useCase))
    }

    // MARK: - 화면 구성
    var body: some View {
        NavigationStack(path: $navigationPath) {
            VStack(spacing: 0) {
                BookSearchBar(
                    // 입력값은 모델에서 읽고, 변경은 모델의 메서드를 통해 전달한다.
                    query: Binding(
                        get: { model.query },
                        set: { model.onQueryChanged($0) }
                    ),
                    onSearch: model.search
                )

                content
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(BookColors.background)
            .navigationBarHidden(true)
            .navigationDestination(for: Int.self) { index in
                // 결과가 교체되어 인덱스가 유효하지 않으면 상세 화면을 만들지 않는다.
                if model.books.indices.contains(index) {
                    BookDetailScreen(book: model.books[index]) {
                        navigationPath.removeLast()
                    }
                }
            }
            .overlay {
                // 빈 검색어 오류만 공통 팝업으로 표시한다.
                if let message = model.queryErrorMessage {
                    BookMessageDialog(message: message, onDismiss: model.dismissQueryError)
                }
            }
        }
    }

    // MARK: - 검색 상태별 화면
    @ViewBuilder
    private var content: some View {
        // 우선순위에 따라 한 번에 하나의 화면 상태만 보여준다.
        if model.isLoading {
            BookLoadingSkeleton(query: model.submittedQuery ?? model.query)
        } else if model.errorMessage != nil {
            errorContent
        } else if model.submittedQuery == nil {
            preSearchContent
        } else if model.books.isEmpty {
            emptyContent
        } else {
            resultContent
        }
    }

    private var errorContent: some View {
        VStack {
            BookStatusView(
                title: "도서를 불러오지 못했어요",
                message: model.errorMessage ?? "",
                titleStyle: .titleMedium,
                actionTitle: model.canRetry ? "다시 시도" : nil,
                onAction: { model.retry() }
            )
            .padding(.top, 126)
            Spacer()
        }
    }

    private var preSearchContent: some View {
        VStack {
            BookStatusView(title: "어떤 책을 찾고 있나요?", message: "책 제목으로 검색해 보세요.")
                .padding(.top, 162)
            Spacer()
        }
    }

    private var emptyContent: some View {
        VStack {
            BookStatusView(
                title: "검색 결과가 없어요",
                message: "검색어를 바꾸거나 책 제목을 확인해 보세요."
            )
            .padding(.top, 118)
            Spacer()
        }
    }

    private var resultContent: some View {
        VStack(spacing: 0) {
            BookResultHeader(
                query: model.submittedQuery ?? "",
                resultCount: model.books.count,
                sort: model.sort,
                onSortChanged: model.onSortChanged
            )
            .zIndex(1)

            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(Array(model.books.enumerated()), id: \.offset) { index, book in
                        BookResultRow(book: book) {
                            navigationPath.append(index)
                        }
                    }
                }
            }
            // 새 검색마다 ScrollView를 새로 만들어 스크롤 위치를 초기화한다.
            .id(model.searchRequestVersion)
            .background(BookColors.surface)
        }
    }
}

#if DEBUG
#Preview("검색 전") {
    BookSearchScreen(useCase: SearchBooksUseCase(repository: PreviewBookRepository()))
        .bookTheme()
}
#endif
