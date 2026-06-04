# PBB-Ku MVP Contract v1

Dokumen ini adalah kontrak kerja MVP untuk implementasi aplikasi Android PBB-Ku Mobile Portal. Isi dokumen disusun dari `README.md`, `docs/srs/SRS_PBBKu.md`, `docs/api/SIMPBB_OP_API.md`, `docs/api/SIMPBB_OP_API.postman_collection.json`, dan `docs/api/SIMPBB_OP_API.postman_environment.json`.

## 1. Tujuan MVP

MVP PBB-Ku harus menghasilkan aplikasi Android yang dapat didemonstrasikan sebagai portal wajib pajak PBB-P2. Aplikasi berperan sebagai client yang mengambil data resmi dari SIMPBB OP API, lalu menampilkan informasi objek pajak, subjek pajak, bangunan, SPPT, tunggakan, informasi pembayaran non-transaksional, pengingat lokal, dan draft laporan perubahan bangunan.

MVP selesai ketika pengguna demo dapat menjalankan alur utama berikut:

1. Membuka aplikasi dan masuk melalui login NIK + OTP simulatif.
2. Mencari objek pajak berdasarkan NOP atau nama wajib pajak.
3. Membuka detail objek pajak dan subjek pajak.
4. Melihat daftar/detail bangunan dan fasilitas LSPOP jika tersedia.
5. Melihat histori SPPT, detail tagihan per tahun, dan daftar tunggakan.
6. Melihat informasi pembayaran non-transaksional dan status tagihan.
7. Mengaktifkan atau melihat pengingat lokal jatuh tempo.
8. Mengisi draft laporan mandiri perubahan bangunan sebagai fitur prototipe.
9. Menjalankan aplikasi tanpa crash saat data kosong, API gagal, field null, atau koneksi tidak stabil.

## 2. Batasan MVP

- Tidak membangun backend internal.
- Tidak membangun database server internal.
- Tidak membuat API baru untuk data PBB resmi.
- Tidak melakukan payment gateway atau transaksi pembayaran nyata.
- Tidak melakukan update langsung data resmi SPOP/LSPOP.
- Tidak menggunakan endpoint `objekPajak/save` untuk alur wajib pajak MVP.
- Tidak membuat panel admin Bapenda.
- Tidak menggunakan data pribadi nyata untuk demo publik, screenshot, atau repository.
- Login NIK dan OTP bersifat simulatif.
- Laporan perubahan bangunan disimpan sebagai draft/prototipe lokal, bukan dikirim ke data resmi Bapenda.
- Data cache harus diberi label sebagai data terakhir diperbarui, bukan data real-time.

## 3. Prinsip Integrasi SIMPBB OP API

- Base URL: `https://simpbb.technosmart.id/api/rpc`.
- Semua endpoint memakai HTTP `POST`.
- Semua request memakai `Content-Type: application/json`.
- Body request selalu dibungkus dalam field `json`.
- Response utama selalu dibaca dari field `json`.
- Semua segmen NOP harus diperlakukan sebagai `String` agar leading zero tidak hilang.
- `NOP_OBJECT` wajib memakai field `kdPropinsi`, `kdDati2`, `kdKecamatan`, `kdKelurahan`, `kdBlok`, `noUrut`, dan `kdJnsOp`.
- Nilai uang, NJOP, luas bumi, dan luas bangunan diformat ulang untuk UI, bukan ditampilkan mentah tanpa validasi.
- Endpoint helper seperti `getNextNoUrut`, `getNextNoFormulir`, dan `nextNoBng` boleh ada di dokumentasi/dev tools, tetapi bukan prioritas MVP portal wajib pajak.

## 4. Definition of Done Umum

- Project Android dapat dibuka dan dijalankan dari Android Studio/Gradle.
- Aplikasi berjalan pada emulator Android untuk demo.
- UI utama menggunakan bahasa Indonesia yang mudah dipahami.
- Semua screen utama punya loading, empty, error, retry, dan success state sesuai kebutuhan.
- Tidak ada crash saat API mengembalikan `null`, response kosong, atau data parsial.
- Tidak ada NIK penuh atau data sensitif di UI setelah login, log debug, screenshot publik, atau file repository.
- Semua request API menggunakan wrapper `json`.
- Semua parsing response mengambil data dari wrapper `json`.
- Semua segmen NOP tetap string dari input, request, cache, sampai UI.
- Nilai rupiah dan tanggal jatuh tempo ditampilkan dengan format Indonesia.
- Data dummy/prototipe diberi label yang jelas.
- README diperbarui dengan instruksi build/run setelah source Android tersedia.
- Minimal ada catatan pengujian manual untuk alur utama MVP.

## 5. Tahap 0 - Persiapan dan Pembekuan Scope

- [x] Tetapkan Android sebagai satu-satunya platform MVP.
- [x] Tetapkan Kotlin sebagai bahasa utama.
- [x] Tetapkan Jetpack Compose sebagai UI framework.
- [x] Tetapkan networking client: Retrofit + OkHttp.
- [x] Tetapkan JSON parser: kotlinx.serialization.
- [x] Tetapkan local storage: DataStore Preferences untuk session/preferensi dan Room untuk cache/draft terstruktur.
- [x] Tetapkan mekanisme notifikasi lokal: WorkManager.
- [x] Tetapkan minimum Android SDK: Android 8.0/API 26.
- [x] Tetapkan struktur package: feature-based dengan layer ringan.
- [x] Tetapkan data demo aman dari Postman environment dan/atau data dummy yang tidak mengandung data pribadi nyata.
- [x] Dokumentasikan bahwa endpoint write `objekPajak/save` tidak dipakai di MVP.

Output tahap ini:

- [x] Keputusan stack final dicatat di README atau dokumen teknis.
- [x] Scope MVP dan non-MVP dipahami seluruh anggota tim.
- [x] Data demo aman tersedia.

Keputusan final Tahap 0:

| Area | Keputusan |
|---|---|
| Platform | Android saja untuk MVP. |
| Bahasa utama | Kotlin. |
| UI framework | Jetpack Compose. |
| Networking | Retrofit + OkHttp. |
| JSON parser | kotlinx.serialization. |
| Session dan preferensi | DataStore Preferences. |
| Cache dan draft terstruktur | Room. |
| Notifikasi lokal | WorkManager. |
| Minimum SDK | Android 8.0/API 26. |
| Struktur package | Feature-based dengan layer ringan per fitur. |
| Data demo | Data contoh Postman environment dan/atau data dummy yang aman, bukan data pribadi nyata. |
| Endpoint write | `objekPajak/save` tidak dipakai dalam alur MVP portal wajib pajak. |

## 6. Tahap 1 - Setup Project Android

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

Output tahap ini:

