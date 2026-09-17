# StoodaScanner 🎓📱

StoodaScanner is a high-performance Android application designed for educators to conduct quick quizzes and track student performance in real-time. By leveraging modern mobile technologies, it transforms a smartphone into a powerful classroom response system.

## 🚀 Key Features

*   **Modern UI/UX**: Built entirely with **Jetpack Compose** and **Material 3**, providing a clean, fluid, and intuitive interface.
*   **Intelligent Class Management**:
    *   **Class Selection**: Easily switch between different classes and groups.
    *   **Importing**: Support for importing student lists directly from **CSV** or **XLSX** spreadsheets.
    *   **Custom Creation**: A dedicated workflow for manually building classes or adjusting imported data.
*   **High-Performance Bulk Scanning**: 
    *   Powered by **ZXing-C++** for lightning-fast detection of multiple QR codes in a single frame.
    *   Real-time feedback via **Compose Canvas** overlays, showing interactive checkmarks where codes are detected.
*   **Automatic Validation**: Implements a robust 4-digit protocol with a built-in checksum to ensure zero-error data collection.
*   **Data Visualization**: Instant generation of distribution graphs to analyze classroom performance across multiple answer choices (A-E).
*   **Standardized PDF Generation**: Built-in utility to generate and print ready-to-use QR code sheets for students.

## 🛠 Tech Stack & Architecture

### Core Technologies
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Navigation**: Compose Navigation with a centralized `AppState` machine.
*   **Camera API**: CameraX with optimized `ImageAnalysis` pipelines.
*   **Native Engine**: ZXing-C++ (via JNI) for peak processing performance.
*   **Data Layer**: JSON-based local persistence for class and student management.

### The Stooda Protocol (4-Digit System)
Each QR code represents a unique combination of student and answer:
1.  **Digits 1-2**: Student Identifier (Index 00-63).
2.  **Digit 3**: Answer Choice (0=A, 1=B, 2=C, 3=D, 4=E, 5=?).
3.  **Digit 4**: Modulo-10 Checksum of the first three digits for transmission integrity.

## 📋 Architecture Highlights
*   **Unidirectional Data Flow**: State is managed in `MainViewModel`, ensuring a single source of truth for navigation and scan results.
*   **Coordinate Transformation**: Precise mapping between CameraX buffer coordinates and screen space for accurate overlay positioning, handling all device orientations.
*   **Resource Efficiency**: Strict lifecycle management of `ImageProxy` and camera resources to prevent memory leaks and ensure sustained performance.

## ⚙️ Setup & Installation

1.  **Prerequisites**:
    *   Android Studio Ladybug (2024.2.1) or newer.
    *   Android SDK 26 (Android 8.0) or higher.
    *   Physical device recommended for optimal CameraX performance.

2.  **Deployment**:
    *   Clone the repository and sync Gradle.
    *   The app requires `CAMERA` and `WRITE_EXTERNAL_STORAGE` (on older APIs) permissions, requested at runtime.

## 📖 Standard Workflow

1.  **Create**: Go to the "New Class" section and choose between importing a spreadsheet or entering names manually.
2.  **Print**: Generate the QR PDF and distribute the sheets to your students.
3.  **Session**: Select the class from the main grid and tap to start scanning.
4.  **Capture**: Point the camera at the classroom. Green checkmarks will confirm each unique capture.
5.  **Review**: Analyze the distribution of answers in the results screen or the visual graph view.

---
*Developed for educators who value speed, accuracy, and modern design in the classroom.*
