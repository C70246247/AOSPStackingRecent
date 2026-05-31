# AOSPStackingRecent

A Xposed module that brings the elegant iOS-style card stacking recents (multi-tasking) effect to AOSP Launcher3 and Pixel Launcher.

## 📱 Preview
![Preview](Preview/preview.gif)

## ✨ Features
- **iOS-style Stacking**: Implements realistic card overlapping and scaling effects by hooking into `RecentsView`.

## 🛠 Requirements
- **LSPosed Framework**: The modern Xposed implementation.
- **Android 12+**: Supports Android 12-16.
- **Target Launchers**: 
  - `com.android.launcher3` (AOSP Launcher3)
  - `com.google.android.apps.nexuslauncher` (Pixel Launcher)

## 🚀 Installation
1. Install the module from the [releases page](https://github.com/BuildSession/AOSPStackingRecent/releases).
2. Open **LSPosed Manager** and enable the module.
3. Ensure the scope includes **Launcher3** or **Pixel Launcher**.
4. Reboot your phone or **Force Stop** the launcher to apply changes.

## TODO List
- Fix Animation Bug

## 🤝 Contribution
This project is open-source. Feel free to submit Pull Requests or Issues if you find bugs or have feature suggestions.

## Special Thanks
- [AOSP](https://source.android.com/)
- [LSPosed](https://github.com/LSPosed/LSPosed)

---
*Developed with by BuildSession*
