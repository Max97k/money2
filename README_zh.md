<div align="center">

# Money2

**現代化、隱私優先且高效能的 Android 投資組合管理工具。**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Android Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

[English](README.md) | [繁體中文](README_zh.md)

</div>

---

Money2 是一款開源的 Android 應用程式，旨在提供流暢、高效能且完全安全的體驗，協助您追蹤股票與 ETF 投資組合。其設計靈感來自頂尖的金融應用程式，並以現代 Android 架構與嚴格的「本地端優先」理念打造。

## ✨ 為什麼選擇 Money2？

* **絕對的隱私安全**：零後端伺服器、零雲端同步。您所有的財務資料與 API 金鑰都完全儲存於您的設備上，並透過 Android 硬體級的 Keystore 搭配 `EncryptedSharedPreferences` 進行加密保護。
* **極致流暢**：基於 Jetpack Compose 與 Kotlin Coroutines 原生開發。即時的市場價格資料全數採用非同步平行拉取，確保畫面絕不卡頓。
* **現代化 UI/UX**：嚴格遵循 Material Design 3 規範，支援動態色彩 (Dynamic Color)，提供優美流暢的原生 Android 體驗。
* **無縫多幣別支援**：完美處理跨國投資組合（例如美股與台股），自動且即時計算並換算匯率，總資產一目瞭然。

## 🛠 技術棧與系統架構

Money2 遵循**無瑕架構 (Clean Architecture)** 準則，嚴格分離 Data、Domain 與 Presentation 層。

* **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **架構模式**: MVI (Model-View-Intent) & Clean Architecture
* **依賴注入 (DI)**: [Koin](https://insert-koin.io/)
* **非同步處理**: Kotlin Coroutines & Flow
* **本地資料庫**: [Room](https://developer.android.com/training/data-storage/room)
* **網路請求**: Retrofit2 + OkHttp3
* **安全性**: Jetpack Security Crypto (`EncryptedSharedPreferences`)

## 🚀 快速開始

### 環境需求
* [Android Studio Koala](https://developer.android.com/studio) (或更新版本)
* JDK 17
* Android SDK (最低支援 API 26 / 目標 API 37)

### 安裝步驟
1. 複製此儲存庫：
   ```bash
   git clone https://github.com/Max97k/money2.git
   cd money2
   ```
2. 使用 Android Studio 開啟專案。
3. 設定本地端 Proxy 密鑰（若您有架設自己的 Worker）。請在您的 `local.properties` 檔案中加入以下內容：
   ```properties
   PROXY_SECRET=your_secret_here
   ```
4. 編譯並將應用程式安裝至您的模擬器或實體 Android 設備。

## 🔒 資訊安全模型

我們極度重視資料保護：
* **零遙測 (No Telemetry)**：專案中不包含任何第三方的追蹤器或資料收集 SDK。
* **憑證管理**：所有敏感的 API 金鑰都嚴格限制在本地的 `local.properties` 中，並透過 `BuildConfig` 於編譯期安全注入。
* **資料加密**：所有本地設定與敏感資訊均會被強制加密。

如需更詳盡的說明，請參閱我們的[隱私權政策 (Privacy Policy)](PRIVACY.md)。

## 📖 開發文件

深入了解我們的系統架構與開發規範：
- [📝 實作計畫 (Implementation Plan)](Implementation_Plan.md) - 核心架構設計與開發階段。
- [🤝 貢獻指南 (Contributing Guidelines)](CONTRIBUTING.md) - 如何提交 Issues 與 Pull Requests。
- [🔖 第三方聲明 (Third-Party Notices)](THIRD_PARTY_NOTICES.md) - 致謝名單。

## 🤝 參與貢獻

我們非常歡迎來自社群的貢獻！無論是回報 Bug、提出新功能建議，還是進行程式碼重構：
1. Fork 此專案。
2. 建立您的功能分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送至您的分支 (`git push origin feature/AmazingFeature`)。
5. 發起 Pull Request。

請確保您的 PR 符合我們的[行為準則 (Code of Conduct)](CONTRIBUTING.md)。

## 📄 授權條款

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 檔案。

---
<div align="center">
  <sub>Built with ❤️ for the open-source community.</sub>
</div>
