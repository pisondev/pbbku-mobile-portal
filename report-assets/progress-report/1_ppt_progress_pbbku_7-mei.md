# PBB-Ku Mobile Portal

PBB-Ku Mobile Portal adalah proyek aplikasi mobile Android untuk portal wajib pajak PBB-P2. Aplikasi dirancang sebagai client yang membantu wajib pajak melihat informasi objek pajak, data bangunan, histori SPPT, tunggakan, dan pengingat pembayaran dengan mengambil data dari SIMPBB OP API.

**Pengembang:**

- Mikail Achmad | 24/542370/PA/23026
- Pison Golda Mountera | 24/543770/PA/23107
- Muhammad Rayyan Buna Satria | 24/543564/PA/23096



---

# Tahap 0

## Persiapan dan Pembekuan Scope



---

# Tahap 0

- Tetapkan Android sebagai satu-satunya platform MVP.
- Tetapkan Kotlin sebagai bahasa utama.
- Tetapkan Jetpack Compose sebagai UI framework.
- Tetapkan networking client: Retrofit + OkHttp.
- Tetapkan JSON parser: kotlinx.serialization.
- Tetapkan local storage: DataStore Preferences untuk session/preferensi dan Room untuk cache/draft terstruktur.
- Tetapkan mekanisme notifikasi lokal: WorkManager.
- Tetapkan minimum Android SDK: Android 8.0/API 26.
- Tetapkan struktur package: feature-based dengan layer ringan.
- Tetapkan data demo aman dari Postman environment dan/atau data dummy yang tidak mengandung data pribadi nyata.
- Dokumentasikan bahwa endpoint write objekPajak/save tidak dipakai di MVP.



---

# Output tahap ini

- Keputusan stack final dicatat di README atau dokumen teknis.
- Scope MVP dan non-MVP dipahami seluruh anggota tim.
- Data demo aman tersedia.



---

# Keputusan Final Tahap ini

| Area | Keputusan |
|---|---|
| Platform | Android saja untuk MVP |
| Bahasa Utama | Kotlin |
| UI Framework | Jetpack Compose |
| Networking | Retrofit + OkHttp |
| JSON Parser | kotlinx.serialization |
| Session dan Preferensi | DataStore Preferences |
| Cache dan Draft Terstruktur | Room |
| Notifikasi Lokal | WorkManager |
| Minimum SDK | Android 8.0/API 26 |
| Struktur Package | Feature-based dengan layer ringan per fitur |
| Data Demo | Data contoh Postman environment dan/atau data dummy yang aman, bukan data pribadi nyata |
| Endpoint Write | objekPajak/save tidak dipakai dalam alur MVP portal wajib pajak |



---

# Tahap 1 - Setup Project Android

- [x] Inisialisasi source Android di `apps/android/app/`.
- [x] Konfigurasi Gradle project dan module app.
- [x] Tambahkan dependency Compose, Navigation Compose, ViewModel, coroutine, networking, serialization, DataStore/Room, dan notification scheduler.
- [x] Buat konfigurasi base URL terpisah dari kode utama.
- [x] Buat struktur folder awal:
  - [x] `data/api` untuk service SIMPBB.
  - [x] `data/dto` untuk request/response API.
  - [x] `data/repository` untuk repository data.
  - [x] `domain/model` untuk model domain.
  - [x] `domain/usecase` jika diperlukan.
  - [x] `ui/navigation` untuk routing screen.
  - [x] `ui/screen` atau package per fitur.
  - [x] `core/format`, `core/result`, `core/error`, dan `core/security` untuk helper umum.
- [x] Buat theme dasar, typography, warna status, dan komponen reusable.
- [x] Buat navigation graph awal.
- [x] Buat screen placeholder untuk seluruh UI MVP.



---

# Tahap 1 - Setup Project Android

## Output Tahap Ini

- Aplikasi dapat build.
- Splash atau halaman awal tersedia pada source aplikasi.

## Progress