- [x] Aplikasi dapat build.
- [ ] Aplikasi sudah dijalankan di emulator/perangkat.
- [x] Splash atau halaman awal tersedia pada source aplikasi.
- [ ] Navigation antar placeholder sudah diuji di emulator/perangkat.

Progress Tahap 1:

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

## 7. Tahap 2 - Fondasi Domain, Local State, dan Security

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

Output tahap ini:

- [x] Model dasar siap dipakai semua fitur.
- [x] Session lokal simulatif berjalan.
- [x] Cache dan draft lokal siap dipakai.
- [x] Kebijakan masking data sensitif diterapkan sejak awal.

Progress Tahap 2:

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

## 8. Tahap 3 - API Client SIMPBB oRPC

- [x] Buat API client dengan base URL `https://simpbb.technosmart.id/api/rpc`.
- [x] Pastikan semua endpoint menggunakan `POST`.
- [x] Buat request wrapper generik `{ "json": ... }`.
- [x] Buat response wrapper generik yang membaca field `json`.
- [x] Buat interceptor/logging hanya untuk debug dan tidak mencetak data sensitif penuh.
- [x] Implementasi endpoint `objekPajak/search` dengan input `query`, `limit`.
- [x] Implementasi endpoint `objekPajak/listDetails` dengan input `kdPropinsi`, `kdDati2`, `limit`, `offset`, `search`.
- [x] Implementasi endpoint `objekPajak/getByNop` dengan `NOP_OBJECT`.
- [x] Implementasi endpoint `objekPajak/getSpptHistory` dengan `NOP_OBJECT`.
- [x] Implementasi endpoint `objekPajak/getTunggakan` dengan `NOP_OBJECT`.
- [x] Implementasi endpoint `lspop/listByNop` dengan `NOP_OBJECT`.
- [x] Implementasi endpoint `lspop/getBuilding` dengan `NOP_OBJECT` dan `noBng`.
- [x] Implementasi endpoint `lspop/listFasilitas` dengan `NOP_OBJECT` dan `noBng`.
- [x] Implementasi endpoint `sppt/listByNop` dengan `NOP_OBJECT`.
- [x] Implementasi endpoint `sppt/get` dengan `NOP_OBJECT` dan `thnPajakSppt`.
- [x] Implementasi endpoint `sppt/list` dengan `thnPajak`, `kdPropinsi`, `statusPembayaran`, `limit`, `offset` jika dibutuhkan untuk demo/eksplorasi.
- [x] Implementasi endpoint `wilayah/listPropinsi`.
- [x] Implementasi endpoint `wilayah/listDati2` dengan `kdPropinsi`.
- [x] Implementasi endpoint `wilayah/listKecamatan` dengan `kdPropinsi`, `kdDati2`.
- [x] Implementasi endpoint `wilayah/listKelurahan` dengan `kdPropinsi`, `kdDati2`, `kdKecamatan`.
- [x] Implementasi endpoint `wilayah/listBlok` dengan `kdPropinsi`, `kdDati2`, `kdKecamatan`, `kdKelurahan`.
- [x] Jangan expose endpoint `objekPajak/save` ke alur UI wajib pajak.
- [x] Buat repository yang mengubah DTO mentah menjadi domain model.
- [x] Buat fallback untuk field optional/null.

Output tahap ini:

- [x] API client dapat memanggil endpoint prioritas.
- [x] Repository siap dikonsumsi ViewModel.
- [x] Leading zero pada NOP terbukti tidak berubah.

Progress Tahap 3:

- `SimpbbApiClient` memakai Retrofit + OkHttp + kotlinx.serialization dengan base URL `https://simpbb.technosmart.id/api/rpc/`.
- Logging OkHttp hanya `BASIC` saat debug, sehingga body request/response tidak dicetak ke log.
- Semua endpoint prioritas router `objekPajak`, `lspop`, `sppt`, dan `wilayah` tersedia di `SimpbbApiService`.
- Endpoint write `objekPajak/save` tidak dibuat di service dan tidak tersedia di repository MVP.
- DTO request menjaga kode wilayah/NOP sebagai `String`, termasuk `kdKecamatan`, `kdKelurahan`, `kdBlok`, dan `noUrut`.
- `SimpbbRepository` mengembalikan `AppResult<ApiPayload>` agar ViewModel dapat menerima success, empty, dan error state secara konsisten.
- Mapping response membaca field `json`; response null/`JsonNull` diperlakukan sebagai empty state.
- `PbbKuApplication` menyiapkan `simpbbApiService` dan `simpbbRepository` secara lazy.
- Unit test wrapper oRPC memastikan `{ "json": ... }` terbentuk dan leading zero NOP tetap terserialisasi sebagai string.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` berhasil, total 6 unit test lulus.
- Verifikasi live API ringan: `POST /wilayah/listPropinsi` dengan body `{"json":{}}` berhasil dan response terbaca dari field `json`.
- Catatan: live test baru dilakukan pada endpoint wilayah yang aman; endpoint objek pajak/SPPT/LSPOP akan divalidasi saat fitur UI terkait diimplementasikan agar tidak mengekspos data demo sembarangan.

## 9. Tahap 4 - Onboarding, Login Simulatif, dan Navigasi Utama

- [x] Buat splash screen yang memeriksa session simulatif.
- [x] Buat onboarding ringkas untuk pengguna baru.
- [x] Buat form input NIK.
- [x] Validasi NIK hanya angka dan panjang 16 digit.
- [x] Buat flow kirim OTP simulatif.
- [x] Buat screen verifikasi OTP simulatif.
- [x] Tentukan OTP demo tetap atau mekanisme dummy lain yang konsisten.
- [x] Setelah OTP valid, simpan session lokal simulatif.
- [x] Setelah login, tampilkan NIK dalam bentuk masked.
- [x] Buat bottom navigation atau navigation utama untuk Beranda, Cari, Notifikasi, dan Pengaturan.
- [x] Buat logout di Pengaturan.

Output tahap ini:

- [x] Pengguna dapat login dan logout.
- [x] Session bertahan sesuai aturan lokal.
- [x] Pengguna diarahkan ke beranda setelah login.

Progress Tahap 4:

- App navigation sekarang memakai flow `splash -> onboarding -> login -> OTP -> main`.
- Splash membaca `SessionDataStore`; jika session aktif, pengguna langsung masuk ke main tabs.
- Onboarding menampilkan ringkasan fungsi PBB-Ku dan tombol `Masuk`.
- Login menerima NIK 16 digit angka dan menolak input non-angka/kurang digit.
- OTP simulatif memakai kode tetap `123456`.
- Setelah OTP valid, `SessionDataStore.saveLogin()` menyimpan session token, timestamp, dan NIK masked.
- NIK asli tidak ditampilkan setelah login; Beranda dan Pengaturan menampilkan bentuk masked seperti `34************12`.
- Main tabs tersedia untuk `Beranda`, `Cari`, `Notifikasi`, dan `Pengaturan`.
- Pengaturan memiliki tombol `Logout` yang memanggil `SessionDataStore.logout()` dan kembali ke Login.
- Unit test validasi NIK ditambahkan.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` berhasil, total 9 unit test lulus.
- Verifikasi runtime emulator headless `Pixel_6_API_35`: install debug berhasil, fresh app membuka onboarding, login NIK demo berhasil, OTP demo berhasil masuk Beranda, NIK tampil masked, dan logout kembali ke Login.

