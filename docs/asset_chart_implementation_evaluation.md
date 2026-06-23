# 資產走勢圖 (AssetTrendChart) 增強實作方案評估

本報告針對 `money2` 專案中的資產走勢圖（`AssetTrendChart`）提出增強實作方案的技術設計與評估，旨在完善時間範圍切換、互動式游標提示、以及大盤指數對照比較等核心功能。

## 1. 資料流與架構設計

為支援動態時間範圍（1D, 5D, 1M, 6M, YTD, 1Y, 5Y, MAX），目前的 `DashboardViewModel` 需要進行重構，以支援不同粒度（如即時分K、日K、週K）的歷史資料撈取與緩存。

### 架構調整方向

1.  **狀態模型設計 (UI State)**:
    在 `DashboardViewModel` 中引入一個明確的 `ChartUiState` 資料類別，包含當前選擇的時間區間、資產趨勢點、大盤指數點（可選）、以及圖表載入狀態。
    ```kotlin
    enum class TimeRange {
        DAY_1, DAY_5, MONTH_1, MONTH_6, YTD, YEAR_1, YEAR_5, MAX
    }

    data class ChartUiState(
        val selectedRange: TimeRange = TimeRange.YEAR_1,
        val assetPoints: List<ChartPoint> = emptyList(),
        val benchmarkPoints: List<ChartPoint>? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class ChartPoint(val timestamp: Long, val value: Float, val normalizedValue: Float = 0f)
    ```

2.  **歷史資料請求粒度 (Granularity)**:
    根據 `TimeRange` 動態調整向 `MarketRepositoryImpl` 發送的請求參數。例如：
    *   **1D/5D**: 需要分K資料（如 5m, 15m interval）。
    *   **1M/6M/YTD/1Y**: 需要日K資料（1d interval）。
    *   **5Y/MAX**: 考慮使用週K或月K資料（1wk, 1mo interval）以減少資料量並提升繪製效能。

3.  **快取機制優化**:
    目前的 `historicalDataCache` 是儲存了 2Y 的 1d 資料。未來需將 Cache 結構升級，支援 `(Symbol, Interval) -> List<Pair<Long, Double>>`。可以利用 Room 資料庫進行持久化快取，減少每次啟動時的網路請求。針對分K資料，由於時效性高，可保留在 Memory Cache (如 `MutableStateFlow`)。

4.  **非同步計算與協程 (Coroutines)**:
    資產總值計算（整合多支股票在各個時間點的持倉數量與歷史價格）計算量龐大。應該利用 `Dispatchers.Default` 進行運算，並且符合專案中對於平行處理的規範，採用 `async { ... }.awaitAll()` 來加速多檔持股的資料整合計算。

## 2. Canvas 互動游標實作方案

為了實作互動式游標與提示框（Tooltip），我們需要在 Jetpack Compose 的 `AssetTrendChart` 中引入觸控手勢的支援，並計算最接近的數據點。

### 實作步驟

1.  **狀態管理 (Gesture State)**:
    在 `AssetTrendChart` 元件內使用 `remember` 儲存當前使用者的觸控 X 座標或計算出的最近資料點索引。
    ```kotlin
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    ```

2.  **指標輸入 (Pointer Input)**:
    使用 `Modifier.pointerInput` 結合 `detectDragGestures` 和 `detectTapGestures`。
    ```kotlin
    Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset -> /* 計算 selectedIndex */ },
            onDrag = { change, _ ->
                val x = change.position.x
                /* 根據 X 座標找出最近的資料點更新 selectedIndex */
            },
            onDragEnd = { selectedIndex = null },
            onDragCancel = { selectedIndex = null }
        )
    }
    ```

3.  **座標映射與尋找最近點**:
    已知 Canvas 的寬度與資料點總數，可輕易將觸控的 X 座標反推回最近的資料點索引 `index = (x / pointSpacing).roundToInt().coerceIn(0, points.lastIndex)`。

