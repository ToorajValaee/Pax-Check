# Fix "dal not initialized" in PaxSdkManager

Investigate and fix the issue where `PaxSdkManager` fails to acquire the `IDAL` instance. This involves adding more aggressive logging, exploring alternative initialization strategies, and improving error reporting.

## User Review Required

> [!IMPORTANT]
> The PAX SDK typically requires specific permissions and runs only on PAX hardware. I will add a check for PAX specific permissions in the code and suggest adding them to `AndroidManifest.xml` if needed.

## Proposed Changes

### PaxSdkManager

#### [MODIFY] [PaxSdkManager.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/sdk/PaxSdkManager.kt)
- Enhance `init()` and `getDal()` with detailed logging.
- Update `acquireDal()` to:
    - Log all methods of `NeptuneLiteUser` using reflection.
    - Log context information (package name, filesDir).
    - Try `getDalWithProcessSafe(context)` as an additional strategy.
    - Capture and log specific exceptions (e.g., "LOAD DAL ERR").
    - Update the `status` StateFlow with the specific error message if initialization fails.

## Verification Plan

### Automated Tests
- Run unit tests for `PaxSdkManager` (if any exist) to ensure no regressions in the logic.
- Note: Actual SDK initialization can only be verified on PAX hardware.

### Manual Verification
- Check logcat for the new aggressive logs to identify the exact point of failure.
- Verify that the UI displays the specific error message instead of a generic "Disconnected" or "dal not initialized".
