# Implementation Plan - Enhanced Logging and Reliability for PAX Hardware

Improve the stability and observability of MSR reading and printing on PAX A8900 devices by adding detailed logging and refining the SDK interaction logic.

## Proposed Changes

### [SDK Manager]

#### [MODIFY] [PaxSdkManager.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/sdk/PaxSdkManager.kt)
- Update `getDal()` to re-acquire the `IDAL` instance from `NeptuneLiteUser` to ensure the binder is valid, especially after app resume or potential service disconnection.

### [Hardware Service]

#### [MODIFY] [PaxHardwareService.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/hardware/PaxHardwareService.kt)
- **MSR Improvements:**
    - Add logs for `mag.open()` and `mag.reset()`.
    - Add periodic logging within the polling loop (every 5 seconds) to show activity.
    - Explicitly log all three tracks (`track1`, `track2`, `track3`) even if null or empty.
    - Keep `mag.reset()` outside the loop as per standard practice, but log its result if applicable.
- **Printer Improvements:**
    - Log `printer.init()` and `printer.start()`.
    - Translate `printer.start()` return codes into human-readable error messages in logs (e.g., "Out of paper", "Overheat").
    - Log the step where text is added to the buffer.

## Verification Plan

### Manual Verification
- Deploy the app to a PAX A8900 device.
- Perform a card swipe and monitor Logcat for:
    - "MSR: Opening Mag..."
    - "MSR: Resetting Mag..."
    - "MSR: Still waiting for swipe..."
    - "MSR: Swipe detected!"
    - Full track data logs.
- Perform a print operation and monitor Logcat for:
    - "Printer: Initializing..."
    - "Printer: Start returned: 0 (Success)" or specific error codes.
