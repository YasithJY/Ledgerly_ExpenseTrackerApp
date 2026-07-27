# Fix Build Error: Cannot query the value of property 'testedVariantArtifacts$kotlin_gradle_plugin_common'

The error is caused by a mismatch or missing configuration between the Kotlin Gradle Plugin (KGP), Android Gradle Plugin (AGP), and Kotlin Symbol Processing (KSP). Specifically, the Kotlin Android plugin is not explicitly applied in the project, which KSP requires to correctly resolve variant artifacts.

## User Review Required

> [!IMPORTANT]
> The project is using very new/experimental versions of AGP (9.3.1) and Gradle (9.5.0). I will align the Kotlin and KSP versions to `2.0.2` as initially indicated by the KSP version string, and ensure the Kotlin plugin is correctly applied.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/gradle/libs.versions.toml)
- Add `kotlin = "2.0.2"` to the `[versions]` block.
- Add `kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }` to the `[plugins]` block.

#### [MODIFY] [build.gradle.kts (root)](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/build.gradle.kts)
- Apply the `kotlin-android` plugin in the `plugins` block with `apply false`.

#### [MODIFY] [app/build.gradle.kts](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/build.gradle.kts)
- Apply the `kotlin-android` plugin in the `plugins` block using `alias(libs.plugins.kotlin.android)`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build completes successfully.
- Run `./gradlew :app:kspDebugKotlin` specifically to ensure KSP tasks are working.

### Manual Verification
- Check if the IDE still reports any sync errors after the changes.
