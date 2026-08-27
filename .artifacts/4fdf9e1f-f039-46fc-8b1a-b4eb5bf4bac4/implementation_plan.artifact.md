# Implementation Plan - Netflix-style Class Selection Flow

Redesign the app entry flow to use a Netflix-style "profile selection" screen for choosing or creating classes.

## User Review Required

> [!IMPORTANT]
> The main menu will be bypassed, and the app will open directly into the "Select Class" screen. The "Quick Setup" and "Manage Classes" options will be accessible from this new flow or moved as needed.

## Proposed Changes

### Backend

#### [MODIFY] [MainActivity.kt](file:///C:/Users/fly/Documents/trabalho facul/StoodaScanner/app/src/main/java/com/example/stoodascanner/backend/MainActivity.kt)
- Change the `SplashScreen` completion callback to navigate to `AppState.SELECT_CLASS` instead of `AppState.MENU`.

#### [MODIFY] [MainViewModel.kt](file:///C:/Users/fly/Documents/trabalho facul/StoodaScanner/app/src/main/java/com/example/stoodascanner/backend/MainViewModel.kt)
- Update `handleBackPress` to return to `SELECT_CLASS` where appropriate, as it's now the home screen.

### Frontend

#### [MODIFY] [SelectClassScreen.kt](file:///C:/Users/fly/Documents/trabalho facul/StoodaScanner/app/src/main/java/com/example/stoodascanner/frontend/SelectClassScreen.kt)
- Redesign the UI to feature a dark background.
- Implement a grid of class "profiles".
- Each profile will show a colored box with the first letter of the class name and the name below.
- Add an "Add Class" profile at the end of the grid.
- Add a "Manage" button to enter management mode (deleting/editing classes).

#### [MODIFY] [ClassCreationScreen.kt](file:///C:/Users/fly/Documents/trabalho facul/StoodaScanner/app/src/main/java/com/example/stoodascanner/frontend/ClassCreationScreen.kt)
- (Optional) Adjust styling if needed to match the new dark theme, or keep it consistent with the existing app theme.

### Resources

#### [MODIFY] [strings.xml](file:///C:/Users/fly/Documents/trabalho facul/StoodaScanner/app/src/main/res/values/strings.xml)
- Add `whos_scanning` ("Who's scanning?") and `add_class` ("Add Class").

## Verification Plan

### Automated Tests
- N/A (UI-heavy change)

### Manual Verification
1. Launch the app and verify the Splash screen transitions to the new "Who's scanning?" screen.
2. Verify that existing classes are displayed in a grid.
3. Verify that tapping a class starts the scanning process.
4. Verify that tapping "Add Class" navigates to the class creation screen.
5. Verify that back navigation from other screens returns to the class selection screen.
