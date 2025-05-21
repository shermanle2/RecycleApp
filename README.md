# ♻️ RecycleApp

스마트 분리수거를 지원하는 Android 애플리케이션입니다.  
Kotlin 및 Jetpack Compose를 기반으로 제작되었으며, 사용자 편의성과 직관적인 UI를 제공합니다.

---

## 📱 주요 기능

- 사용자의 분리배출 기록 확인
- 리사이클 관련 정보 제공
- 이미지 기반 쓰레기 분류 지원 (예정)
- 직관적인 네비게이션 및 화면 구성

---

## ⚙️ 개발 환경

| 항목              | 내용                                |
|------------------|-------------------------------------|
| IDE              | Android Studio Giraffe (2022.3.1) 이상 |
| 언어              | Kotlin                              |
| 빌드 시스템       | Gradle (KTS)                        |
| 최소 SDK 버전     | 24                                  |
| 타겟 SDK 버전     | 33                                  |
| JDK              | 17                                  |

---

## 🧪 실행 환경

- 에뮬레이터: **Medium Phone API 36** (Pixel 3a 기준)
- Jetpack Compose UI 기반 화면 구성
- `google-services.json` 포함 (개인 개발자 테스트용 Firebase 설정)

---

## 📁 프로젝트 구조

RecycleApp/
├── app/
│ ├── src/ # 앱 소스 코드
│ ├── build.gradle.kts # 모듈별 Gradle 설정
│ └── res/drawable/ # 커스텀 이미지 리소스
├── build.gradle.kts # 프로젝트 전역 Gradle 설정
├── settings.gradle.kts
├── local.properties # 로컬 Android SDK 경로 (Git에 포함 안됨)
├── google-services.json # Firebase 설정 파일
└── libs.versions.toml # 라이브러리 버전 정의

---

## 🔐 주의사항

- `local.properties` 및 `.idea/`, `build/`, `.gradle/` 폴더는 `.gitignore` 처리되어 있습니다.
- `google-services.json`은 테스트 목적의 개발자 Firebase 설정입니다. 실제 배포 전에는 보안상 키를 관리해야 합니다.

