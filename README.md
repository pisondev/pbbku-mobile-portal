# PBB-Ku Mobile Portal

PBB-Ku Mobile Portal adalah proyek aplikasi mobile Android untuk portal wajib pajak PBB-P2. Aplikasi dirancang sebagai client yang membantu wajib pajak melihat informasi objek pajak, data bangunan, histori SPPT, tunggakan, dan pengingat pembayaran melalui API internal PBB-Ku yang mempertahankan kontrak oRPC SIMPBB OP API.

Proyek ini disusun untuk kebutuhan Proyek 2 Mobile Apps. Pada fase final, PBB-Ku memiliki aplikasi Android di `apps/android` dan API internal Go di `apps/api`. API internal mengadaptasi pola endpoint SIMPBB OP API dengan database demo milik proyek sendiri (SQLite untuk lokal, PostgreSQL untuk deployment), sementara aplikasi Android tetap menyimpan data lokal hanya untuk session simulatif, cache, preferensi, notifikasi lokal, dan draft fitur prototipe.

## Status Proyek

Status saat ini:

- Dokumentasi SRS tersedia di `docs/srs/SRS_PBBKu.md`.
- Dokumentasi integrasi SIMPBB OP API awal tersedia di `docs/api/`.
- API internal kompatibel SIMPBB tersedia di `apps/api/`.
- Dokumentasi API deployment/Postman tersedia di `apps/api/docs/postman/`.
- DBML untuk ERD database internal tersedia di `apps/api/docs/diagram/pbbku_internal_api.dbml`.
- Kontrak MVP v1 tersedia di `docs/contract/v1-mvp_contract.md`.
- Diagram arsitektur tersedia di `docs/diagram/`.
- Tahap 0, yaitu persiapan dan pembekuan scope teknis MVP, sudah selesai.
- Tahap 1, yaitu setup project Android, sudah dieksekusi sampai build debug berhasil.
- Tahap 2, yaitu fondasi domain, local state, dan security, sudah dieksekusi sampai unit test helper dan build debug berhasil.
- Tahap 3, yaitu API client SIMPBB oRPC, sudah dieksekusi sampai unit test, build debug, dan verifikasi live endpoint wilayah berhasil.
- Tahap 4, yaitu onboarding, login simulatif, OTP, navigation utama, dan logout, sudah dieksekusi sampai runtime test emulator berhasil.
- Tahap 5, yaitu pencarian dan daftar objek pajak, sudah dieksekusi sampai unit test, build debug, dan runtime test pencarian API berhasil.
- Tahap 6, yaitu detail objek pajak dan subjek pajak, sudah dieksekusi sampai unit test dan build debug berhasil.
- Tahap 7, yaitu data bangunan dan fasilitas LSPOP, sudah dieksekusi sampai unit test, lint debug, dan build debug berhasil.
- Tahap 8, yaitu histori SPPT, detail tagihan, dan tunggakan, sudah dieksekusi sampai unit test dan build debug berhasil.
- Tahap 9, yaitu referensi wilayah dan filter, sudah dieksekusi sampai repository wilayah, filter bertingkat, unit test, lint debug, dan build debug berhasil.
- Tahap 10, yaitu informasi pembayaran non-transaksional dan SSPD prototipe, sudah dieksekusi sampai build debug dan lint debug berhasil.
- Tahap 11, yaitu notifikasi lokal pengingat jatuh tempo, sudah dieksekusi sampai toggle reminder, WorkManager, halaman notifikasi, build debug, dan lint debug berhasil.
- Tahap 12, yaitu laporan mandiri perubahan bangunan, sudah dieksekusi sampai form draft lokal/prototipe, validasi, ringkasan simulasi, hapus draft, unit test, build debug, dan lint debug berhasil.
- Tahap 13, yaitu pengaturan, cache, dan debug mode, sudah dieksekusi sampai aksi hapus cache, hapus draft laporan, info versi aplikasi, mode developer debug-only, build debug, dan lint debug berhasil.
- Tahap 14, yaitu UI polish dan usability, sudah dieksekusi sampai Beranda operasional, glosarium istilah PBB, status pembayaran konsisten, penekanan nominal/jatuh tempo, empty/error state pencarian, build debug, dan lint debug berhasil.
- Tahap 15, yaitu pengujian fungsional, sudah dieksekusi dalam bentuk unit-level functional test suite terstruktur sampai 36 unit test lulus, build debug, dan lint debug berhasil.
- Tahap 16, yaitu pengujian non-fungsional, sudah dieksekusi dalam bentuk unit test dan inspeksi konfigurasi sampai 45 unit test lulus, build debug, dan lint debug berhasil.
- Tahap 17, yaitu dokumentasi, demo, dan finalisasi, sudah dieksekusi pada bagian yang bisa dilakukan tanpa emulator: README final, skenario demo end-to-end, catatan pengujian manual/runtime, acceptance criteria akhir, build debug, unit test, dan lint debug.
- Source Android tersedia di `apps/android/` dengan Gradle Wrapper dan module `app`.
- APK debug berhasil dibuat di `apps/android/app/build/outputs/apk/debug/app-debug.apk`.
- Runtime test dasar berhasil di emulator `Pixel_6_API_35`: onboarding, login NIK demo, OTP `123456`, Beranda dengan NIK masked, logout kembali ke Login, pencarian `WAYAN`, hasil objek pajak tampil, dan hasil pertama membuka Detail Objek Pajak.

