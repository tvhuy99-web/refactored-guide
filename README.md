# Mineclonia Blind VI — GitHub APK build

Repository: `tvhuy99-web/refactored-guide`

The source is stored as one compressed archive, split only into transport parts so it can be uploaded safely without thousands of individual file commits. GitHub Actions reassembles and extracts it before building.

To build a debug APK, open **Actions → Build Mineclonia Blind VI APK → Run workflow**. A push to `main` also starts the build.

The build uses the Android configuration already present in the source:
- Gradle wrapper 8.14.5
- Android Gradle Plugin 8.13.2
- Java 17
- NDK 29.0.14206865
- ABI outputs: armeabi-v7a, arm64-v8a, x86 and x86_64

The archive intentionally excludes generated compiler artifacts, APK/ZIP build outputs, the local Android dependency archive, and the private keystore. The workflow downloads the dependency archive and verifies its pinned SHA-256 before compiling.
