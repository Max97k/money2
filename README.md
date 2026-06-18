# Money2 - Investment Portfolio Manager

Money2 is a fast, smooth, and privacy-focused local investment portfolio management application for Android. It focuses on tracking stocks and ETFs with a seamless user experience, inspired by Google Finance.

## Features

- **Privacy-First**: No backend server. All your data, including API keys, is stored locally and securely using Android's `EncryptedSharedPreferences` and `Room Database`.
- **Dashboard**: View your total value, day's gain, total gain, and a performance chart of your investments.
- **Holdings**: A complete list of your investments, showing symbols, current price, daily change, quantity, total value, and return rate.
- **Transactions**: Quick and smooth transaction entry using a global FAB and Bottom Sheet.
- **BYOK (Bring Your Own Key)**: Use your own Finnhub API key to fetch real-time US stock quotes. TWSE (Taiwan Stock Exchange) data is fetched directly without rate limits.

## Tech Stack

This project is built using modern Android development best practices (Clean Architecture):

- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3 (Dynamic Color)
- **Dependency Injection**: Koin
- **Navigation**: Compose Type-Safe Navigation
- **Concurrency**: Kotlin Coroutines + Flow
- **Local Database**: Room
- **Networking**: Retrofit + OkHttp
- **Security**: EncryptedSharedPreferences

## Getting Started

### Prerequisites

- Android Studio
- JDK 17
- Android SDK (API 37 support)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Max97k/money2.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device running Android 8.0 (API 26) or higher.

### API Key Setup

To fetch US stock prices, you need a Finnhub API key.
1. Sign up at [Finnhub](https://finnhub.io/) to get a free API key.
2. Open the Money2 app, navigate to **Settings**, and enter your API key.
3. The key will be securely saved locally on your device.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
