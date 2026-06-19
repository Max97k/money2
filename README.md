<div align="center">

# Money2

**A modern, privacy-first, and high-performance investment portfolio manager for Android.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Android Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

[English](README.md) | [繁體中文](README_zh.md)

</div>

---

Money2 is an open-source Android application designed to provide a seamless, performant, and completely secure experience for tracking your stock and ETF investments. Inspired by top-tier financial apps, Money2 is built with modern Android architecture and a strict local-first philosophy.

## ✨ Why Money2?

* **Absolute Privacy**: Zero backend servers. Zero cloud sync. All of your financial data and API keys are stored locally on your device, secured by Android's Hardware-Backed Keystore via `EncryptedSharedPreferences`.
* **Lightning Fast**: Built natively with Jetpack Compose and Kotlin Coroutines. Real-time market data is fetched entirely in parallel.
* **Modern UI/UX**: Adheres to Material Design 3 guidelines with Dynamic Color support, offering a beautifully fluid native Android experience.
* **Seamless Multi-Currency**: Automatically handles cross-border portfolios (e.g., USD and TWD) with real-time exchange rate calculations.

## 🛠 Tech Stack & Architecture

Money2 follows the **Clean Architecture** principles, strictly separating concerns into Data, Domain, and Presentation layers.

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Architecture**: MVI (Model-View-Intent) & Clean Architecture
* **Dependency Injection**: [Koin](https://insert-koin.io/)
* **Concurrency**: Kotlin Coroutines & Flow
* **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
* **Networking**: Retrofit2 + OkHttp3
* **Security**: Jetpack Security Crypto (`EncryptedSharedPreferences`)

## 🚀 Getting Started

### Prerequisites
* [Android Studio Koala](https://developer.android.com/studio) (or newer)
* JDK 17
* Android SDK (Min API 26 / Target API 37)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Max97k/money2.git
   cd money2
   ```
2. Open the project in Android Studio.
3. Configure the local proxy secret (if required by your custom worker). Add the following line to your `local.properties` file:
   ```properties
   PROXY_SECRET=your_secret_here
   ```
4. Build and deploy the application to your emulator or physical Android device.

## 🔒 Security Model

We take data protection seriously. 
* **No Telemetry**: We do not include any third-party tracking or analytics SDKs.
* **Secret Management**: All sensitive API keys are kept strictly within `local.properties` and are injected securely at compile time via `BuildConfig`.
* **Data Encryption**: All local preferences are encrypted.

Please review our [Privacy Policy](PRIVACY.md) for comprehensive details.

## 📖 Documentation

Dive deeper into our architecture and guidelines:
- [📝 Implementation Plan](Implementation_Plan.md) - Core architecture and development phases.
- [🤝 Contributing Guidelines](CONTRIBUTING.md) - How to submit issues and PRs.
- [🔖 Third-Party Notices](THIRD_PARTY_NOTICES.md) - Acknowledgments.

## 🤝 Contributing

We welcome contributions from the community! Whether it's a bug report, a new feature proposal, or a code refactor:
1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

Please ensure your PR adheres to our [Code of Conduct](CONTRIBUTING.md).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
<div align="center">
  <sub>Built with ❤️ for the open-source community.</sub>
</div>
