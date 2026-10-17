import Foundation
import Observation
import SharedLogic

/// 검색 화면의 상태와 검색 요청을 관리한다.
@MainActor
@Observable
final class BookSearchViewModel {
    // MARK: - 화면 상태
    var query = ""
    private(set) var submittedQuery: String?
    private(set) var sort: BookSort = .accuracy
    private(set) var books: [Book] = []
    private(set) var totalCount: Int?
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    private(set) var canRetry = false
    private(set) var isAppending = false
    private(set) var appendErrorMessage: String?
    private(set) var canRetryAppend = false
    private(set) var queryErrorMessage: String?
    private(set) var searchRequestVersion = 0
    // MARK: - 검색 요청 관리용 내부 값
    @ObservationIgnored private var activeSearchCondition: (query: String, sort: String)?
    @ObservationIgnored private var generation = 0
    @ObservationIgnored private let bridge: BookPagingBridge

    init(useCase: SearchBooksUseCase) {
        bridge = BookPagingBridge(searchBooksPage: useCase)
    }

    deinit {
        bridge.close()
    }

    // MARK: - 사용자 입력과 화면 동작
    func onQueryChanged(_ value: String) {
        query = value
        queryErrorMessage = nil
    }

    func dismissQueryError() {
        queryErrorMessage = nil
    }

    func search() {
        executeSearch(query: query.trimmingCharacters(in: .whitespacesAndNewlines), sort: sort)
    }

    func onSortChanged(_ value: BookSort) {
        guard sort.name != value.name else { return }
        sort = value
        guard let submittedQuery else { return }
        executeSearch(query: submittedQuery, sort: value)
    }

    func retry() {
        guard !isLoading, canRetry else { return }
        bridge.retry()
    }

    func retryAppend() {
        guard !isAppending, canRetryAppend else { return }
        bridge.retry()
    }

    func onBookAppeared(at index: Int) {
        bridge.onItemVisible(index: Int32(index))
    }
    
    // MARK: - 검색 실행
    private func executeSearch(query: String, sort: BookSort) {
        guard !query.isEmpty else {
            queryErrorMessage = "검색어를 입력해주세요."
            return
        }

        let condition = (query: query, sort: sort.name)
        if isLoading, let activeSearchCondition,
           activeSearchCondition.query == condition.query,
           activeSearchCondition.sort == condition.sort { return }

        generation += 1
        searchRequestVersion += 1
        let requestGeneration = generation
        activeSearchCondition = condition
        submittedQuery = query
        books = []
        totalCount = nil
        isLoading = true
        errorMessage = nil
        canRetry = false
        isAppending = false
        appendErrorMessage = nil
        canRetryAppend = false
        queryErrorMessage = nil

        bridge.start(query: query, sort: sort) { [weak self] pagingState in
            Task { @MainActor [weak self] in
                guard let self, requestGeneration == self.generation else { return }
                self.books = pagingState.books
                self.totalCount = pagingState.totalCount.map { Int($0.int32Value) }
                self.isLoading = pagingState.isRefreshing
                self.isAppending = pagingState.isAppending
                self.errorMessage = pagingState.refreshError.map { Self.message(for: $0.failure) }
                self.canRetry = pagingState.refreshError?.canRetry ?? false
                self.appendErrorMessage = pagingState.appendError.map { Self.message(for: $0.failure) }
                self.canRetryAppend = pagingState.appendError?.canRetry ?? false
            }
        }
    }
    
    // MARK: - 오류 유형별 표시 정책
    private static func message(for failure: BookSearchFailure?) -> String {
        switch failure?.name {
        case BookSearchFailure.network.name:
            "인터넷 연결을 확인한 뒤 다시 시도해 주세요."
        case BookSearchFailure.timeout.name:
            "응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요."
        case BookSearchFailure.server.name:
            "서비스에 일시적인 문제가 있어요. 잠시 후 다시 시도해 주세요."
        case BookSearchFailure.authentication.name, BookSearchFailure.permissionDenied.name:
            "현재 도서 검색 서비스를 이용할 수 없어요."
        case BookSearchFailure.rateLimited.name:
            "검색 이용량이 많아 잠시 후 다시 이용해 주세요."
        case BookSearchFailure.invalidRequest.name:
            "검색 요청을 처리할 수 없어요."
        case BookSearchFailure.responseParsing.name:
            "도서 정보를 처리하지 못했어요."
        default:
            "잠시 후 다시 시도해 주세요."
        }
    }
}
