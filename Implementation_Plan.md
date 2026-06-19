# Final Master Plan: money2 投資組合管理應用程式

本文件是對過去所有討論的全面總結與最終評估，並已與 Google Finance 介面及舊版 Wealth-Manager 原始碼進行了功能與 API 的最終核對。本文件為 `money2` 開發的最高指導原則。

## 1. 產品願景與範圍 (Product Vision & Scope)
`money2` 的定位是一個**極致流暢、注重隱私的本地端投資組合管理工具**，專注於股票與 ETF 的資產追蹤，不包含瑣碎的日常收支記帳。
* **核心價值**：快速記帳、即時掌握淨值、無後端隱私保障。
* **目標平台**：全面瞄準 Android 17 (API 37)。

## 2. 功能比對：對齊 Google Finance
我們的核心體驗與 Google Finance 完美對齊：
* **Dashboard (總覽)**：對應 Google 的 Total value, Day's gain, Total gain, 與績效走勢圖。
* **Holdings (持股清單)**：對應 Google 的 "Your investments" 列表，顯示代號、股價、漲跌幅、持有股數、總價值、總報酬率。
* **Transactions (交易紀錄)**：對應 Google 的交易新增流程，我們以全局 FAB 搭配 Bottom Sheet 實作，確保操作絲滑不中斷。

## 3. 技術選型與底層架構 (Technical Stack)
遵守現代化 Android 開發的最佳實踐 (Clean Architecture 原則)：
* **UI 框架**：`Jetpack Compose` (淘汰 XML)。
* **視覺規範**：`Material Design 3` - 強制啟用動態色彩 (Dynamic Color) 與色調高度 (Tonal Elevation)。
* **依賴注入**：`Koin` - 輕量且完美整合 Compose。
* **導航機制**：`Compose Type-Safe Navigation` - 基於 `kotlinx.serialization` 的最新官方安全導航標準。
* **資料處理**：`Kotlin Coroutines` + `Flow` (實現全響應式 UI)。
* **本機資料庫**：`Room Database`。
* **網路連線**：`Retrofit` + `OkHttp`。
* **資料安全**：`EncryptedSharedPreferences` (用於儲存 API Key 等敏感資料)。

## 4. 外部 API 與安全性 (APIs & Security)
已確認沿用舊版 `Wealth-Manager` 的 API 實作細節：
* **FinnhubApi (美股)**：
  * 端點: `/finnhub/quote` (報價), `/finnhub/search` (搜尋)。
  * 限制: 60 requests/minute (必須實作本地快取避免超量)。
* **TwseApi (台股)**：
  * 端點: `/twse/v1/exchangeReport/STOCK_DAY_ALL`。
  * 特性: 無次數限制，適合一次性拉取所有台股當日價格作為快取。
* **ExchangeRateApi (匯率)**：
  * 端點: `/exchangerate/latest/{baseCurrency}`。
  * 功能: 將美金 (USD) 持股換算為台幣 (TWD) 總資產。
* **BYOK (自帶金鑰) 與隱私保護**：
  * 使用者在「設定 (Settings)」頁面填寫 Finnhub API Key。
  * 該 Key 會透過 Android `EncryptedSharedPreferences` 加密儲存，無後端伺服器介入。

## 5. 測試與品質保證 (Testing Strategy)
* **單元測試 (Unit Tests)**：針對 Domain Layer (核心業務邏輯) 撰寫完整的 JUnit 測試，確保 `AssetCalculationUseCase` (資產計算) 與 `ExchangeRateUseCase` (匯率換算) 精準無誤。

## 6. 執行階段狀態 (Execution Phases Status)
* [x] **Phase 1: 基礎建設 (Infrastructure)** - 設定 Koin, Room, Retrofit, Kotlinx Serialization 等依賴與 Gradle 版本目錄。
* [x] **Phase 2: 資料與領域層 (Data & Domain)** - 設計 Room 資料表，實作 API 串接與加密儲存，撰寫 UseCases 與單元測試。
* [x] **Phase 3: UI 與使用者體驗 (Presentation & UX)** - 實作 M3 主題與 Type-Safe 導航，開發 Dashboard, Holdings, Transactions 等 Compose 頁面。
* [x] **Phase 4: 整合與優化 (Integration & Polish)** - ViewModel 狀態串接、效能優化 (非同步並行拉取報價)、安全性升級 (移除硬編碼密鑰、關閉正式環境 HTTP 敏感日誌) 與最終測試 (DashboardStatsUseCase)。