## 10. Tahap 5 - Pencarian dan Daftar Objek Pajak

- [x] Buat halaman Cari Objek Pajak.
- [x] Buat input query untuk NOP atau nama wajib pajak.
- [x] Tambahkan debounce agar request tidak berlebihan.
- [x] Panggil `objekPajak/search` dengan `query` dan `limit`.
- [x] Tampilkan hasil berupa NOP, nama wajib pajak, alamat ringkas jika tersedia, dan status fallback jika data parsial.
- [x] Buat empty state ketika hasil kosong.
- [x] Buat error state dan tombol retry.
- [x] Buat aksi memilih item hasil pencarian untuk membuka Detail Objek Pajak.
- [x] Buat halaman daftar objek pajak menggunakan `objekPajak/listDetails` jika diperlukan untuk demo.
- [x] Tambahkan pagination `limit` dan `offset` untuk daftar objek pajak jika daftar dipakai.
- [ ] Tambahkan filter wilayah sederhana jika data wilayah sudah tersedia. Catatan: filter wilayah interaktif dipindahkan ke Tahap 9 karena perlu repository dan UI referensi wilayah bertingkat.

Output tahap ini:

- [x] Pengguna dapat mencari NOP/nama WP.
- [x] Pengguna dapat memilih hasil pencarian.
- [x] Empty/error/loading state berjalan.

Progress Tahap 5:

