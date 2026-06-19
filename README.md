# Money2 - Investment Portfolio Manager

Money2 is a fast, smooth, and privacy-focused local investment portfolio management application for Android. It focuses on tracking stocks and ETFs with a seamless user experience, inspired by Google Finance.

## 📚 Documentation

- [📝 Implementation Plan](Implementation_Plan.md) - Architecture, scope, and development phases.
- [🛡️ Privacy Policy](PRIVACY.md) - Details on our local-first, privacy-focused approach.
- [🤝 Contributing](CONTRIBUTING.md) - Guidelines for submitting issues and pull requests.
- [⚖️ License](LICENSE) - MIT License details.
- [🔖 Third-Party Notices](THIRD_PARTY_NOTICES.md) - Acknowledgments and open-source licenses.
## Features

- **Privacy-First**: No backend server. All your data, including API keys, is stored locally and securely using Android's `EncryptedSharedPreferences` and `Room Database`.
- **High Performance**: Market prices are fetched in parallel via Coroutines for lightning-fast portfolio updates.
- **Secure by Design**: Strict security practices, including disabled HTTP logging in production and secure local proxy secret management.
- **Dashboard**: View your total value, day's gain, total gain, and a performance chart of your investments.
- **Holdings**: A complete list of your investments, showing symbols, current price, daily change, quantity, total value, and return rate.
- **Transactions**: Quick and smooth transaction entry using a global FAB and Bottom Sheet.
- **Proxy API Integration**: Uses a Cloudflare Worker proxy for Finnhub to fetch real-time US stock quotes securely without needing client-side API keys.

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

### Versioning

We use [Semantic Versioning](http://semver.org/) for versioning. For the versions available, see the tags on this repository.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Privacy

Money2 is designed with a **Privacy-First** approach. For more details, please review our [Privacy Policy](PRIVACY.md).

## Acknowledgments

This app is built with amazing open-source technologies. See [Third-Party Notices](THIRD_PARTY_NOTICES.md) for the full list of libraries and their licenses.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
