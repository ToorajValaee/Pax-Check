# Fix PaxSdkManager.kt NoSuchFieldError Crash

The app crashes during PAX SDK initialization with a `NoSuchFieldError`. This is likely due to binary incompatibility between the Kotlin stubs provided in the source and the actual NeptuneLite SDK (Java-based) at runtime, specifically regarding the `Companion` object of the `NeptuneLiteUser` stub.

## Proposed Changes

### [Component Name] SDK Management

#### [MODIFY] [PaxSdkManager.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/sdk/PaxSdkManager.kt)
- Update the `init()` method to catch `Throwable` instead of `Exception` to handle `LinkageError`s like `NoSuchFieldError`.
- Improve error handling to update the status flow gracefully.
- Add an alternative check for `getDal(context)` directly on `NeptuneLiteUser.getInstance()` as it's a common pattern in different SDK versions.

#### [MODIFY] [NeptuneLiteUser.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/neptunelite/api/NeptuneLiteUser.kt) (Convert to Java)
- Convert `NeptuneLiteUser` stub from Kotlin to Java to ensure binary compatibility with the expected Java static method `getInstance()`, avoiding the `Companion` object field.
- Add `getDal(context)` method to the stub to allow compilation of the alternative initialization path.

#### [MODIFY] [INeptuneLiteApi.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/neptunelite/api/INeptuneLiteApi.kt) (Convert to Java)
- Convert to Java for consistency and better interoperability with the SDK JAR.

#### [MODIFY] [IDal.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/dal/IDal.kt) (Convert to Java)
- Convert to Java for consistency.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still compiles.

### Manual Verification
- The crash should no longer occur on launch.
- If running on an emulator, the status should transition to "Error: ..." instead of crashing.
