# PBB-Ku Mobile Portal

## Progress 14 Mei 2026

PBB-Ku Mobile Portal adalah proyek aplikasi mobile Android untuk portal wajib pajak PBB-P2. Aplikasi dirancang sebagai client yang membantu wajib pajak melihat informasi objek pajak, data bangunan, histori SPPT, tunggakan, dan pengingat pembayaran dengan mengambil data dari SIMPBB OP API.

**Pengembang:**

- Mikail Achmad | 24/542370/PA/23026
- Pison Golda Mountera | 24/543770/PA/23107
- Muhammad Rayyan Buna Satria | 24/543564/PA/23096

ugm.ac.id

Merakyat, Mandiri, Berkelanjutan  
Inclusive, Self-Reliant, Sustainable

---

# Tahap 3 - API Client SIMPBB oRPC

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
- Verifikasi live API ringan: `POST /wilayah/listPropinsi` dengan body `{ "json": {} }` berhasil dan response terbaca dari field `json`.
- Catatan: live test baru dilakukan pada endpoint wilayah yang aman; endpoint objek pajak/SPPT/LSPOP akan divalidasi saat fitur UI terkait diimplementasikan agar tidak mengekspos data demo sembarangan.

ugm.ac.id

Merakyat, Mandiri, Berkelanjutan | Inclusive, Self-Reliant, Sustainable

---

# Tahap 4 - Onboarding, Login Simulatif, dan Navigasi Utama

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

ugm.ac.id

Merakyat, Mandiri, Berkelanjutan | Inclusive, Self-Reliant, Sustainable

---

# Progress Tahap 5

- Halaman Cari Objek Pajak sekarang memakai SearchViewModel dan SearchUiState.
- Input pencarian menerima NOP atau nama wajib pajak dan menjalankan request otomatis setelah minimal 3 karakter.
- Debounce pencarian memakai coroutine delay agar request tidak dikirim pada setiap perubahan karakter secara langsung.
- Pencarian utama memanggil objekPajak/search dengan query dan limit.
- Hasil pencarian ditampilkan sebagai kartu berisi nama wajib pajak, NOP lengkap, dan alamat ringkas jika tersedia.
- Mapper ObjekPajakMapper menjaga seluruh segmen NOP sebagai String dan membentuk domain ObjekPajakSummary.
- Empty state, loading state, error message, dan tombol Retry tersedia di screen pencarian.
- Tombol Daftar demo memanggil objekPajak/listDetails dengan parameter demo aman dan mendukung tombol Muat berikutnya berbasis limit dan offset.
- Memilih hasil pencarian membuka route object_detail/{nopDisplay} dan menampilkan Detail Objek Pajak.
- Unit test mapper objek pajak ditambahkan untuk response search dan listDetails.
- Verifikasi unit/build: `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` berhasil, total 11 unit test lulus.
- Verifikasi runtime emulator headless `Pixel_6_API_35`: fresh login berhasil, tab Cari dibuka, query BUDI menampilkan hasil API seperti BUDI EMBER BOCOR dengan NOP 32.04.010.001.001.0001.0, dan item hasil berhasil membuka Detail Objek Pajak.

ugm.ac.id

Merakyat, Mandiri, Berkelanjutan | Inclusive, Self-Reliant, Sustainable

---

# TERIMA KASIH

ugm.ac.id

Merakyat, Mandiri, Berkelanjutan  
Inclusive, Self-Reliant, Sustainable
