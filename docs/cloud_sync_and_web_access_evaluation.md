# money2 雲端同步與跨裝置存取 (Cloud Sync & Web Access) 技術與實作方案評估

本報告旨在評估並設計為 `money2` Android 應用程式加入「雲端同步與跨裝置存取」功能的技術方案。目前專案所有資料（持倉、交易與設定）皆儲存於本地 Room 資料庫，此方案將在維持本地優先（Local-First）與高隱私性的前提下，加入雲端備份、跨裝置同步與網頁端存取能力。

## 1. 後端架構選型對比

為了支援跨裝置同步與網頁端存取，我們比較了兩種主流的無伺服器 (Serverless) 後端方案：

| 評估維度 | Cloudflare Workers + D1 Database | Google Cloud Firebase (Firestore + Cloud Functions) |
| :--- | :--- | :--- |
| **資料庫模型** | 關聯式資料庫 (SQLite 語法相容) | NoSQL 文件資料庫 |
| **與 Room 契合度** | **極高**。D1 基於 SQLite，與 Android Room 的資料模型（`TransactionEntity`, `HoldingEntity` 等）可直接對應。 | 中等。需要將關聯式的資料（如 Holding 與 Transaction 的關聯）轉換為文件結構。 |
| **離線支持 (Offline)** | 需自行實作同步協議與本地緩存邏輯。 | **極佳**。Firebase SDK 內建強大的離線緩存與重新連線後的自動同步功能。 |
| **營運成本** | **極低**。Cloudflare 提供極具競爭力的免費額度，且無冷啟動問題。 | 初期免費額度高，但讀寫操作次數計費，若資料量大或同步頻繁，成本可能快速上升。 |
| **開發複雜度** | 中等。需自行撰寫 API 路由與同步邏輯。 | 較低。使用 Firebase SDK 可大幅減少後端 API 開發工作，但需適應 NoSQL 查詢限制。 |
| **網頁端存取** | 極為便利。可輕鬆與 Cloudflare Pages 整合部署前端。 | 容易整合，Firebase Hosting 提供完善的前端部署支援。 |

**結論建議**：
考慮到 `money2` 的資料庫模型（Room / SQLite），**Cloudflare Workers + D1** 是最適合的長期方案。其與本地資料庫結構高度一致，能有效減少資料格式轉換的阻力，且後續營運成本極低。

## 2. 數據同步機制設計

為了維持應用程式在無網路下的可用性，應採用**離線優先 (Offline-First)** 架構。

### 2.1 同步標記與衝突處理
- **同步標記 (Sync Markers)**：在每個 Entity（如 `TransactionEntity`, `HoldingEntity`）中加入兩個控制欄位：
  - `updatedAt` (Long): 資料最後修改的時間戳記。
  - `syncStatus` (Int): 標記資料狀態（0: 已同步, 1: 待新增, 2: 待更新, 3: 待刪除）。
- **衝突處理機制 (Conflict Resolution)**：採用 **Last-Write-Wins (LWW, 最後寫入者獲勝)** 原則。當設備嘗試同步資料到雲端時，比對雲端資料庫中的 `updatedAt`。若設備端的 `updatedAt` 較新，則覆蓋雲端資料；若雲端較新，則設備端下載並更新本地資料。為了防止時鐘不同步，推薦使用伺服器端時間作為最終判定標準，或引入單調遞增的修訂號 (Revision Number)。

### 2.2 Schema 設計與 API 端點
**新增雲端同步表 (Cloud Sync Table) 範例 (D1 SQLite):**
```sql
CREATE TABLE CloudTransactions (
    id TEXT PRIMARY KEY, -- 使用 UUID 取代本地 AutoGenerate Long ID，確保跨裝置唯一性
    userId TEXT NOT NULL,
    title TEXT,
    amount REAL,
    category TEXT,
    type TEXT,
    date INTEGER,
    note TEXT,
    updatedAt INTEGER NOT NULL,
    isDeleted INTEGER DEFAULT 0
);
```
*(註：原本的 `TransactionEntity` 需將 `id` 改為 UUID (String) 才能在分散式環境下避免 ID 碰撞。)*

