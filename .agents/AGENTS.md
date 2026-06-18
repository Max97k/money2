# Project-Scoped Rules

## Core Android Development Rules
請記住這條核心開發規則，並套用於之後所有的 Android 任務：

1. 框架與技術：一律使用 Jetpack Compose，絕對不使用舊版 XML 佈局。
2. 設計美學 (Material You)：嚴格遵循 Material Design 3 (m3.material.io) 的視覺哲學。
3. 視覺實作標準：
   - 啟用動態色彩 (Dynamic Color)。
   - 使用色調高度 (Tonal Elevation) 來區分 UI 層級，避免使用過時的重度陰影。
   - 保持介面簡潔、充裕留白，元件使用 M3 規範的圓角形狀。
   - UI 元件請直接調用 Compose Material 3 函式庫中的組件 (如 Card, Scaffold, TopAppBar)。

遇到任何設計決策，請優先對齊 Google 官方的 M3 美學標準。

4. 目標 SDK 與 API：接下來的開發全面瞄準 Android 17，實作時請盡量優先採用 Android 17 (API 37) 官方所推薦的做法與架構。
