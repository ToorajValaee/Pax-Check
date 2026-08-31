# Fix PAX Hardware Integration (MSR and Printer)

The current implementation fails because of incorrect stubs and mismatched method signatures with the actual NeptuneLite SDK JAR.

## User Review Required

> [!IMPORTANT]
> The stubs in `com.pax` will be removed as they are shadowing the real SDK classes in the JAR file.

## Proposed Changes

### [Component Name] SDK and Hardware Integration

#### [DELETE] [IDal.java](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/dal/IDal.java)
#### [DELETE] [IMsr.java](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/dal/IMsr.java)
#### [DELETE] [IPrinter.java](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/dal/IPrinter.java)
#### [DELETE] [INeptuneLiteApi.java](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/neptunelite/api/INeptuneLiteApi.java)
#### [DELETE] [NeptuneLiteUser.java](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/pax/neptunelite/api/NeptuneLiteUser.java)

#### [MODIFY] [PaxSdkManager.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/sdk/PaxSdkManager.kt)
- Change `IDal` to `IDAL`.
- Update `init()` to use `NeptuneLiteUser.getInstance().getDal(context)` directly.
- Remove references to `INeptuneLiteApi` as it's not present in the JAR.

#### [MODIFY] [PaxHardwareService.kt](file:///D:/Source/Repos/PaxCheck/app/src/main/java/com/example/paxcheck/hardware/PaxHardwareService.kt)
- Change `IDal` to `IDAL`.
- Change `msr` to `mag` and use `getMag()` instead of `getMsr()`.
- Update `readMsr()` to handle `TrackData`.
- Add a simple loop/timeout to wait for a card swipe in `readMsr()` using `mag.isSwiped()`.
- Improve `printText()` by setting a default font.

## Verification Plan

### Automated Tests
- Run build to ensure no compilation errors after removing stubs and updating code.
- `./gradlew :app:assembleDebug`

### Manual Verification
- Deploy to a PAX device and test the MSR read and printing functions.
