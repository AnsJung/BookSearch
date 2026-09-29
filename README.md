# BookSearch

카카오 도서 검색 API를 활용해 책을 검색하고 상세 정보를 확인하는 Android·iOS 앱입니다.
Kotlin Multiplatform으로 비즈니스 로직과 데이터 처리를 공유하고, 각 플랫폼의 네이티브 UI로 화면을 구현합니다.

## 사용 기술

아래는 개발 과정에서 적용할 기술입니다.

| 구분 | 기술 |
| --- | --- |
| 언어 | Kotlin, Swift |
| 공통 로직 | Kotlin Multiplatform (KMP) |
| UI | Android: Jetpack Compose / iOS: SwiftUI |
| 아키텍처 | Clean Architecture + MVVM |
| 네트워크 | Ktor Client |
| 직렬화·역직렬화 | kotlinx.serialization |
| 비동기 처리 | Kotlin Coroutines, Flow |
| 이미지 로딩·캐싱 | Android: Coil / iOS: Nuke, NukeUI |
| 설정 저장 | Preferences DataStore |
| 의존성 주입 | Koin |
| API | 카카오 도서 검색 API |

Presentation은 플랫폼별로 구현하고, Domain과 Data는 공통 코드로 구성합니다.

## 주요 기능

초기 구현 범위입니다.

- **도서 검색**: 검색어로 책을 검색합니다. 초기 버전에는 디바운스를 적용하지 않습니다.
- **검색 결과 목록**: 책 표지, 제목, 저자 등 주요 정보를 표시합니다.
- **도서 상세 정보**: 목록에서 선택한 책의 소개, 출판사, 출간일, ISBN 등을 확인합니다.
- **화면 상태 처리**: 로딩, 검색 결과 없음, 오류 상태와 재시도를 제공합니다.

## 디자인

피그마 에이전트 & GPT를 사용하여 디자인 설계 및 반영

https://www.figma.com/design/6HGmFLvSAkWQx75Mi0d0Mb/%EB%8F%84%EC%84%9C%EA%B2%80%EC%83%89%EC%95%B1?t=wyKD3hKJNGwmdHog-1