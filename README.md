# SecureBeam 🛡️⚡

> **Air-Gapped Offline Optical File Transfer Protocol for Android & Web**  
> Designed & Developed by **[@Devlopwithparth](https://github.com/Devlopwithparth)**

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Web-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-cyan.svg)]()
[![Security](https://img.shields.io/badge/Encryption-AES--256--GCM-red.svg)]()

---

## 📌 Overview

**SecureBeam** is an enterprise-grade, zero-trust offline file transfer application engineered to securely transmit confidential data between devices without requiring **Internet, Wi-Fi, Bluetooth, NFC, or Mobile Data**. 

Instead of traditional wireless signals, SecureBeam uses an **optical data stream** — rendering compressed, AES-256-GCM encrypted payload chunks into high-speed animated QR code sequences (5–30 FPS) displayed on one device's screen and scanned via CameraX on the receiving device.

---

## ✨ Key Features

- **🚀 100% Air-Gapped Transmission**: Zero network interface exposure. Immune to wireless interception, Wi-Fi sniffing, or Man-in-the-Middle (MitM) attacks.
- **🔐 AES-256-GCM & RSA Security**: Military-grade authenticated encryption paired with RSA-2048 digital signatures and SHA-256 file checksums.
- **📸 CameraX Optical QR Streaming**: High-speed animated QR code generator (5 to 30 FPS) with dynamic frame density and automated lost-frame recovery bit-matrices.
- **🔥 DoD 5220.22-M File Shredder**: 3-Pass zeroization and random data overwrite protocol to permanently eradicate sensitive temporary cache files.
- **🎨 Modern Material 3 Cyber Dark UI**: Obsidian aesthetics built with Jetpack Compose featuring smooth animations, dashboard statistics, trust score gauges, and security audit event logs.
- **🌐 Web Browser Version Included**: Run and test the full optical transfer pipeline directly in any modern browser.

---

## 🏗️ Tech Stack

- **Android App**: Android Studio, Kotlin, Jetpack Compose Material 3, Navigation Compose, MVVM, Room Database, CameraX, ZXing, Biometrics (`androidx.biometric`), Security Crypto (`androidx.security:security-crypto`).
- **Web App**: HTML5, Vanilla CSS3 (Obsidian Dark palette), JavaScript (ES6+), `QRCode.js`, `jsQR`.
- **Architecture**: Modular MVVM with Repository Pattern, Clean Architecture, and Coroutines Flow.

---

## 🚀 How to Run

### 1. Web Version (Instant Test)
Open `index.html` in any web browser or host on GitHub Pages:
- Default PIN: **`1234`**

### 2. Android Project Setup
1. Clone or download this repository.
2. Open the folder in **Android Studio**.
3. Allow Gradle to sync dependencies (JDK 17 or JDK 21 required).
4. Run on an Android device or emulator with Camera support (**Shift + F10**).

---

## 📄 License & Copyright

```text
Copyright (c) 2026 SecureBeam. All Rights Reserved.
Designed & Developed by @Devlopwithparth
```

Permission is hereby granted to use, modify, and distribute this software for educational and personal projects, provided developer attribution is preserved.

---

<p center>
  <b>Designed & Developed with ❤️ by <a href="https://github.com/Devlopwithparth">@Devlopwithparth</a></b>
</p>
