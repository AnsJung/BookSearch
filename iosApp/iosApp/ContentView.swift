import SharedLogic
import SwiftUI

struct ContentView: View {
    let useCase: SearchBooksUseCase

    var body: some View {
        BookSearchScreen(useCase: useCase)
    }
}

#if DEBUG
#Preview {
    ContentView(useCase: SearchBooksUseCase(repository: PreviewBookRepository()))
        .bookTheme()
}
#endif
