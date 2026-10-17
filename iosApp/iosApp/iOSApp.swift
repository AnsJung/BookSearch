import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    private let searchBooksUseCase: SearchBooksUseCase

    init() {
        guard let apiKey = Bundle.main.object(
            forInfoDictionaryKey: "KAKAO_REST_API_KEY"
        ) as? String, !apiKey.isEmpty else {
            fatalError("KAKAO_REST_API_KEY 설정이 필요합니다.")
        }
        #if DEBUG
        let enableHttpLogging = true
        #else
        let enableHttpLogging = false
        #endif
        KoinInitializerKt.doInitKoin(apiKey: apiKey, enableHttpLogging: enableHttpLogging)
        searchBooksUseCase = SearchBooksUseCaseProvider().searchBooksUseCase
    }
    var body: some Scene {
        WindowGroup {
            ContentView(useCase: searchBooksUseCase)
                .bookTheme()
        }
    }
}