- Halaman `Cari Objek Pajak` sekarang memakai `SearchViewModel` dan `SearchUiState`.
- Input pencarian menerima NOP atau nama wajib pajak dan menjalankan request otomatis setelah minimal 3 karakter.
- Debounce pencarian memakai coroutine delay agar request tidak dikirim pada setiap perubahan karakter secara langsung.
- Pencarian utama memanggil `objekPajak/search` dengan `query` dan `limit`.
- Hasil pencarian ditampilkan sebagai kartu berisi nama wajib pajak, NOP lengkap, dan alamat ringkas jika tersedia.
- Mapper `ObjekPajakMapper` menjaga seluruh segmen NOP sebagai `String` dan membentuk domain `ObjekPajakSummary`.
- Empty state, loading state, error message, dan tombol `Retry` tersedia di screen pencarian.
- Tombol `Daftar demo` memanggil `objekPajak/listDetails` dengan parameter demo aman dan mendukung tombol `Muat berikutnya` berbasis `limit` dan `offset`.
- Memilih hasil pencarian membuka route `object_detail/{nopDisplay}` dan menampilkan Detail Objek Pajak.
- Unit test mapper objek pajak ditambahkan untuk response `search` dan `listDetails`.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` berhasil, total 11 unit test lulus.
- Verifikasi runtime emulator headless `Pixel_6_API_35`: fresh login berhasil, tab Cari dibuka, query `WAYAN` menampilkan hasil API seperti `I WAYAN SUTARJA` dengan NOP `32.04.010.001.001.0001.0`, dan item hasil berhasil membuka Detail Objek Pajak.

## 11. Tahap 6 - Detail Objek Pajak dan Subjek Pajak

- [x] Buat halaman Detail Objek Pajak.
- [x] Panggil `objekPajak/getByNop` memakai `NOP_OBJECT`.
- [x] Tampilkan NOP lengkap.
- [x] Tambahkan tombol salin NOP jika memungkinkan.
- [x] Tampilkan alamat objek pajak.
- [x] Tampilkan luas bumi.
- [x] Tampilkan nilai sistem bumi/NJOP bumi jika tersedia.
- [x] Tampilkan jenis bumi dan status wajib pajak jika tersedia.
- [x] Tampilkan nama wajib pajak.
- [x] Tampilkan alamat wajib pajak dengan perhatian khusus pada keamanan demo.
- [x] Tampilkan status pekerjaan wajib pajak jika tersedia.
- [x] Tampilkan `Data tidak tersedia` untuk field kosong/null.
- [x] Simpan detail terakhir berhasil dimuat ke cache read-only.
- [x] Tampilkan timestamp cache jika memakai data cache.
- [x] Sediakan shortcut ke Bangunan, Histori SPPT, Tunggakan, dan Laporan Perubahan Bangunan.

Output tahap ini:

- [x] Detail NOP dapat dibuka dari hasil pencarian.
- [x] Data objek dan subjek pajak tampil rapi.
- [x] Data null/parsial tidak menyebabkan crash.

Progress Tahap 6:

- Placeholder Detail Objek Pajak diganti menjadi `ObjectDetailScreen` dengan state loading, empty, error, retry, success, dan cache.
- `ObjectDetailViewModel` memanggil `objekPajak/getByNop` memakai `NopRequest` dari domain `Nop`, sehingga seluruh segmen NOP tetap `String`.
- Mapper `toObjekPajakDetailOrNull` membaca field objek dari root response `json` dan field subjek dari `json.subjekPajak`.
- UI menampilkan NOP lengkap, alamat objek, luas bumi, nilai sistem bumi/NJOP bumi, jenis bumi, status WP, nama WP, alamat WP, dan status pekerjaan WP.
- Field kosong/null ditampilkan sebagai `Data tidak tersedia`.
- Tombol `Salin NOP` tersedia di halaman detail.
- Response detail yang berhasil dimuat disimpan ke cache read-only `object_detail:{nop}`; ketika cache dipakai, timestamp terakhir diperbarui ditampilkan.
- Shortcut Bangunan, Histori SPPT, Tunggakan, dan Laporan Perubahan Bangunan tersedia dari halaman detail dan diarahkan ke placeholder tahap berikutnya.
- Unit test mapper detail ditambahkan untuk memastikan leading zero NOP tetap aman dan field objek/subjek terbaca benar.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.

## 12. Tahap 7 - Data Bangunan dan Fasilitas LSPOP

- [x] Buat halaman Daftar Bangunan.
- [x] Panggil `lspop/listByNop`.
- [x] Tampilkan nomor bangunan, luas bangunan, jumlah lantai, jenis bangunan, dan informasi JPB jika tersedia.
- [x] Buat empty state jika NOP tidak memiliki bangunan.
- [x] Buat halaman Detail Bangunan.
- [x] Panggil `lspop/getBuilding` dengan `noBng`.
- [x] Tampilkan detail bangunan utama.
- [x] Panggil `lspop/listFasilitas` dengan `noBng`.
- [x] Tampilkan fasilitas seperti AC, lift, kolam renang, atau fasilitas lain jika tersedia.
- [x] Tampilkan fallback jika fasilitas kosong.
- [x] Hubungkan data lama bangunan ke form laporan perubahan bangunan.

Output tahap ini:

- [x] Pengguna dapat melihat daftar dan detail bangunan.
- [x] Fasilitas bangunan tampil bila tersedia.
- [x] Data bangunan dapat menjadi referensi laporan mandiri.

Progress Tahap 7:

- Route Bangunan dari Detail Objek Pajak sekarang membuka `BuildingListScreen`, bukan placeholder.
- `BuildingListViewModel` memanggil `lspop/listByNop` dan menampilkan loading, empty, error, retry, dan success state.
- `BuildingDetailScreen` dan `BuildingDetailViewModel` memanggil `lspop/getBuilding` dan `lspop/listFasilitas` untuk nomor bangunan yang dipilih.
- Request `noBng` dikirim sebagai number karena live check menunjukkan `noBng` string menghasilkan HTTP 400, sedangkan numeric diterima API.
- Mapper `LspopMapper` dibuat fleksibel untuk membaca array langsung, `rows`, atau `data`, serta beberapa alias field LSPOP umum.
- UI daftar bangunan menampilkan nomor bangunan, luas bangunan, jumlah lantai, jenis bangunan, JPB, dan NJOP bangunan bila tersedia.
- UI detail bangunan menampilkan luas, lantai, JPB, tahun dibangun/renovasi, kondisi, konstruksi, atap, dinding, lantai, langit-langit, dan NJOP bangunan bila tersedia.
- UI fasilitas menampilkan daftar fasilitas dan fallback `Fasilitas bangunan tidak tersedia` bila kosong.
- Tombol `Buat Laporan Perubahan` dari detail bangunan membawa NOP dan nomor bangunan ke placeholder laporan mandiri tahap berikutnya.
- Unit test mapper LSPOP dan serialisasi request `noBng` ditambahkan.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil, total 16 unit test lulus.
- Verifikasi lint: `./gradlew :app:lintDebug` dari MSYS2 zsh berhasil setelah atribut `android:windowLightNavigationBar` dipindahkan ke resource `values-v27` agar aman untuk minSdk 26.

## 13. Tahap 8 - Histori SPPT, Detail Tagihan, dan Tunggakan

- [x] Buat halaman Histori SPPT.
- [x] Panggil `sppt/listByNop` atau `objekPajak/getSpptHistory`.
- [x] Tampilkan daftar tahun pajak.
- [x] Tampilkan nominal tagihan dengan format rupiah.
- [x] Tampilkan status pembayaran dengan label mudah dipahami.
- [x] Tampilkan tanggal jatuh tempo dengan format Indonesia jika tersedia.
- [x] Buat halaman Detail Tagihan.
- [x] Panggil `sppt/get` dengan `thnPajakSppt`.
- [x] Tampilkan rincian tagihan: tahun pajak, status, nominal, jatuh tempo, denda jika tersedia.
- [x] Tampilkan rincian NJOP tanah, NJOP bangunan, NJOP total, NJOPTKP, tarif, dan total PBB jika tersedia.
- [x] Buat fallback jika rincian perhitungan tidak tersedia.
- [x] Buat halaman Tunggakan.
- [x] Panggil `objekPajak/getTunggakan`.
- [x] Tampilkan daftar SPPT belum lunas.
- [x] Hitung dan tampilkan total tunggakan aktif jika data nominal tersedia.
- [x] Sediakan tombol `Lihat Cara Bayar` untuk tagihan belum lunas.
- [x] Jangan sediakan tombol bayar nyata.

Output tahap ini:

- [x] Pengguna dapat melihat histori SPPT.
- [x] Pengguna dapat membuka detail tagihan per tahun.
- [x] Pengguna dapat melihat daftar dan total tunggakan.
- [x] Tidak ada klaim transaksi pembayaran nyata.

Progress Tahap 8:

- Route Histori SPPT dari Detail Objek Pajak sekarang membuka `SpptHistoryScreen`.
- Route Tunggakan dari Detail Objek Pajak sekarang membuka `TunggakanScreen`.
- `SpptHistoryViewModel` memanggil `sppt/listByNop`; jika hasil kosong, mencoba fallback `objekPajak/getSpptHistory`.
- `TaxBillDetailViewModel` memanggil `sppt/get` memakai `NOP_OBJECT` dan `thnPajakSppt`.
- `TunggakanViewModel` memanggil `objekPajak/getTunggakan` dan menghitung total tunggakan aktif jika nominal tersedia.
- Mapper `SpptMapper` dibuat toleran terhadap payload array langsung, `rows`, `data`, atau `list`, serta beberapa alias field umum untuk tahun pajak, nominal, status bayar, jatuh tempo, denda, NJOP, NJOPTKP, tarif, dan PBB terutang.
- UI histori dan tunggakan menampilkan tahun pajak, nominal rupiah, status `Lunas`/`Belum Lunas`/`Jatuh Tempo`/`Tidak Diketahui`, tanggal jatuh tempo format Indonesia, denda, loading, empty, error, dan retry state.
- UI detail tagihan menampilkan tahun pajak, status, nominal, jatuh tempo, denda, tanggal pembayaran, NJOP tanah, NJOP bangunan, NJOP total, NJOPTKP, tarif, dan PBB terutang dengan fallback `Data tidak tersedia`.
- Tombol `Lihat Cara Bayar` tersedia hanya untuk tagihan belum lunas/jatuh tempo dan diarahkan ke placeholder Informasi Pembayaran non-transaksional Tahap 10; tidak ada tombol bayar nyata.
- Live check API pada NOP demo `32.04.010.001.001.0001.0` menghasilkan `[]` untuk `sppt/listByNop` dan `objekPajak/getTunggakan`, serta `null` untuk `sppt/get` tahun 2024, sehingga empty state sudah menjadi jalur penting.
- Verifikasi build: `./gradlew :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Unit test `SpptMapperTest` ditambahkan untuk payload `rows`, array langsung, status jatuh tempo, parsing tanggal, nominal, denda, NJOP, NJOPTKP, tarif, dan fallback tahun pajak detail.

## 14. Tahap 9 - Referensi Wilayah dan Filter

- [x] Buat repository wilayah.
- [x] Ambil daftar provinsi dari `wilayah/listPropinsi`.
- [x] Ambil daftar kabupaten/kota dari `wilayah/listDati2`.
- [x] Ambil daftar kecamatan dari `wilayah/listKecamatan`.
- [x] Ambil daftar kelurahan dari `wilayah/listKelurahan`.
- [x] Ambil daftar blok dari `wilayah/listBlok`.
- [x] Buat UI filter wilayah bertingkat jika dipakai di daftar objek pajak.
- [x] Pastikan pilihan wilayah mempertahankan kode sebagai string.
- [x] Cache referensi wilayah secara lokal jika perlu.
- [x] Tambahkan empty/error state untuk tiap level wilayah.

