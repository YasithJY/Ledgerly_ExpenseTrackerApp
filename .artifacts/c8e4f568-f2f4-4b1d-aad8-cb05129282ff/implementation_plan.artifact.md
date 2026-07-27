# Fix Build Errors and Code Warnings

The project currently fails to build because `androidx.core:core-ktx:1.19.0` requires `compileSdk` 37, while the project is set to 36. Additionally, there are several minor code warnings and unused functions that need cleaning up.

## User Review Required

> [!IMPORTANT]
> I will update the `compileSdk` and `targetSdk` to 37. This is necessary for the current dependencies to work correctly.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/build.gradle.kts)
- Change `compileSdk` to 37.
- Change `targetSdk` to 37.

### Code Cleanup

#### [MODIFY] [AddTransactionActivity.kt](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/src/main/java/com/example/expensetrackerapp/AddTransactionActivity.kt)
- Fix formatting (missing commas).

#### [MODIFY] [AppDatabase.kt](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/src/main/java/com/example/expensetrackerapp/AppDatabase.kt)
- Fix formatting (missing commas).

#### [MODIFY] [Transaction.kt](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/src/main/java/com/example/expensetrackerapp/Transaction.kt)
- Fix formatting (missing commas).

#### [MODIFY] [TransactionAdapter.kt](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/src/main/java/com/example/expensetrackerapp/TransactionAdapter.kt)
- Use string resources or better formatting for currency display.
- Use `toColorInt()` for color parsing.

#### [MODIFY] [TransactionViewModel.kt](file:///D:/SLIIT/SLIIT%20Y4%20S2/MADD/Assignment_01_IT22136374/app/src/main/java/com/example/expensetrackerapp/TransactionViewModel.kt)
- Remove or mark unused functions (`update`, `delete`) if they are indeed not used. (I'll keep them but might suppress warnings if they are intended for future use, or just leave them if they don't block build).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the build passes with SDK 37.

### Manual Verification
- Verify the app runs on an emulator/device if possible.
