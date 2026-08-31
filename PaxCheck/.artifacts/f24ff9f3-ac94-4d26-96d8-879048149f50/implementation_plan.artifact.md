# Implementation Plan - Task 3: Compose UI and Navigation

Implement the core UI screens, navigation, and shared logging for the Pax Check application.

## Proposed Changes

### [sdk]

#### [MODIFY] [PaxSdkManager.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/sdk/PaxSdkManager.kt)
- Add a `StateFlow<String>` to expose the initialization status of the SDK.
- Update `init()` to set the status (e.g., "Initializing", "Connected", "Error").

### [ui]

#### [MODIFY] [HardwareViewModel.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/hardware/HardwareViewModel.kt)
- Add `logMessages: StateFlow<List<String>>` to store test logs.
- Update `readMsr()` and `printTest()` to append messages to the log.
- Add `sdkStatus: StateFlow<String>` by observing `PaxSdkManager`.

#### [NEW] [NavRoutes.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/navigation/NavRoutes.kt)
- Define `@Serializable` classes for routes: `Dashboard`, `MsrTest`, `PrinterTest`.

#### [NEW] [DashboardScreen.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/screens/DashboardScreen.kt)
- Display SDK status.
- Navigation buttons to MSR and Printer tests.
- Display a summary of the latest logs.

#### [NEW] [MsrScreen.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/screens/MsrScreen.kt)
- UI for triggering MSR read.
- Display captured track data.
- Shared log view.

#### [NEW] [PrinterScreen.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/screens/PrinterScreen.kt)
- Text field for user input.
- "Print" button.
- Shared log view.

#### [NEW] [SharedLogView.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/ui/components/SharedLogView.kt)
- A reusable scrollable log component.

#### [MODIFY] [MainActivity.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/MainActivity.kt)
- Set up Navigation 3 using `NavBackStack` and `NavDisplay`.
- Wire up the ViewModel.

## Verification Plan

### Automated Tests
- N/A for this task (focus on UI/Navigation).

### Manual Verification
- Launch the app and verify:
    - SDK status shows "Connected" on the Dashboard.
    - Navigating to MSR screen works.
    - Navigating to Printer screen works.
    - Printing text adds an entry to the log and shows "Print Success".
    - Swiping a card (if simulated/real) shows data and log entry.
    - The log is visible across screens.
