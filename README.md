<h1 align="center">
  <img src="Store%20Assets/store_logo_512.png" width="42" height="42" valign="middle" alt="OptiDoc Logo" /> OptiDoc
</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material_3-7C4DFF?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" />
  <img src="https://img.shields.io/badge/Google_ML_Kit-FFCA28?style=for-the-badge&logo=google&logoColor=black" alt="Google ML Kit" />
  <img src="https://img.shields.io/badge/Room_DB-4285F4?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" />
  <img src="https://img.shields.io/badge/Hilt-34A853?style=for-the-badge&logo=dagger&logoColor=white" alt="Hilt" />
  <img src="https://img.shields.io/badge/Offline_First-00897B?style=for-the-badge" alt="Offline First" />
  <img src="https://img.shields.io/badge/Version-v1.3.1-blue?style=for-the-badge" alt="Version" />
  <img src="https://img.shields.io/badge/License-MIT-4c1?style=for-the-badge" alt="License" />
</p>

<p align="center">
  <img src="GitHub%20Showcase.png" width="100%" alt="OptiDoc Showcase Banner" />
</p>

**OptiDoc** is a privacy-first, local-first intelligent document scanner and OCR application built for Android. 
Designed with a modern, high-performance UI using **Jetpack Compose** and **Material 3**, OptiDoc enables users to digitize physical documents, extract text via on-device Optical Character Recognition (OCR), apply custom watermarks and digital signatures, and organize everything into folders with 100% offline security.

---

## 🚀 Highlights & Releases
See [CHANGELOG.md](CHANGELOG.md) for the complete release history, updates, and changelogs.  
*(Tüm sürüm geçmişi ve güncelleme notları için [CHANGELOG.md](CHANGELOG.md) dosyasına göz atabilirsiniz.)*

---

## 🇬🇧 English

### ✨ Key Features

- 📄 **Smart Document Scanning:**
  - Powered by Google ML Kit Document Scanner with automatic edge detection, cropping, and shadow removal.
  - Multi-page batch scanning with instant perspective correction.

- 🔍 **On-Device OCR (Optical Character Recognition):**
  - Extract text from scanned pages accurately using ML Kit Text Recognition v2.
  - In-app text editing and proofreading before exporting.

- ✍️ **Digital Signature & Custom Watermark:**
  - Draw custom digital signatures directly onto documents with intuitive canvas controls.
  - Add personalized text watermarks with customizable opacity, color, and positioning.

- 📁 **Folder & Document Management:**
  - Organize scans into custom color-coded folders.
  - Instant real-time title search and flexible sorting (by date and title).
  - Page reordering: move pages left/right in multi-page documents seamlessly.

- 📤 **Versatile Export Options:**
  - Export as standard searchable PDF, text-only PDF, plain text (`.txt`), or high-resolution JPEG images.
  - Integrated system share sheets with memory-efficient bitmap subsampling.

- 🛡️ **Offline-First & Total Privacy:**
  - All document processing, OCR extraction, and storage happen 100% locally on your device.
  - Zero cloud dependencies, no mandatory accounts, zero tracking.

### 🛠️ Architecture & Tech Stack

- **UI & Design:** Jetpack Compose, Material 3, Plus Jakarta Sans typography, Android 15 Edge-to-Edge display support.
- **Architecture:** Clean Architecture (MVVM/MVI), Single-Activity Multi-Screen Navigation Compose.
- **Dependency Injection:** Dagger Hilt (`@Singleton` / `@HiltViewModel`).
- **Asynchronous Execution:** Kotlin Coroutines & Reactive `StateFlow` streams.
- **Local Persistence:** Room SQLite Database with automated schema migrations.
- **Machine Learning:** Google ML Kit (Document Scanner & Text Recognition).
- **Monetization & Safety:** Google AdMob 23.6.0 with strict cooldown management and test device protection.

---

## 🇹🇷 Türkçe

### ✨ Temel Özellikler

- 📄 **Akıllı Belge Tarama:**
  - Google ML Kit Document Scanner ile otomatik kenar algılama, kırpma ve gölge giderme.
  - Otomatik perspektif düzeltmeli çok sayfalı seri tarama desteği.

- 🔍 **Cihaz İçi OCR (Optik Karakter Tanıma):**
  - ML Kit Text Recognition v2 ile taranan sayfalardaki metinleri yüksek doğrulukla çıkarın.
  - Dışa aktarmadan önce çıkarılan metinleri uygulama içinde düzenleme ve kontrol etme.

- ✍️ **Dijital İmza ve Özel Filigran:**
  - Parmağınızla çizerek belgelere kolayca özel dijital imza ekleme.
  - İstenen metin, saydamlık ve renkte özelleştirilebilir filigran yerleştirme.

- 📁 **Klasör ve Belge Yönetimi:**
  - Belgeleri özel klasörler altında gruplama ve düzenleme.
  - Anlık başlık araması ve esnek sıralama seçenekleri (tarihe ve başlığa göre).
  - Çok sayfalı belgelerde sayfaları sola/sağa taşıyarak kolayca sıralama.

- 📤 **Çok Yönlü Dışa Aktarma:**
  - Standart PDF, sadece metin içeren PDF, düz metin (`.txt`) veya yüksek çözünürlüklü JPEG formatlarında kaydetme ve paylaşma.
  - Bellek dostu bit eşlem alt örneklemesi (bitmap subsampling) ile optimize edilmiş paylaşım.

- 🛡️ **Çevrimdışı ve Tam Gizlilik:**
  - Tüm tarama, OCR metin çıkarma ve veritabanı işlemleri %100 telefonunuzda yerel olarak gerçekleşir.
  - Bulut bağımlılığı yok, hesap açma zorunluluğu yok, sıfır veri toplama.

### 🛠️ Mimari ve Teknolojiler

- **Arayüz ve Tasarım:** Jetpack Compose, Material 3, Plus Jakarta Sans tipografisi, Android 15 Edge-to-Edge tam ekran uyumu.
- **Mimari:** Clean Architecture (MVVM/MVI), Single-Activity Navigation Compose.
- **Bağımlılık Enjeksiyonu:** Dagger Hilt (`@Singleton` / `@HiltViewModel`).
- **Asenkron Yapı:** Kotlin Coroutines ve Reaktif `StateFlow` akışları.
- **Yerel Veritabanı:** Room SQLite Veritabanı (Otomatik Veritabanı Migration desteği ile).
- **Makine Öğrenimi:** Google ML Kit (Document Scanner & Text Recognition).
- **Gelir Modeli ve Güvenlik:** Google AdMob 23.6.0 (2 dakika bekleme süreli geçiş reklamları ve test cihazı koruması).

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
