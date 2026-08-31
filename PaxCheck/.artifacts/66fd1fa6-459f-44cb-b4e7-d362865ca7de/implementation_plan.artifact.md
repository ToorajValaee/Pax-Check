# Implementation Plan - Task 8: Error Mapping and Run Verify

The goal is to enhance error reporting for hardware operations (MSR and Printer) by capturing descriptive error messages from `PaxHardwareService` and displaying them in the UI logs via `HardwareViewModel`.

## Proposed Changes

### Hardware Service Layer

#### [MODIFY] [HardwareService.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/hardware/HardwareService.kt)
- Introduce `HardwareResult<T>` sealed class to represent success or failure with a message.
- Update `readMsr()` to return `HardwareResult<CardData>`.
- Update `printText()` to return `HardwareResult<Unit>`.

#### [MODIFY] [PaxHardwareService.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/hardware/PaxHardwareService.kt)
- Update implementation to return `HardwareResult.Success` or `HardwareResult.Error`.
- Ensure `getPrinterErrorMessage(result)` is used to provide detailed error messages in `HardwareResult.Error`.
- Capture exceptions and return them as `HardwareResult.Error`.

### ViewModel Layer

#### [MODIFY] [HardwareViewModel.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/hardware/HardwareViewModel.kt)
- Update `readMsr()` and `printTest()` to handle `HardwareResult`.
- Update `addLog()` calls to include specific error messages (e.g., "Printer Error: [Message]").

### Testing

#### [MODIFY] [PaxHardwareServiceTest.kt](file:///D:/Source/Repos/PaxCheck/app/src/test/java/com/example/paxcheck/hardware/PaxHardwareServiceTest.kt)
- Update test cases to assert on `HardwareResult.Success` or `HardwareResult.Error`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:testDebugUnitTest --tests com.example.paxcheck.hardware.PaxHardwareServiceTest`

### Manual Verification
- Perform a build check: `./gradlew :app:assembleDebug`
