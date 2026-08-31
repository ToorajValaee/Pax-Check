- [x] Create `app/libs` directory and placeholder file for NeptuneLite SDK JAR.
- [x] Configure `app/build.gradle.kts` to include local JAR dependencies.
- [x] Implement `PaxSdkManager` for SDK binding and `IDal` initialization.
- [x] Initialize `PaxSdkManager` in `MainActivity`.
- [x] Provide SDK stubs to ensure the project builds successfully without the JAR.
- [x] Verify build (Success).

### Task 2: Hardware Service Layer
- [x] Implement `HardwareService` interface.
- [x] Implement `PaxHardwareService` using Coroutines for non-blocking I/O.
- [x] Implement MSR reading logic (open, reset, read, close).
- [x] Implement Printer logic (init, printStr, start).
- [x] Add MockK dependency for unit testing.
- [x] Create unit tests for `PaxHardwareService`.
- [x] Verify unit tests pass (Success).
