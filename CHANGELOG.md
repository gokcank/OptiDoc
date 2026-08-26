# Changelog

All notable changes, patches, and new features in the OptiDoc project will be documented in this file.

*(OptiDoc projesindeki tüm önemli değişiklikler, yamalar ve yeni özellikler bu dosyada belgelenmektedir.)*

---

## [v1.3.1] - 2026-08-26

### 🇬🇧 English
#### 🛠️ Quality, Security & Infrastructure
- **Play Console Compliance:** Fully aligned with Google Play Console Pre-launch Report requirements (enabled R8 Full Mode, resource shrinking, and `FULL` NDK debug symbol mapping for crash/ANR symbolication).
- **Memory Optimization:** Implemented memory-efficient bitmap subsampling (`inSampleSize` & `inMutable`) in `ExportRepositoryImpl` to eliminate high-resolution RAM spikes.
- **Modern AdMob Integration:** Integrated Adaptive Banner with edge-to-edge window insets and safe Interstitial ads featuring a 2-minute cooldown and background preloading (`play-services-ads:23.6.0`).
- **Refreshed About Menu:** Added official developer GitHub link and logcat-assisted bug report email attachment via `FileProvider`.
- **Codebase Cleanliness:** Modernized Compose APIs (`LocalActivity.current`), cleaned up deprecated resource references, and purged 20+ unused strings.

### 🇹🇷 Türkçe
#### 🛠️ Kalite, Güvenlik ve Altyapı
- **Play Console Uyumu:** Google Play Console Ön Başlatma standartlarına tam uyum sağlandı (R8 Full Mode, Kaynak Küçültme ve çökmeleri netleştiren tam NDK sembol haritalaması).
- **Bellek Optimizasyonu:** Yüksek çözünürlüklü taramalarda RAM taşmasını engelleyen bellek dostu bit eşlem alt örneklemesi (`inSampleSize` ve `inMutable`) geliştirildi.
- **Modern AdMob Entegrasyonu:** Sistem çubuklarıyla uyumlu Banner ve 2 dakika bekleme süreli Geçiş reklamları entegre edildi (`play-services-ads:23.6.0`).
- **Yenilenen Hakkında Menüsü:** Geliştirici GitHub bağlantısı ve FileProvider destekli logcat ekli hata bildirimi eklendi.
- **Kod Temizliği:** Modern Compose API'lerine geçildi (`LocalActivity.current`) ve 20+ ölü kaynak temizlendi.

---

## [v1.3.0] - 2026-08-03

### 🇬🇧 English
#### ✨ Features & Visuals
- **Digital Signature:** Draw custom digital signatures directly onto documents with intuitive canvas controls.
- **Custom Watermark:** Add personalized text watermarks with custom opacity, color, and positioning.
- **Image Loading Stability:** Enhanced stability for document page viewing and multi-format exports.
- **Android 15 Ready:** Modern Edge-to-Edge display support and performance optimizations.

### 🇹🇷 Türkçe
#### ✨ Yenilikler ve Görsellik
- **Dijital İmza:** Ekrana çizilerek belgelere kolayca eklenebilen dijital imza özelliği getirildi.
- **Özelleştirilebilir Filigran:** İstenen metin, saydamlık ve renkte filigran ekleme desteği eklendi.
- **Görsel Yükleme İyileştirmesi:** Sayfa ve dosya görüntüleme kararlılığı artırıldı.
- **Android 15 Uyumu:** Modern uçtan uca (Edge-to-Edge) ekran ve performans geliştirmeleri yapıldı.

---

## [v1.2.2] - 2026-07-24

### 🇬🇧 English
#### ⚙️ Monetization & Security
- **AdMob Test Mode Configuration:** Isolated real ad units to official store release builds and configured Google Test Ad IDs for testing.
- **Invalid Traffic Protection:** Automated test device registration to protect developer accounts against invalid impressions.

