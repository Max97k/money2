# Privacy Policy

**Last Updated:** June 2026

Money2 ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how our Android application handles your data.

## 1. Data Collection & Storage
Money2 is a **local-first, serverless application**. We do **not** collect, store, or transmit any of your personal data, portfolio holdings, or transaction history to our servers or any third-party analytics services. 
All your data is stored locally on your device using Android's `Room Database`.

## 2. API Keys & Secrets
If you provide API keys (such as a Finnhub API Key) for fetching real-time market data, these keys are encrypted and stored locally on your device using Android's `EncryptedSharedPreferences`. We do not have access to your API keys.

## 3. Network Requests
To provide real-time stock and ETF quotes, Money2 makes direct network requests to the following third-party financial APIs:
- **Finnhub / Cloudflare Worker Proxy** (for US market data)
- **TWSE Open Data** (for Taiwan market data)
- **ExchangeRate API** (for currency conversion)

These requests only contain the stock symbols or currency codes required to fetch the data. No personal identifiers or portfolio balances are sent during these requests. Please refer to the privacy policies of these respective services for how they handle API requests.

## 4. Analytics & Tracking
We do **not** use any third-party tracking, crash reporting (e.g., Firebase Crashlytics), or analytics software in this application.

## 5. Changes to This Policy
We may update this Privacy Policy from time to time as we add new features. Any changes will be reflected in this file.

## 6. Contact
If you have any questions or concerns about this Privacy Policy, please open an issue in the [Money2 GitHub Repository](https://github.com/Max97k/money2/issues).
