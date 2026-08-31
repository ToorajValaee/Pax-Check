# Implementation Plan - Task 2: Hardware Service Layer

This task involves creating a robust hardware interaction layer that wraps PAX NeptuneLite SDK calls for MSR (card reading) and Thermal Printer functionality. The layer will provide a clean API for the UI to interact with, ensuring all hardware operations are non-blocking and use Kotlin Coroutines.

## User Review Required

> [!IMPORTANT]
> The current implementation of `PaxHardwareService` and `HardwareService` already exists in the project. This plan focuses on refining these implementations, ensuring they adhere to the acceptance criteria, and adding unit tests for verification.

## Proposed Changes

### Hardware Service Layer

#### [MODIFY] [PaxHardwareService.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/hardware/PaxHardwareService.kt)
- Refine error handling to provide more detailed logs.
- Ensure all hardware-related exceptions are caught and handled gracefully.
- Verify that `Dispatchers.IO` is used for all blocking SDK calls.

#### [NEW] [PaxHardwareServiceTest.kt](file:///D:/Source/Repos/PaxCheck/app/src/test/java/com/example/paxcheck/hardware/PaxHardwareServiceTest.kt)
- Implement unit tests for `PaxHardwareService` using Mockito or MockK (depending on project setup).
- Test successful MSR reading and error scenarios.
- Test successful printing and error scenarios.

## Verification Plan

### Automated Tests
- Run unit tests: `./gradlew :app:testDebugUnitTest --tests "com.example.paxcheck.hardware.PaxHardwareServiceTest"`

### Manual Verification
- Build the app to ensure no compilation errors: `./gradlew :app:assembleDebug`
- Observe logs during execution (if a device were available) to verify asynchronous execution.
