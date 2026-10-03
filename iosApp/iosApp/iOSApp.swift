import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    init() {
        guard let apiKey = Bundle.main.object(
            forInfoDictionaryKey: "KAKAO_REST_API_KEY"
        ) as? String, !apiKey.isEmpty else {
            fatalError("KAKAO_REST_API_KEY 설정이 필요합니다.")
        }
        KoinInitializerKt.doInitKoin(apiKey: apiKey)
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