4.  **繪製十字線與提示框**:
    當 `selectedIndex` 不為 null 時：
    *   **垂直線/十字線**: `drawLine` 繪製一條貫穿選定 X 座標的垂直線，甚至加上水平十字對準線。
    *   **焦點圓點**: `drawCircle` 在對應的 (X, Y) 座標繪製一個高亮圓點。
    *   **動態提示框 (Tooltip)**: 可以在 Canvas 內使用 `drawText` (搭配 TextMeasurer) 繪製，或是利用 Box 疊加一個 Compose 的 Card 元件在 Canvas 之上（根據 X, Y 座標做 Offset）。使用 Compose 元件可以更容易實作複雜的排版（例如同時顯示日期、資產總值與百分比變化）。

## 3. 大盤指數對照與歸一化

為了在同一個圖表上直觀比較資產表現與大盤（如 S&P 500 `^GSPC` 或台股加權指數 `^TWII`），必須將兩組不同量級的數據歸一化為百分比。

### 實作步驟

1.  **撈取標竿指數**:
    在 `DashboardViewModel` 中新增對大盤指數的歷史資料請求。可由使用者在設定中選擇，或是預設撈取 S&P 500。利用現有的 `MarketRepository.fetchHistoricalPrices`。

2.  **基期歸一化 (Normalization)**:
    將選定時間區間的**第一個資料點**作為基準點（0% 或 100%）。
    計算公式：`NormalizedValue(t) = (Value(t) / Value(t_0)) - 1.0` (得出如 +0.05 代表 5%)。

3.  **整合至圖表繪製 (Canvas Drawing)**:
    在 Canvas 中，Y 軸不再代表絕對金額，而是代表百分比。
    *   找出資產與大盤的全局 `maxNormalizedVal` 與 `minNormalizedVal` 來決定 Y 軸的 Range。
    *   繪製兩條不同顏色的貝氏曲線（例如資產使用 Primary Color，大盤使用 Secondary/Gray Color）。
    *   為大盤指數加上虛線樣式 `PathEffect.dashPath(floatArrayOf(10f, 10f))` 以利區分。

## 4. 分步實作計劃

為了確保開發品質與穩定性，建議分為以下四個里程碑（Milestones）：

### Milestone 1: 核心資料重構與時間範圍切換
*   **目標**: 實作 `TimeRange` 切換邏輯，調整 `DashboardViewModel` 抓取與快取歷史資料的機制。
*   **任務**:
    *   更新 `MarketRepositoryImpl` 支援動態 interval 查詢。
    *   在 `DashboardScreen` UI 頂部新增時間範圍切換按鈕 (SegmentedButton 或 Row of TextButtons)。
    *   優化 `DashboardViewModel` 中的 `trendPoints` 狀態流，使其根據選擇的區間重新計算歷史資產價值。

### Milestone 2: 互動式游標與提示框
*   **目標**: 讓 Canvas 圖表支援觸控互動。
*   **任務**:
    *   在 `AssetTrendChart` 中加入 `pointerInput` 以捕捉拖曳手勢。
    *   實作座標對應資料點的數學轉換邏輯。
    *   在 Canvas 上繪製動態垂直線與選取點的高光標示。
    *   利用 Compose `Popup` 或 `Box` 偏移來顯示包含日期與金額的 Tooltip。

### Milestone 3: 大盤對照功能
*   **目標**: 引入大盤指數並進行繪製。
*   **任務**:
    *   新增取得大盤歷史資料的非同步邏輯。
    *   實作歸一化演算法 (Normalization)，將資產與大盤轉換至百分比基底。
    *   更新 `AssetTrendChart` 以支援接收兩組 `ChartPoint`，並繪製兩條不同樣式（實線/虛線）的曲線。
    *   更新互動游標，當滑動時同時顯示資產與大盤的數值。

### Milestone 4: 效能優化與測試
*   **目標**: 確保計算效率與 UI 順暢度。
*   **任務**:
    *   確保所有迴圈與歷史資料合併的計算皆在 `Dispatchers.Default` 中執行，避免卡頓 Main Thread。
    *   針對 `DashboardViewModel` 的時間切換邏輯撰寫 Unit Tests，使用 `kotlinx-coroutines-test` 中的 `runTest` 驗證 `StateFlow` 的排放（遵循專案測試規範）。
    *   測試極端狀況，如無網路連線時的圖表展示與資料回退機制。