### 🇹🇷 Türkçe
#### ⚙️ Gelir Modeli ve Güvenlik
- **AdMob Test Modu Yapılandırması:** Canlı reklam kimlikleri mağaza sürümüne izole edildi ve geliştirme için Google Test Ad ID altyapısı kuruldu.
- **Geçersiz Trafik Koruması:** Geliştirici hesabını korumak amacıyla otomatik test cihazı tescili sağlandı.

---

## [v1.2.1] - 2026-07-23

### 🇬🇧 English
#### ✨ UI/UX & Typography
- **Plus Jakarta Sans:** Upgraded entire app typography with the modern Plus Jakarta Sans font family.
- **ModalBottomSheet Export UX:** Redesigned export format selection with sleek bottom sheets.
- **Android 15/16 Compliance:** Full edge-to-edge support and large screen compatibility.
- **Automated Testing:** Expanded unit tests and test suites.

### 🇹🇷 Türkçe
#### ✨ Arayüz ve Tipografi
- **Plus Jakarta Sans:** Tüm uygulama tipografisi modern Plus Jakarta Sans yazı tipiyle yenilendi.
- **Modern Dışa Aktarma UX:** Dışa aktarma format seçimi modern alt menüye (BottomSheet) taşındı.
- **Android 15/16 Uyumu:** Tam ekran (Edge-to-Edge) görünüm ve büyük ekran desteği sağlandı.
- **Otomatik Testler:** Kapsamlı birim test altyapısı kuruldu.

---

## [v1.2.0] - 2026-07-21

### 🇬🇧 English
#### ✨ Folders & Document Management
- **Folder System:** Group and organize documents into custom folders with quick filtering.
- **Page Reordering:** Move pages left/right in multi-page documents seamlessly.
- **Safety Confirmations:** Added confirmation dialogs for document and folder deletion.

### 🇹🇷 Türkçe
#### ✨ Klasörler ve Belge Yönetimi
- **Klasör Sistemi:** Belgeleri özel klasörlerde gruplama ve filtreleme özelliği eklendi.
- **Sayfa Sıralama:** Çok sayfalı belgelerde sayfaları sola/sağa taşıyarak sıralama imkanı getirildi.
- **Güvenlik Onayları:** Belge ve klasör silme işlemleri için onay pencereleri eklendi.

---

## [v1.1.0] - 2026-07-21

### 🇬🇧 English
#### ✨ Search & Sorting
- **Instant Search:** Real-time search by document title across all folders.
- **Flexible Sorting:** Sort documents by date (newest/oldest) and title (A-Z / Z-A).

### 🇹🇷 Türkçe
#### ✨ Arama ve Sıralama
- **Anlık Arama:** Başlığa göre anında belge arama özelliği eklendi.
- **Esnek Sıralama:** Belgeleri tarihe (en yeni/eski) ve başlığa (A-Z / Z-A) göre sıralama desteği getirildi.

---

## [v1.0.0] - 2026-07-21

### 🇬🇧 English
#### 🚀 Initial Release
- **ML Kit Document Scanner:** High quality document scanning with automatic edge detection and cropping.
- **OCR Text Extraction:** On-device Optical Character Recognition powered by ML Kit Text Recognition.
- **Multi-Format Export:** Export documents as standard PDF or editable TXT files.
- **Offline & Private:** Fully on-device document processing with Room SQLite database.

### 🇹🇷 Türkçe
#### 🚀 İlk Sürüm
- **ML Kit Belge Tarayıcı:** Otomatik kenar algılamalı ve kırpmalı yüksek kaliteli belge tarama.
- **OCR Metin Çıkarma:** Cihaz üzerinde çalışan Optik Karakter Tanıma (ML Kit).
- **Çoklu Format Dışa Aktarma:** Belgeleri standart PDF veya TXT olarak kaydetme ve paylaşma.
- **Çevrimdışı ve Güvenli:** Room veritabanı ile tamamen cihaz üzerinde çalışan yerel mimari.