**RESTful API 端點設計:**
- `POST /api/sync/pull`: 取得自上次 `lastSyncTime` 之後所有變更的資料。
- `POST /api/sync/push`: 將本地端標記為待同步的變更批次上傳至伺服器。伺服器回傳衝突處理後的最終狀態。

## 3. 帳號認證與隱私防護

保持 `money2` 的高隱私性是首要任務。

### 3.1 帳號認證 (Authentication)
建議採用 **Firebase Authentication**（若選擇 Firebase 生態）或 **Cloudflare Access / Auth0** 來處理使用者登入。
為了最大化使用者體驗，應支援 **Google Sign-In**。
- 未登入的使用者：可完全在本地使用應用程式，行為與現狀一致。
- 已登入的使用者：取得 JWT Token 後，附帶於每次 API 請求的 Authorization Header 中，用以隔離並存取該使用者的雲端資料庫（透過 `userId` 進行多租戶隔離）。

### 3.2 端對端加密 (End-to-End Encryption, E2EE)
為確保敏感的財務資料不被伺服器管理者窺探，應實作端對端加密：
- 用戶在啟用同步時，需產生或設定一組**主密碼 (Master Password)** 或**恢復金鑰 (Recovery Key)**。
- 該金鑰不應上傳至伺服器。利用 Android 的 `EncryptedSharedPreferences` 儲存在本地。
- 資料在上傳至 API 之前（例如交易金額、備註等欄位），在客戶端使用 AES-GCM 演算法進行加密。伺服器（D1 或 Firestore）只儲存加密後的字串與 `updatedAt`。
- 網頁端存取時，使用者登入後需輸入相同的恢復金鑰才能在瀏覽器本地解密資料。

## 4. 網頁端存取方案 (Web Access)

建立一個與 Android 專案共享同一個雲端資料庫的 Web 伴隨式應用程式 (Companion App)。

- **技術堆疊**：使用 **React (Next.js)** 或 **Vue (Vite)** 建立前端單頁應用程式 (SPA)。可部署於 Cloudflare Pages 或 Vercel。
- **功能定位**：初期可作為「唯讀儀表板 (Read-only Dashboard)」，讓使用者能在電腦大螢幕上檢視資產分佈圖表與歷史交易紀錄。
- **架構設計**：
  - 前端應用透過相同的 Cloudflare Workers API 存取 D1 Database。
  - 使用者透過 Google Sign-In 登入網頁版，並在第一次載入時輸入「恢復金鑰」解密資料（如果啟用了 E2EE）。
  - 將複雜的圖表與數據視覺化邏輯放在網頁端，提供比手機更豐富的報表分析功能。

## 5. 分步實作計劃

為了降低開發風險，建議將此大型功能拆分為三個階段進行實作：

### 第一階段：資料匯出/匯入與本地備份 (Phase 1)
- **目標**：不依賴雲端伺服器，先解決基本的資料遷移需求。
- **工作項目**：
  1. 將 Room Database 內的資料轉換為 JSON 或 CSV 格式。
  2. 實作本地檔案匯出與匯入功能（使用 Storage Access Framework）。
  3. 將本地 Entity 的主鍵 (`id`) 從 `Long` (AutoGenerate) 遷移到 `String` (UUID)，為未來的雲端同步做準備。

### 第二階段：雲端 API 建立與自動同步 (Phase 2)
- **目標**：實作跨裝置的無縫資料同步。
- **工作項目**：
  1. 建立後端基礎設施（如設定 Cloudflare Workers 與 D1）。
  2. 整合 Google 登入 (OAuth) 取得使用者身分。
  3. 修改 Android App 資料庫模型，加入 `updatedAt` 與 `syncStatus` 欄位。
  4. 實作後台 WorkManager 任務，在網路連線恢復或應用程式啟動時自動呼叫 `push` 與 `pull` API 進行雙向同步（處理 LWW 衝突）。
  5. （選擇性）實作基於主密碼的端對端加密機制。

### 第三階段：Web 唯讀檢視看板 (Phase 3)
- **目標**：提供跨平台的網頁端存取體驗。
- **工作項目**：
  1. 建立 React/Vite 前端專案。
  2. 整合前端的帳號登入與資料解密邏輯。
  3. 實作儀表板 UI（如資產圓餅圖、歷史交易列表）。
  4. 部署至 Cloudflare Pages 或其他靜態託管平台。