Output tahap ini:

- [x] Filter wilayah dapat dipakai untuk membantu pencarian/daftar.
- [x] Kode wilayah tidak kehilangan leading zero.

Progress Tahap 9:

- Domain `WilayahItem` dan `WilayahLevel` ditambahkan untuk menyimpan kode wilayah sebagai `String`.
- `WilayahMapper` membaca payload wilayah dari array langsung, `rows`, `data`, `list`, atau `result`, serta mendukung alias field kode/nama untuk provinsi, kabupaten/kota, kecamatan, kelurahan, dan blok.
- `WilayahRepository` dibuat di layer repository untuk mengambil referensi wilayah dari endpoint `wilayah/listPropinsi`, `wilayah/listDati2`, `wilayah/listKecamatan`, `wilayah/listKelurahan`, dan `wilayah/listBlok`.
- Repository wilayah menyimpan cache in-memory per session aplikasi agar pilihan bertingkat yang sudah dimuat tidak selalu memanggil API ulang.
- Halaman `Cari Objek Pajak` sekarang memiliki filter wilayah bertingkat: provinsi, kabupaten/kota, kecamatan, kelurahan, dan blok.
- Daftar objek pajak memakai kode provinsi/kabupaten terpilih pada `objekPajak/listDetails`, sesuai parameter endpoint yang terdokumentasi; level kecamatan sampai blok tetap tersedia sebagai referensi bertingkat.
- UI filter menampilkan loading dan error/empty state referensi wilayah, serta tombol reset pilihan wilayah.
- Unit test `WilayahMapperTest` memastikan leading zero kode wilayah tetap dipertahankan.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 15. Tahap 10 - Informasi Pembayaran Non-Transaksional dan SSPD Prototipe

- [x] Buat halaman Informasi Pembayaran.
- [x] Tampilkan status tagihan berdasarkan data SPPT/tunggakan.
- [x] Tampilkan instruksi pembayaran umum/non-transaksional.
- [x] Beri label jelas bahwa aplikasi tidak memproses pembayaran.
- [x] Sediakan informasi kanal pembayaran sebagai konten statis/dummy jika tidak ada endpoint resmi.
- [x] Buat tampilan Bukti/SSPD Prototipe untuk tagihan lunas jika data memungkinkan.
- [x] Beri label jelas bahwa bukti/SSPD adalah prototipe jika bukan data resmi dari API.
- [x] Jangan membuat QR pembayaran nyata.
- [x] Jangan menyimpan bukti pembayaran resmi palsu tanpa label prototipe.

Output tahap ini:

- [x] Pengguna mendapat arahan pembayaran tanpa transaksi nyata.
- [x] Status pembayaran tetap bersumber dari data SPPT/tunggakan.

Progress Tahap 10:

- Placeholder Informasi Pembayaran diganti dengan `PaymentInfoScreen`.
- `PaymentInfoViewModel` memuat status tagihan dari `sppt/get`; jika detail kosong/gagal, ViewModel mencoba fallback `sppt/listByNop` untuk mencari tahun pajak yang sama.
- UI menampilkan status, nominal tagihan, jatuh tempo, denda, dan tanggal pembayaran jika tersedia dari data SPPT.
- UI menampilkan arahan pembayaran non-transaksional dan label tegas bahwa aplikasi tidak memproses pembayaran atau membuat transaksi.
- Kanal pembayaran ditampilkan sebagai konten informasi umum/demo karena tidak ada endpoint pembayaran resmi pada MVP.
- SSPD/Bukti prototipe hanya ditampilkan ketika status tagihan dari data SPPT adalah `Lunas`, dengan label `PROTOTIPE - bukan bukti pembayaran resmi`.
- Tidak ada QR pembayaran, virtual account, payment gateway, atau bukti resmi palsu yang dibuat aplikasi.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 16. Tahap 11 - Notifikasi Lokal Pengingat Jatuh Tempo

- [x] Buat pengaturan aktif/nonaktif reminder.
- [x] Ambil tanggal jatuh tempo dari data SPPT jika tersedia.
- [x] Jadwalkan reminder lokal untuk tagihan belum lunas.
- [x] Dukung skenario reminder 30 hari, 7 hari, dan 1 hari sebelum jatuh tempo.
- [x] Batalkan reminder untuk tagihan yang sudah lunas.
- [x] Buat halaman Notifikasi berisi daftar reminder.
- [x] Tampilkan status reminder aktif, terjadwal, atau tidak tersedia karena tanggal jatuh tempo kosong.
- [x] Buat fallback demo jika data jatuh tempo tidak tersedia, dengan label simulatif.
- [x] Pastikan notifikasi tidak memuat NIK penuh atau data sensitif.

Output tahap ini:

- [x] Pengguna dapat mengatur reminder.
- [x] Reminder lokal dapat didemonstrasikan.
- [x] Reminder tidak mengklaim berasal dari server Bapenda.

Progress Tahap 11:

- `PaymentReminderRepository` ditambahkan untuk menyimpan preferensi reminder dan daftar reminder lokal di DataStore.
- `PaymentReminderWorker` ditambahkan untuk menampilkan notifikasi lokal melalui WorkManager dan notification channel `Pengingat PBB`.
- Pengaturan sekarang memiliki toggle `Pengingat jatuh tempo`; pada Android 13+ toggle meminta izin `POST_NOTIFICATIONS` sebelum mengaktifkan reminder.
- Saat Histori SPPT atau Tunggakan memuat data, aplikasi menjadwalkan reminder untuk tagihan belum lunas/jatuh tempo dengan tanggal jatuh tempo tersedia.
- Reminder dijadwalkan 30 hari, 7 hari, dan 1 hari sebelum jatuh tempo menggunakan WorkManager.
- Jika tagihan sudah lunas, work reminder untuk NOP/tahun pajak terkait dibatalkan.
- Jika tanggal jatuh tempo kosong atau jadwal 30/7/1 hari sudah lewat, reminder ditampilkan sebagai `Tidak Tersedia`.
- Halaman Notifikasi sekarang menampilkan status reminder, NOP, tahun pajak, jatuh tempo, jadwal, nominal, dan catatan.
- Fallback demo simulatif tersedia saat belum ada reminder nyata, dengan label simulatif yang jelas.
- Isi notifikasi tidak memuat NIK, alamat lengkap, atau data sensitif; notifikasi hanya menyebut tagihan PBB dan tahun pajak.
- Reminder lokal tidak mengklaim berasal dari server Bapenda.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 17. Tahap 12 - Laporan Mandiri Perubahan Bangunan

