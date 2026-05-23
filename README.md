# AOSPStackingRecent

一个为 AOSP Launcher3 和 Pixel Launcher 带来 iOS 风格卡片堆叠多任务效果的 Xposed 模块。

## 功能特性
- **iOS 风格堆叠**：通过 Hook `RecentsView` 实现卡片在滚动时的重叠和缩放效果，模拟 iOS 的多任务视觉。

## 开发中
- [ ] **划卡动画修复**：正在优化任务卡片划掉时的位移衔接动画。

## 安装要求
- 已获取 Root 权限。
- 已安装 Xposed 框架（如 LSPosed）。
- 支持基于 Android 12+ 的 AOSP Launcher3 或 Pixel Launcher。

## 使用说明
1. 安装并激活模块。
2. 打开 **AOSPStackingRecent** 设置应用。
3. 调整滑块至满意的效果，设置会自动保存。
4. 如果效果没有立即生效，请强行停止 Pixel Launcher 或 Launcher3。

## 目标应用
- `com.google.android.apps.nexuslauncher` (Pixel Launcher)
- `com.android.launcher3` (Launcher3)


---
*由 Build_ 开发*
