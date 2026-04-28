# StoodaScanner 🎓📱

StoodaScanner is a specialized Android application designed for teachers to conduct quick quizzes and track student performance in real-time. By leveraging the device's camera to scan custom QR codes, teachers can instantly collect answers from an entire classroom.

## 🚀 Key Features

*   **Bulk Scanning:** High-performance QR code analysis using ZXing-C++ for rapid detection of multiple codes.
*   **Real-time Feedback:** A custom UI overlay (`ScanOverlay`) draws semi-transparent checkmarks on the viewfinder to confirm each student's answer has been captured.
*   **Automatic Validation:** Built-in 4-digit protocol with a custom checksum verification to ensure scan accuracy.
*   **Result Visualization:** Instant generation of distribution graphs (`ResultGraphView`) to analyze classroom performance (Answers A through E).
*   **PDF Generation:** Built-in utility to generate and print the standardized QR code sheets for students.
*   **Haptic Feedback:** Vibrates on unique successful scans to allow teachers to focus on the students, not the screen.

## 🛠 Architecture & Implementation

### Core Components
*   **`QRCodeAnalyzer`**: An `ImageAnalysis.Analyzer` implementation that processes camera frames. It uses `zxingcpp` for high-speed multi-code detection and calculates the geometric center of each QR code for overlay positioning.
*   **`ScanOverlay`**: A custom `View` that manages the temporal rendering of detection markers. It uses a `postInvalidate` loop to handle the 200ms fade/removal logic.
*   **`QRDecoder`**: Implements the proprietary 4-digit mapping:
    *   **Digits 1-2**: Student ID (01-64).
    *   **Digit 3**: Answer Choice (A, B, C, D, E, or ?).
    *   **Digit 4**: Modulo-10 Checksum of the first three digits.
*   **`QRGenerator`**: Handles the creation of standardized PDF sheets for classroom distribution.

### Tech Stack
*   **Language**: Kotlin
*   **Camera API**: CameraX
*   **Processing Engine**: ZXing-C++ (via `zxing-cpp-android`)
*   **UI**: Material Design & Custom Canvas Drawing

## 📋 Best Practices Implemented
*   **Backpressure Handling**: Uses `STRATEGY_KEEP_ONLY_LATEST` in CameraX to ensure the UI remains responsive even during heavy processing.
*   **Geometric Scaling**: Implements precise coordinate transformation between the `ImageAnalysis` buffer resolution and the physical screen `PreviewView` dimensions, accounting for portrait/landscape orientation.
*   **Concurrency**: Separates image analysis (background thread) from UI updates (main thread) using `runOnUiThread`.
*   **Resource Management**: Strict adherence to `ImageProxy` closing lifecycle to prevent memory leaks and camera freezes.

## ⚙️ Compilation & Setup

1.  **Prerequisites**:
    *   Android Studio Hedgehog (2023.1.1) or newer.
    *   Android SDK 23 (Android 6.0) or higher.
    *   Physical Android device (CameraX performance is limited on emulators).

2.  **Steps**:
    *   Clone the repository.
    *   Open the project in Android Studio.
    *   Wait for **Gradle Sync** to finish.
    *   Connect your device via USB or Wi-Fi Debugging.
    *   Click **Run 'app'** (Shift + F10).

3.  **Permissions**:
    *   The app will request `Manifest.permission.CAMERA` on the first launch.

## 📖 How to Use

1.  **Prepare**: Use the "Generate QR PDF" button to create the student cards.
2.  **Configure**: Enter the number of students/answers expected in the current session.
3.  **Scan**: Point the camera at the students' QR cards. Watch for the green circles indicating successful capture.
4.  **Analyze**: Once the target count is reached, review the list of results or tap "Show Visual Graph" to see the answer distribution.

---
*Developed for educators who value speed and efficiency in the classroom.*