- [x] Buat halaman Laporan Perubahan Bangunan.
- [x] Prefill NOP dari objek pajak yang sedang dibuka.
- [x] Prefill nomor bangunan dan data lama dari LSPOP jika tersedia.
- [x] Form memuat NOP, pilihan nomor bangunan dari daftar LSPOP terkait NOP/NIK, jenis perubahan, data lama, input baru yang tampil sesuai jenis perubahan, dan deskripsi perubahan.
- [x] Validasi luas baru berupa angka valid.
- [x] Validasi jumlah lantai baru berupa angka valid.
- [x] Validasi deskripsi perubahan tidak kosong saat submit simulasi.
- [x] Tampilkan perbandingan data lama vs data baru.
- [x] Tampilkan ringkasan sebelum submit simulasi.
- [x] Simpan laporan sebagai draft lokal.
- [x] Tampilkan status `Draft`, `Siap Diajukan`, atau `Sudah Diajukan`.
- [x] Beri peringatan bahwa perubahan data resmi memerlukan verifikasi petugas Bapenda.
- [x] Sediakan hapus draft.
- [x] Jangan memanggil `objekPajak/save` untuk laporan MVP.

Output tahap ini:

- [x] Pengguna dapat membuat draft laporan perubahan bangunan.
- [x] Laporan tidak mengubah data resmi.
- [x] Status prototipe terlihat jelas.

Progress Tahap 12:

- Placeholder `Laporan Perubahan Bangunan` diganti dengan `ReportDraftScreen`.
- Route laporan dari Detail Objek Pajak mem-prefill NOP, sedangkan route dari Detail Bangunan mem-prefill NOP dan nomor bangunan.
- `ReportDraftViewModel` memuat data lama bangunan dari `lspop/getBuilding` bila nomor bangunan tersedia, lalu mengisi luas bangunan lama, jumlah lantai lama, JPB, dan jenis bangunan lama jika response API tersedia.
- Form laporan memuat NOP, pilihan nomor bangunan dari daftar LSPOP terkait NOP/NIK, jenis perubahan, field lama/baru yang tampil sesuai jenis perubahan, dan deskripsi perubahan.
- Validasi form ditambahkan melalui `ReportDraftFormValidator`: luas baru harus angka valid, jumlah lantai baru harus angka bulat valid, dan deskripsi wajib diisi untuk ringkasan/submit simulasi.
- Pengguna dapat menyimpan draft lokal dengan status `Draft`, menyiapkan ringkasan dengan status `Siap Diajukan`, atau menandai laporan sebagai `Sudah Diajukan`.
- Ringkasan sebelum submit menampilkan perbandingan data lama vs baru dan label bahwa laporan hanya prototipe lokal, bukan perubahan data resmi SIMPBB.
- `ReportDraftRepository` dihubungkan ke `PbbKuApplication` sehingga screen laporan menyimpan, memuat ulang, dan menghapus draft dari tabel Room `report_drafts`.
- Tidak ada pemanggilan endpoint write `objekPajak/save`; fitur laporan tetap lokal/prototipe sesuai batasan MVP.
- Unit test `ReportDraftFormValidatorTest` ditambahkan untuk validasi draft, submit simulasi, luas, dan jumlah lantai.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 18. Tahap 13 - Pengaturan, Cache, dan Debug Mode

- [x] Buat halaman Pengaturan.
- [x] Tampilkan NIK masked atau identitas session simulatif.
- [x] Sediakan toggle notifikasi.
- [x] Sediakan hapus cache.
- [x] Sediakan hapus draft laporan.
- [x] Sediakan logout.
- [x] Tampilkan informasi aplikasi dan versi.
- [x] Buat debug mode hanya untuk developer jika diperlukan.
- [x] Pastikan debug log tidak memuat NIK penuh, nomor telepon, atau alamat lengkap.

Output tahap ini:

- [x] Pengguna dapat mengontrol data lokal.
- [x] Session, cache, dan draft dapat dihapus.

Progress Tahap 13:

- Halaman Pengaturan yang sudah ada dilengkapi menjadi kontrol data lokal nyata, bukan daftar item statis.
- Pengaturan tetap menampilkan NIK dalam bentuk masked dari session simulatif.
- Toggle `Pengingat jatuh tempo` tetap memakai `PaymentReminderRepository` dan meminta izin `POST_NOTIFICATIONS` pada Android 13+ sebelum mengaktifkan reminder.
- Tombol `Hapus Cache` memanggil `LocalCacheRepository.clearAll()` untuk menghapus cache read-only data terakhir.
- Tombol `Hapus Draft Laporan` memanggil `ReportDraftRepository.clearAll()` untuk menghapus semua draft laporan perubahan bangunan lokal.
- Logout tetap menghapus session melalui flow autentikasi yang sudah ada.
- Kartu informasi aplikasi menampilkan nama aplikasi dan versi dari `BuildConfig.VERSION_NAME`/`VERSION_CODE`.
- Kartu `Mode Pengembang` hanya tampil pada build debug dan berisi ringkasan aman: build debug, base URL API, logging jaringan level `BASIC`, dan catatan bahwa NIK penuh tidak disimpan/ditampilkan.
- Tidak ada penambahan log yang memuat NIK penuh, nomor telepon, alamat lengkap, atau body request/response API.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 19. Tahap 14 - UI Polish dan Usability

- [x] Pastikan semua teks berbahasa Indonesia.
- [x] Tambahkan penjelasan singkat untuk istilah NOP, NJOP, SPPT, SSPD, dan tunggakan.
- [x] Pastikan alur utama singkat: login, cari NOP, lihat tagihan, lihat detail.
- [x] Tonjolkan nominal, status, dan jatuh tempo di tampilan tagihan.
- [x] Gunakan warna status konsisten, misalnya hijau untuk lunas dan merah/oranye untuk belum lunas/jatuh tempo.
- [x] Pastikan ukuran teks nyaman di layar smartphone.
- [x] Pastikan UI portrait rapi.
- [x] Pastikan loading state tidak membuat layout melompat terlalu banyak.
- [x] Pastikan empty state informatif.
- [x] Pastikan error state memberi aksi retry.
- [x] Pastikan konten panjang dapat di-scroll.
- [x] Pastikan data dummy/prototipe punya label jelas.

Output tahap ini:

- [x] Aplikasi layak didemonstrasikan ke reviewer/pengguna uji.
- [x] Alur utama dapat dipahami tanpa penjelasan teknis panjang.

Progress Tahap 14:

