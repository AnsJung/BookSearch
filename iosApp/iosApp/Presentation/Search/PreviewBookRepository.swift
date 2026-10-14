#if DEBUG
import SharedLogic

/// Preview에서 Koin과 실제 네트워크 없이 검색 화면을 구성한다.
final class PreviewBookRepository: BookRepository {
    func searchByTitle(query: String, sort: BookSort) async throws -> [Book] {
        []
    }
}
#endif