## Ruang Lingkup MVP

Fitur utama yang dirancang untuk MVP:

- Login atau onboarding simulatif menggunakan NIK dan OTP.
- Pencarian NOP atau nama wajib pajak.
- Detail objek pajak dan subjek pajak.
- Daftar dan detail bangunan/LSPOP.
- Histori SPPT dan detail tagihan per tahun pajak.
- Daftar tunggakan.
- Filter referensi wilayah.
- Informasi pembayaran non-transaksional.
- Notifikasi lokal untuk pengingat jatuh tempo.
- Form pelaporan mandiri perubahan bangunan sebagai prototipe, tanpa mengubah data resmi.

Fitur yang tidak termasuk MVP:

- Payment gateway atau transaksi pembayaran nyata.
- Update langsung data resmi SPOP/LSPOP.
- Panel admin Bapenda.
- Integrasi write resmi ke SIMPBB core system dari aplikasi wajib pajak.

## Arsitektur

```text
Wajib Pajak
  -> Android App PBB-Ku
  -> PBB-Ku Internal API (/api/rpc kompatibel SIMPBB)
  -> Database PBB-Ku (SQLite lokal / PostgreSQL deployment)
  -> Data demo objek pajak, subjek pajak, bangunan, SPPT, tunggakan
```

Diagram arsitektur lengkap dapat dilihat di `docs/diagram/2-1_arsitektur-sistem-pbbku.png`.

## Struktur Repository

```text
pbbku-mobile-portal/
+-- apps/
|   +-- android/
|   |   +-- app/
|   |   +-- gradle/
|   |   +-- build.gradle.kts
|   |   +-- gradle.properties
|   |   +-- gradlew
|   |   +-- gradlew.bat
|   |   `-- settings.gradle.kts
|   `-- api/
|       +-- cmd/
|       +-- docs/
|       |   +-- diagram/
|       |   `-- postman/
|       +-- internal/
|       +-- migrations/
|       +-- seeds/
|       `-- tests/
+-- docs/
|   +-- api/
|   |   +-- SIMPBB_OP_API.md
|   |   +-- SIMPBB_OP_API.postman_collection.json
|   |   `-- SIMPBB_OP_API.postman_environment.json
|   +-- contract/
|   |   `-- v1-mvp_contract.md
|   +-- diagram/
|   |   `-- 2-1_arsitektur-sistem-pbbku.png
|   +-- demo/
|   |   `-- end_to_end_demo.md
|   +-- srs/
|   |   `-- SRS_PBBKu.md
|   `-- testing/
|       +-- functional_unit_test_notes.md
|       +-- manual_test_notes.md
|       `-- nonfunctional_unit_test_notes.md
+-- .env
+-- .gitignore
`-- README.md
```

## Dokumentasi

Dokumen utama proyek:

- `docs/srs/SRS_PBBKu.md`: Software Requirements Specification berdasarkan IEEE 830-1998.
- `docs/api/SIMPBB_OP_API.md`: ringkasan endpoint SIMPBB OP API untuk integrasi aplikasi.
- `docs/api/SIMPBB_OP_API.postman_collection.json`: Postman Collection untuk eksplorasi endpoint.
- `docs/api/SIMPBB_OP_API.postman_environment.json`: Postman Environment berisi variable base URL dan contoh parameter.
- `apps/api/README.md`: dokumentasi API internal, mode database, security, REST/oRPC endpoint, dan deployment.
- `apps/api/docs/postman/`: Postman Collection dan Environment untuk deployment API internal PBB-Ku.
- `apps/api/docs/diagram/pbbku_internal_api.dbml`: DBML untuk generate ERD di dbdiagram.io.
- `docs/contract/v1-mvp_contract.md`: kontrak kerja MVP, tahapan todo, acceptance criteria, dan advanced feature.
- `docs/demo/end_to_end_demo.md`: skenario demo end-to-end yang aman untuk reviewer.
- `docs/testing/functional_unit_test_notes.md`: catatan pengujian fungsional berbasis unit test.
- `docs/testing/nonfunctional_unit_test_notes.md`: catatan pengujian non-fungsional berbasis unit test dan inspeksi konfigurasi.
- `docs/testing/manual_test_notes.md`: ringkasan runtime test/manual test yang pernah berhasil dan sisa risiko manual.
- `docs/testing/testing_coverage_audit.md`: audit coverage test final dan batas testing UI otomatis.

## PBB-Ku Internal API dan Kontrak SIMPBB

API internal berada di `apps/api` dan dibuat agar Android tetap memakai kontrak oRPC yang sama dengan SIMPBB OP API. Endpoint utama tersedia di prefix:

```text
Production   : https://pbbku-api.tierratie.com/api/rpc/
Local Android: http://10.0.2.2:8080/api/rpc/
Local host   : http://localhost:8080/api/rpc/
```

Mode database:

- SQLite: default lokal untuk pengembangan cepat dan demo offline.
- PostgreSQL: mode deployment/VPS.

Dokumentasi API internal lengkap tersedia di `apps/api/README.md`.

## SIMPBB OP API Acuan

Konfigurasi dasar API:

```text
Base URL     : https://simpbb.technosmart.id/api/rpc
Protocol     : oRPC over HTTP POST
Content-Type : application/json
Auth         : PUBLIC untuk router terdokumentasi
```

Di source Android, base URL terpusat di:

```text
apps/android/app/src/main/java/id/pbbku/mobileportal/data/api/SimpbbApiConfig.kt
```

Untuk mengganti environment API, pakai Gradle property `PBBKU_API_BASE_URL` atau gunakan overload `SimpbbApiClient.create(baseUrl = ...)` pada wiring aplikasi/test.

Semua request menggunakan method `POST` dan body JSON dengan wrapper:

```json
{
  "json": {
    "param1": "value"
  }
}
```

Endpoint prioritas untuk aplikasi:

- `objekPajak/search`
- `objekPajak/listDetails`
- `objekPajak/getByNop`
- `objekPajak/getSpptHistory`
- `objekPajak/getTunggakan`
- `lspop/listByNop`
- `lspop/getBuilding`
- `lspop/listFasilitas`
- `sppt/listByNop`
- `sppt/get`
- `sppt/list`
- `wilayah/listPropinsi`
- `wilayah/listDati2`
- `wilayah/listKecamatan`
- `wilayah/listKelurahan`
- `wilayah/listBlok`

Catatan integrasi penting:

- Segmen NOP harus diperlakukan sebagai string agar leading zero tidak hilang.
- Response utama dibaca dari field `json`.
- Endpoint write seperti `objekPajak/save` tidak diprioritaskan untuk portal wajib pajak.

## Keputusan Teknologi Android MVP

Stack final untuk MVP:

- Platform: Android.
- Bahasa utama: Kotlin.
- UI framework: Jetpack Compose.
- Networking: Retrofit + OkHttp.
- JSON parser: kotlinx.serialization.
- Session dan preferensi lokal: DataStore Preferences.
- Cache dan draft terstruktur: Room.
- Notifikasi lokal: WorkManager.
- Minimum SDK: Android 8.0/API 26.
- Struktur package: feature-based dengan layer ringan.

Keputusan scope penting:

- Data demo/runtime aplikasi berasal dari API internal PBB-Ku yang mengadaptasi kontrak SIMPBB OP API.
- Data lokal hanya untuk session simulatif, preferensi, cache read-only, notifikasi lokal, dan draft/prototipe.
- Endpoint write seperti `objekPajak/save` tidak digunakan dalam alur MVP portal wajib pajak.
- Data demo menggunakan contoh Postman environment dan/atau data dummy aman, bukan data pribadi nyata.

## Menjalankan Proyek

Prasyarat lokal:

- JDK 17.
- MSYS2 zsh dengan konfigurasi Android di `~/.zshrc`.
- Android SDK utama di `C:\Android\Sdk` atau `/c/Android/Sdk`.
- Android SDK platform `android-35` dan build-tools `35.0.0`.
- Android Studio atau emulator/perangkat Android untuk runtime test.