- Beranda tidak lagi berupa placeholder; sekarang menampilkan session masked, shortcut `Cari Objek Pajak`, shortcut `Lihat Notifikasi`, dan glosarium ringkas istilah PBB.
- Glosarium Beranda menjelaskan NOP, NJOP, SPPT, SSPD, dan tunggakan dengan bahasa Indonesia non-teknis.
- Alur utama dipersingkat dari Beranda ke pencarian objek pajak, lalu detail objek pajak, histori SPPT/detail tagihan, tunggakan, bangunan, atau laporan perubahan.
- Komponen `PaymentStatusLabel` dan helper warna status dibuat di `ui/component` agar status `Lunas`, `Belum Lunas`, `Jatuh Tempo`, dan `Tidak Diketahui` memakai warna konsisten di halaman tagihan dan pembayaran.
- Tampilan histori/detail tagihan kini lebih menonjolkan nominal, status, dan jatuh tempo; tagihan yang masih perlu dibayar memakai penekanan warna error pada nominal dan jatuh tempo.
- Empty dan error state pencarian diperbaiki menjadi kartu informatif; error state menyediakan tombol `Coba Lagi` dekat pesan error.
- Beranda, pencarian, tagihan, pembayaran, notifikasi, pengaturan, dan laporan tetap memakai layout scroll vertikal (`LazyColumn`) agar konten panjang aman pada portrait smartphone.
- Label prototipe/dummy yang sudah ada pada pembayaran, notifikasi demo, laporan perubahan, dan mode developer dipertahankan agar data resmi API tidak tercampur dengan data simulatif.
- Polish lanjutan menambahkan design token theme untuk warna, tipografi, dan radius komponen agar tampilan lebih modern dan konsisten tanpa mengubah scope MVP.
- Komponen bersama `AppCard`, `PageHeader`, `InfoPill`, dan `StateCard` dipakai untuk merapikan hierarchy konten, empty/error state, dan label konteks pada alur utama.
- Header kontekstual ditambahkan/dirapikan pada onboarding, login, OTP, beranda, pencarian, detail objek pajak, tagihan, pembayaran, bangunan, laporan perubahan, notifikasi, dan pengaturan.
- Navigasi bawah dibuat lebih jelas dengan state warna aktif/nonaktif yang konsisten dengan theme aplikasi.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.
- Catatan: verifikasi visual langsung di emulator belum dilakukan pada tahap ini.

## 20. Tahap 15 - Pengujian Fungsional

- [x] Uji login dengan NIK valid 16 digit.
- [x] Uji login dengan NIK kurang/lebih dari 16 digit.
- [x] Uji login dengan karakter non-angka.
- [x] Uji OTP benar.
- [x] Uji OTP salah.
- [x] Uji logout.
- [x] Uji pencarian NOP/nama WP.
- [x] Uji hasil pencarian kosong.
- [x] Uji detail NOP dari hasil pencarian.
- [x] Uji leading zero pada request dan UI.
- [x] Uji daftar bangunan.
- [x] Uji detail bangunan.
- [x] Uji fasilitas bangunan.
- [x] Uji histori SPPT.
- [x] Uji detail SPPT tahun pajak.
- [x] Uji tunggakan.
- [x] Uji instruksi pembayaran non-transaksional.
- [x] Uji notifikasi lokal.
- [x] Uji form laporan perubahan bangunan.
- [x] Uji simpan draft laporan.
- [x] Uji hapus draft laporan.
- [x] Uji cache data terakhir.
- [x] Uji hapus cache.
- [x] Uji data null/parsial.
- [x] Uji API error.
- [x] Uji tidak ada koneksi internet.

Output tahap ini:

- [x] Catatan hasil pengujian tersedia.
- [x] Bug blocking demo sudah diperbaiki.

Progress Tahap 15:

- Pengujian fungsional tahap ini dieksekusi sebagai unit-level functional test suite yang terstruktur, sesuai permintaan agar dapat dijalankan cepat tanpa emulator dan tanpa ketergantungan jaringan.
- File `MvpFunctionalContractTest` ditambahkan untuk mengelompokkan skenario dengan nama test berbasis kode TC kontrak.
- Coverage unit-level mencakup validasi NIK, OTP demo, masking NIK, wrapper oRPC, leading zero NOP, request search, request detail tagihan, request detail bangunan, mapper search/detail objek pajak data parsial, mapper SPPT/detail tagihan/tunggakan, mapper bangunan/detail/fasilitas, validasi laporan perubahan bangunan, dan mapping error/no internet.
- Catatan hasil dan mapping skenario tersedia di `docs/testing/functional_unit_test_notes.md`.
- Hasil terakhir `:app:testDebugUnitTest`: 36 test lulus, 0 gagal, 0 error.
- Verifikasi build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.
- Catatan scope: interaksi UI penuh seperti tap bottom navigation, logout visual, hapus cache visual, permission dialog notifikasi, dan runtime offline tetap lebih tepat diverifikasi lewat emulator/UI test otomatis pada tahap lanjutan; tidak ditemukan bug blocking dari unit-level functional suite.

## 21. Tahap 16 - Pengujian Non-Fungsional

- [x] Beranda tampil dalam waktu wajar setelah session lokal tersedia.
- [x] Loading muncul cepat saat request pencarian dimulai.
- [x] Request pencarian memakai limit.
- [x] Daftar objek/SPPT memakai pagination jika data besar.
- [x] Request API tidak memblokir UI.
- [x] Pencarian memakai debounce.
- [x] Cache lokal tampil cepat.
- [x] Aplikasi tidak crash pada response kosong.
- [x] Aplikasi tidak crash pada field null.
- [x] Aplikasi menampilkan pesan saat internet mati.
- [x] Base URL tidak hardcoded tersebar di banyak tempat.
- [x] Repository pattern memisahkan data layer dari UI.
- [x] DTO, domain model, dan UI model dipisahkan minimal untuk data kompleks.
- [x] Format rupiah benar.
- [x] Format tanggal Indonesia benar.
- [x] Data resmi API dan data dummy/prototipe dibedakan.
- [x] Tidak ada data pribadi nyata di repository.
- [x] Tidak ada NIK penuh di log debug.

Output tahap ini:

- [x] Aplikasi memenuhi kebutuhan performa, keamanan, keandalan, maintainability, dan kualitas data MVP.

Progress Tahap 16:

- Pengujian non-fungsional tahap ini dieksekusi lewat unit test dan inspeksi konfigurasi agar dapat berjalan offline tanpa emulator.
- `SearchConfig` ditambahkan untuk membuat aturan debounce, panjang minimum query, limit halaman, dan default demo menjadi konfigurasi terpusat yang bisa diuji.
- `SimpbbApiClient.loggingLevelFor()` ditambahkan agar kebijakan logging debug dapat diuji; debug memakai level `BASIC`, bukan `BODY`, dan release memakai `NONE`.
- `NonFunctionalContractTest` ditambahkan untuk menguji limit/pagination request pencarian dan SPPT, debounce config, base URL/endpoint terpusat, formatter rupiah/tanggal/status Indonesia, mapper response kosong/null, pesan internet mati/timeout, label dummy/prototipe, dan kebijakan logging.
- Catatan hasil dan mapping kriteria non-fungsional tersedia di `docs/testing/nonfunctional_unit_test_notes.md`.
- Hasil terakhir `:app:testDebugUnitTest`: 45 test lulus, 0 gagal, 0 error.
- Verifikasi build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.
- Catatan scope: pengukuran waktu aktual Beranda, loading visual, cache latency nyata, dan profiling request non-blocking di perangkat tetap lebih tepat dilakukan lewat runtime emulator/profiling; secara implementasi Beranda membaca session lokal, request API dipanggil dari coroutine ViewModel, dan cache memakai Room lokal.