- Source Android dibuat dengan Gradle Kotlin DSL di `apps/android`.
- Gradle Wrapper dibuat dengan Gradle `8.10.2`.
- Module app memakai package `id.pbbku.mobileportal`.
- Dependency awal sudah mencakup Jetpack Compose, Navigation Compose, Lifecycle ViewModel Compose, kotlinx.coroutines, Retrofit, OkHttp, kotlinx.serialization, DataStore Preferences, Room, dan WorkManager.
- Struktur source memakai feature-based ringan untuk `feature/home`, `feature/search`, `feature/notifications`, dan `feature/settings`, dengan layer pendukung `core`, `data`, `domain`, dan `ui`.
- `SimpbbApiConfig.BASE_URL` sudah disiapkan dengan nilai `https://simpbb.technosmart.id/api/rpc/`.
- Environment Android resmi project memakai setup MSYS2 zsh dengan `ANDROID_HOME=/c/Android/Sdk`.
- File lokal `apps/android/local.properties` dibuat untuk mengarah ke `C:\Android\Sdk`; file ini tidak di-commit karena sudah masuk `.gitignore`.
- SDK utama `C:\Android\Sdk` sudah memuat `platforms;android-35` dan `build-tools;35.0.0` untuk kebutuhan `compileSdk=35`.
- Build command terverifikasi dari MSYS2 zsh: `cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android` lalu `./gradlew :app:assembleDebug`.
- Build offline terverifikasi dari MSYS2 zsh: `./gradlew :app:assembleDebug --offline`.
- Hasil verifikasi build: `BUILD SUCCESSFUL`, menghasilkan APK debug di `apps/android/app/build/outputs/apk/debug/app-debug.apk`.
- Runtime test awal Tahap 1 belum dilakukan saat itu; runtime dasar kemudian berhasil pada Tahap 4 menggunakan emulator headless `Pixel_6_API_35`.



---

# Tahap 2 - Fondasi Domain, Local State, dan Security

Ini adalah area penjelasan atau deskripsi yang berkaitan dengan materi sub judul.

- [x] Buat model `Nop` dengan semua segmen bertipe `String`.
- [x] Buat helper untuk membentuk NOP display lengkap tanpa menghapus leading zero.
- [x] Buat parser NOP lengkap ke `NOP_OBJECT` jika pengguna memasukkan NOP sebagai satu string.
- [x] Buat formatter rupiah.
- [x] Buat formatter tanggal Indonesia.
- [x] Buat formatter status bayar: `Lunas`, `Belum Lunas`, `Jatuh Tempo`, dan fallback `Tidak Diketahui`.
- [x] Buat result wrapper client untuk `Loading`, `Success`, `Empty`, dan `Error`.
- [x] Buat mapping error network, timeout, HTTP error, parse error, dan empty response.
- [x] Buat local session simulatif.
- [x] Buat masking NIK, misalnya `34************12`.
- [x] Pastikan NIK tidak disimpan plaintext. Jika penyimpanan aman belum tersedia, simpan token/session simulatif dan NIK masked saja.
- [x] Buat mekanisme hapus session/logout.
- [x] Buat mekanisme cache read-only data terakhir dengan timestamp.
- [x] Buat penyimpanan draft laporan perubahan bangunan.



---

# Tahap 2 - Fondasi Domain, Local State, dan Security

## Output Tahap Ini

- Model dasar siap dipakai semua fitur.
- Session lokal simulatif berjalan.
- Cache dan draft lokal siap dipakai.
- Kebijakan masking data sensitif diterapkan sejak awal.

## Progress

- `Nop` sekarang memiliki `parseOrNull`, `asDisplayText`, `asGroupedText`, dan `asApiMap`; seluruh segmen tetap `String`.
- `PaymentStatus` menyiapkan label `Lunas`, `Belum Lunas`, `Jatuh Tempo`, dan `Tidak Diketahui`.
- Formatter rupiah, tanggal Indonesia, dan status pembayaran disiapkan di `core/format`.
- `AppResult` dan `AppError` menyiapkan state dan pesan untuk loading, success, empty, network error, timeout, HTTP error, parse error, dan unknown error.
- `SessionDataStore` memakai DataStore Preferences untuk menyimpan `maskedNik`, `sessionToken`, dan timestamp session; NIK asli tidak disimpan plaintext.
- Logout simulatif tersedia melalui `SessionDataStore.logout()`.
- `PbbKuDatabase` memakai Room untuk `cache_entries` dan `report_drafts`.
- `LocalCacheRepository` menyimpan cache read-only dengan `updatedAtEpochMillis`.
- `ReportDraftRepository` menyimpan draft laporan perubahan bangunan dengan status `DRAFT`, `READY_TO_SUBMIT`, atau `SENT_SIMULATION`.
- `PbbKuApplication` menyiapkan akses lazy ke `PbbKuDatabase` dan `SessionDataStore`.
- Unit test helper kritis tersedia untuk NOP parser dan NIK masker.
- Verifikasi: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil, total 4 unit test lulus.



---

# TERIMA KASIH