Build debug dari MSYS2 zsh:

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:assembleDebug
```

Unit test dan build debug:

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Ringkasan semua automated test untuk screenshot laporan:

```zsh
zsh scripts/test.sh
```

Script tersebut menjalankan API unit/integration/functional test, Android JVM unit/functional/nonfunctional contract test, dan Android lint dengan output ringkas berwarna. Untuk menyiapkan emulator dan membuka aplikasi sebagai manual E2E prep, jalankan:

```zsh
zsh scripts/test.sh --e2e
```

APK debug akan tersedia di:

```text
apps/android/app/build/outputs/apk/debug/app-debug.apk
```

Untuk menjalankan di emulator/perangkat setelah device terdeteksi:

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:installDebug
```

Untuk manual end-to-end testing dengan satu perintah dari root repo:

```zsh
source ~/.zshrc
bash scripts/run_android_e2e.sh
```

Default AVD adalah `Pixel_6_API_35`. Jika perlu memakai AVD lain:

```zsh
source ~/.zshrc
AVD_NAME=Nama_AVD bash scripts/run_android_e2e.sh
```

Status verifikasi saat ini:

- Environment zsh terverifikasi memakai `JAVA_HOME=/c/Program Files/Eclipse Adoptium/jdk-17.0.14.7-hotspot`.
- Environment zsh terverifikasi memakai `ANDROID_HOME=/c/Android/Sdk`.
- `./gradlew :app:assembleDebug` dari MSYS2 zsh sudah berhasil.
- `./gradlew :app:assembleDebug --offline` dari MSYS2 zsh sudah berhasil.
- `./gradlew :app:testDebugUnitTest :app:assembleDebug --offline` dari MSYS2 zsh sudah berhasil.
- `./gradlew :app:lintDebug` dari MSYS2 zsh sudah berhasil.
- Unit/JVM test Android saat ini: 46 test lulus, mencakup parser NOP, masking NIK, validasi NIK, OTP demo, wrapper oRPC, mapper objek pajak, mapper detail objek pajak, mapper LSPOP, mapper SPPT, mapper wilayah, request detail bangunan, validasi form laporan perubahan bangunan, suite fungsional MVP, dan suite non-fungsional MVP.
- API test saat ini: unit, integration SQLite, dan functional/edge-case suite lulus; PostgreSQL integration bersifat opsional dan skip jika `PBBKU_TEST_POSTGRES_URL` belum di-set.
- Live API check ringan berhasil untuk `POST /wilayah/listPropinsi` dengan body `{"json":{}}`.
- File lokal `apps/android/local.properties` mengarah ke `C:\Android\Sdk` dan tidak di-commit karena sudah di-ignore.
- Runtime test dasar berhasil di emulator headless `Pixel_6_API_35`: install debug, fresh onboarding, login NIK demo `3404123456789012`, OTP demo `123456`, Beranda dengan masked NIK `34************12`, dan logout kembali ke Login.
- Runtime test pencarian berhasil di emulator headless `Pixel_6_API_35`: tab Cari, query `WAYAN`, hasil dari `objekPajak/search` tampil, termasuk `I WAYAN SUTARJA` dengan NOP `32.04.010.001.001.0001.0`, lalu item hasil membuka Detail Objek Pajak.

Untuk eksplorasi API acuan SIMPBB, import file berikut ke Postman:

1. `docs/api/SIMPBB_OP_API.postman_collection.json`
2. `docs/api/SIMPBB_OP_API.postman_environment.json`

Untuk eksplorasi API deployment PBB-Ku, import file berikut:

1. `apps/api/docs/postman/PBBKu_Deployment_API.postman_collection.json`
2. `apps/api/docs/postman/PBBKu_Deployment_API.postman_environment.json`

## Keamanan dan Data Demo

- Jangan gunakan data pribadi nyata untuk demonstrasi publik.
- Jangan commit secret, token, atau credential ke repository.
- File `.env` disiapkan untuk konfigurasi lokal dan sudah masuk `.gitignore`.
- NIK pada UI harus disamarkan setelah login.
- Log debug tidak boleh memuat NIK penuh atau data sensitif.
- NIK demo aman untuk alur login simulatif: `3404123456789012`.
- OTP demo tetap: `123456`.
- Query demo yang pernah berhasil untuk runtime test: `WAYAN`.
- Skenario demo lengkap tersedia di `docs/demo/end_to_end_demo.md`.
