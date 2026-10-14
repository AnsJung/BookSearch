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
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    private(set) var canRetry = false
    private(set) var queryErrorMessage: String?
    private(set) var searchRequestVersion = 0
    // MARK: - 검색 요청 관리용 내부 값
    @ObservationIgnored private var searchTask: Task<Void, Never>?
    @ObservationIgnored private var activeSearchCondition: (query: String, sort: String)?
    @ObservationIgnored private var generation = 0
    @ObservationIgnored private var useCase: SearchBooksUseCase?
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
        guard !isLoading, canRetry, let submittedQuery else { return }
        executeSearch(query: submittedQuery, sort: sort)
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

        searchTask?.cancel()
        generation += 1
        searchRequestVersion += 1
        let requestGeneration = generation
        activeSearchCondition = condition
        submittedQuery = query
        books = []
        isLoading = true
        errorMessage = nil
        canRetry = false
        queryErrorMessage = nil

        // Preview에서는 검색 전까지 Koin을 참조하지 않는다.
        if useCase == nil { useCase = SearchBooksUseCaseProvider().searchBooksUseCase }
        guard let useCase else { return }

        searchTask = Task {
            defer {
                if requestGeneration == generation { isLoading = false }
            }
            do {
                let result = try await useCase.invoke(query: query, sort: sort)
                guard !Task.isCancelled, requestGeneration == generation else { return }
                books = result
            } catch {
                guard !Task.isCancelled, requestGeneration == generation else { return }
                let failure = ((error as NSError).kotlinException as? BookSearchException)?.failure
                errorMessage = Self.message(for: failure)
                canRetry = Self.canRetry(failure)
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

    private static func canRetry(_ failure: BookSearchFailure?) -> Bool {
        guard let failure else { return false }
        return [BookSearchFailure.network, .timeout, .server].contains { $0.name == failure.name }
    }
}
