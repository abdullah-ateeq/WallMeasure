# WallMeasure 📐🧱

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20Material3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![MinSDK](https://img.shields.io/badge/MinSDK-26%20%28Android%208.0%29-orange.svg)](https://developer.android.com/about/dashboards)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-37-blue.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**WallMeasure** is a modern, intuitive Android application designed for builders, contractors, interior designers, painters, and DIY enthusiasts to quickly measure wall surface areas, account for doors and window cutouts, apply material waste allowances, and export accurate project estimates.

---

## 🌟 Features

- 📏 **Accurate Wall Area Calculation**:
  - **Single Wall Mode**: Calculate surface area using customizable width and height dimensions.
  - **Multiple Walls / Room Mode**: Calculate 4-wall rectangular room surface areas seamlessly (`2 × (Length + Width) × Height`).
  - **Variable Wall Heights**: Supports uniform height, sloped/pitch roof walls, and custom height profiles.

- 🚪 **Door & Window Cutouts (Openings)**:
  - Add doors, windows, and custom cutouts with custom quantities and dimensions.
  - Automatically deduct opening areas to compute exact **Net Area** from **Gross Area**.

- 🎨 **Waste & Allowance Percentage**:
  - Configurable material allowance (0%, 5%, 10%, 15%, 20%, or custom %) for painting, tiling, wallpapering, or drywall installation.

- 🔄 **Multi-Unit Support & Real-time Conversion**:
  - **Input Units**: Millimeters (mm), Centimeters (cm), Meters (m), Inches (in), Feet (ft).
  - **Output Area Units**: Square Meters (m²), Square Feet (sq ft), Square Centimeters (cm²), Square Inches (sq in).
  - Integrated live **Dimension Converter** tool.

- 📊 **Visual Wall Diagram**:
  - Real-time canvas representation displaying wall dimensions and cutouts dynamically scaled on screen.

- 📂 **History Management**:
  - Save project measurements with custom titles and timestamps.
  - View, search, filter, restore, or delete previous calculations.

- 📤 **Export & Sharing Options**:
  - Export calculation summaries as **TXT**, **CSV**, or **JSON** files.
  - Built-in Android Share intent integration to send reports via Email, Messaging, or Drive.

- 🎨 **Modern Material 3 Design**:
  - Built with 100% **Jetpack Compose**.
  - Edge-to-Edge interface support with **Material You Dynamic Colors** and Dark/Light mode toggles.

---

## 🛠️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design system
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin `StateFlow` and unidirectional data flow
- **Navigation**: Navigation Compose
- **Asynchronous Execution**: Kotlin Coroutines
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) with Android Gradle Plugin (AGP) `9.4.1` & Kotlin `2.2.10`

---

## 📁 Project Structure

```text
WallMeasure/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/wallmeasure/
│       │   │   ├── MainActivity.kt               # Entry point & Navigation host
│       │   │   ├── calculator/                  # Wall & Unit calculation math logic
│       │   │   ├── data/                        # History storage & Export helpers
│       │   │   ├── model/                       # Data classes (Wall, Opening, Settings)
│       │   │   ├── navigation/                  # Screen routes & navigation targets
│       │   │   ├── ui/                          # Composable screens & reusable components
│       │   │   │   ├── components/              # Wall Diagram, Cards, Dialogs
│       │   │   │   ├── screens/                 # Home, History, Openings, Settings, About
│       │   │   │   └── theme/                   # Material 3 Color, Type, Theme
│       │   │   └── viewmodel/                   # WallMeasureViewModel & State representation
│       │   └── res/                             # Resources & App Icons
│       └── test/                                # Unit tests for Calculator & History
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / 2024.2+ or higher
- **JDK**: Java 11 or higher
- **Android Device / Emulator**: Running Android 8.0 (API Level 26) or newer

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/WallMeasure.git
   cd WallMeasure
   ```

2. **Open in Android Studio**:
   - Open Android Studio and select **Open**.
   - Navigate to the `WallMeasure` directory.
   - Allow Gradle to sync dependencies.

3. **Run on Device / Emulator**:
   - Connect your Android device or start an emulator.
   - Click **Run** (`Shift + F10`) or execute from terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 🧪 Testing

Run unit tests using Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Abdullah Ateeq**
- GitHub: [@abdullah-ateeq](https://github.com/abdullah-ateeq)