## 22. Tahap 17 - Dokumentasi, Demo, dan Finalisasi

- [x] Perbarui README dengan cara setup, build, dan run aplikasi.
- [x] Dokumentasikan stack final.
- [x] Dokumentasikan base URL dan cara mengganti environment.
- [x] Dokumentasikan endpoint yang dipakai aplikasi.
- [x] Dokumentasikan batasan MVP.
- [x] Dokumentasikan akun/data demo yang aman.
- [x] Buat skenario demo end-to-end.
- [ ] Siapkan screenshot/video demo tanpa data pribadi nyata.
- [x] Pastikan folder docs tetap rapi.
- [x] Pastikan Postman Collection dan Environment tetap sinkron dengan dokumentasi API.
- [x] Pastikan build final bisa dijalankan di emulator/perangkat demo.

Output tahap ini:

- [x] MVP siap dipresentasikan.
- [x] Reviewer dapat memahami scope, cara menjalankan, dan batasan aplikasi.

Progress Tahap 17:

- README diperbarui dengan status Tahap 17, struktur `docs/demo` dan `docs/testing`, dokumen pengujian, endpoint tambahan yang dipakai aplikasi, base URL source location, cara mengganti environment, serta data demo aman.
- Skenario demo end-to-end dibuat di `docs/demo/end_to_end_demo.md`, mencakup login NIK/OTP simulatif, pencarian, detail objek pajak, bangunan, laporan perubahan, SPPT/tagihan, pembayaran non-transaksional, tunggakan, notifikasi, pengaturan, dan logout.
- Catatan pengujian manual/runtime dibuat di `docs/testing/manual_test_notes.md`, merangkum runtime test emulator yang pernah berhasil dan sisa risiko manual.
- Folder `docs` sekarang memisahkan API, contract, diagram, demo, SRS, dan testing.
- Postman Collection dan Environment tetap berada di `docs/api/`; tidak ada perubahan endpoint baru di luar dokumentasi integrasi yang sudah ada.
- Acceptance Criteria Akhir MVP disinkronkan berdasarkan bukti tahapan sebelumnya: build/test/lint terbaru, runtime emulator historis, unit-level functional/non-functional test, dan dokumentasi demo/testing.
- Screenshot/video demo tanpa data pribadi nyata belum dibuat pada tahap ini karena membutuhkan capture visual dari emulator/perangkat dan seleksi data yang aman untuk publik.
- Verifikasi build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh berhasil.
- Verifikasi lint: `./gradlew :app:lintDebug --offline` dari MSYS2 zsh berhasil.

## 23. Acceptance Criteria Akhir MVP

- [x] Aplikasi Android berhasil build.
- [x] Aplikasi berhasil run di emulator Android.
- [x] Login NIK + OTP simulatif selesai.
- [x] Session lokal bekerja.
- [x] Logout bekerja.
- [x] Search NOP/nama WP memakai `objekPajak/search`.
- [x] Detail objek pajak memakai `objekPajak/getByNop`.
- [x] Histori SPPT memakai `sppt/listByNop` atau `objekPajak/getSpptHistory`.
- [x] Detail tagihan memakai `sppt/get`.
- [x] Tunggakan memakai `objekPajak/getTunggakan`.
- [x] Data bangunan memakai `lspop/listByNop`.
- [x] Detail/fasilitas bangunan memakai `lspop/getBuilding` dan `lspop/listFasilitas` jika tersedia.
- [x] Filter/referensi wilayah memakai endpoint `wilayah/*` jika fitur filter masuk build demo.
- [x] Informasi pembayaran bersifat non-transaksional.
- [x] Notifikasi lokal dapat didemonstrasikan atau minimal terjadwal dengan data jatuh tempo/simulasi.
- [x] Laporan perubahan bangunan tersimpan lokal sebagai draft/prototipe.
- [x] Tidak ada endpoint write resmi yang dipanggil dari alur MVP.
- [x] Semua NOP tetap menjaga leading zero.
- [x] Data cache diberi timestamp.
- [x] Data dummy/prototipe diberi label.
- [x] Error, empty, loading, retry state tersedia di alur API utama.
- [x] README memiliki instruksi menjalankan aplikasi.
- [x] Catatan pengujian manual tersedia.

## 24. Advanced Feature

- Integrasi autentikasi resmi berbasis NIK/akun wajib pajak jika Bapenda menyediakan mekanisme resmi.
- Integrasi OTP nyata melalui SMS, WhatsApp, email, atau identity provider resmi.
- Backend internal ringan untuk audit, user account, push notification, dan sinkronisasi draft, jika scope proyek diperluas.
- Push notification server-side menggunakan Firebase Cloud Messaging.
- Integrasi payment gateway resmi untuk pembayaran PBB.
- QRIS atau virtual account resmi untuk tagihan.
- Bukti pembayaran/SSPD resmi yang diambil dari endpoint resmi, bukan prototipe.
- Pengajuan keringanan/insentif PBB secara online dengan tracking status resmi.
- Submit laporan perubahan bangunan ke workflow Bapenda dengan validasi petugas.
- Upload foto pendukung laporan perubahan bangunan.
- Lampiran dokumen pendukung seperti KTP, bukti kepemilikan, atau surat kuasa.
- Geotagging objek pajak atau integrasi peta untuk melihat lokasi OP.
- Estimasi perubahan NJOP/PBB berdasarkan perubahan luas bangunan sebagai simulasi edukatif.
- Multi-NOP dashboard untuk wajib pajak yang memiliki banyak objek pajak.
- Favorit/pin NOP untuk akses cepat.
- Offline-first mode yang lebih lengkap dengan sinkronisasi saat online.
- Export ringkasan tagihan ke PDF.
- Share ringkasan tagihan melalui aplikasi lain dengan masking data sensitif.
- In-app help center untuk istilah PBB, NJOP, SPPT, SSPD, dan tunggakan.
- Chatbot FAQ PBB berbasis konten resmi Bapenda.
- Analytics penggunaan fitur untuk evaluasi usability, tanpa menyimpan data pribadi sensitif.
- Role petugas/admin untuk validasi laporan, jika panel Bapenda masuk scope lanjutan.
- Audit trail perubahan data untuk fitur laporan resmi.
- CI/CD build Android dan automated test.
- Test UI otomatis untuk alur login, search, detail NOP, SPPT, dan laporan mandiri.
