# Manajemen Susunan Folder Proyek

Dokumen ini menjelaskan struktur folder repository PBB-Ku dan fungsi masing-masing bagian agar mudah dipresentasikan dan dipelihara.

## Struktur Root

```text
pbbku-mobile-portal/
+-- apps/
+-- docs/
+-- presentation asset/
+-- scripts/
+-- .env
+-- .gitignore
+-- README.md
```

## `apps/`

Folder `apps/` berisi source aplikasi utama.

```text
apps/
+-- android/
+-- api/
```

### `apps/android/`

Berisi proyek Android Kotlin.

Bagian penting:

- `app/build.gradle.kts`: konfigurasi module Android app.
- `settings.gradle.kts`: konfigurasi project Gradle.
- `gradle/libs.versions.toml`: daftar versi dependency.
- `gradlew` dan `gradlew.bat`: Gradle Wrapper.
- `app/src/main/`: source aplikasi utama.
- `app/src/test/`: unit test.

### `apps/api/`

Berisi backend/API pendukung dan test API lokal. Pada MVP mobile, aplikasi Android tetap diposisikan sebagai client untuk SIMPBB OP API.

## Struktur Source Android

```text
apps/android/app/src/main/java/id/pbbku/mobileportal/
+-- core/
+-- data/
+-- domain/
+-- feature/
+-- ui/
+-- MainActivity.kt
+-- PbbKuApplication.kt
```

## `core/`

Berisi utility lintas fitur.

Contoh:

- Formatting tanggal dan rupiah.
- Validasi dan masking NIK.
- Result wrapper.
- Error model.

Folder ini tidak boleh bergantung pada UI screen tertentu.

## `domain/`

Berisi model domain yang dipakai aplikasi.

Contoh:

- `Nop`
- `ObjekPajakDetail`
- `TaxBillSummary`
- `BuildingDetail`
- `PaymentReminder`

Model domain dibuat agar UI tidak langsung bergantung pada format response API.

## `data/`

Berisi implementasi akses data.

Subfolder utama:

- `api/`: Retrofit service dan konfigurasi client.
- `dto/`: bentuk data request/response API.
- `mapper/`: konversi DTO ke model domain.
- `repository/`: orkestrasi API, cache, dan sumber data.
- `local/`: Room database, DAO, entity.
- `session/`: session simulatif via DataStore.
- `reminder/`: WorkManager dan repository reminder.
- `demo/`: data demo aman untuk pembatasan akses NIK.

## `feature/`

Berisi screen dan ViewModel per fitur.

Contoh:

- `auth/`: splash, onboarding, login, OTP.
- `home/`: beranda.
- `search/`: pencarian objek pajak.
- `objectdetail/`: detail objek pajak.
- `building/`: daftar dan detail bangunan.
- `sppt/`: histori SPPT, detail tagihan, tunggakan.
- `payment/`: informasi pembayaran.
- `report/`: laporan perubahan bangunan.
- `notifications/`: reminder lokal.
- `settings/`: pengaturan.
- `profile/`: profil wajib pajak.

Pemisahan per fitur membuat perubahan UI lebih mudah dilacak.

## `ui/`

Berisi komponen UI bersama.

Subfolder/komponen:

- `component/`: card, header, shortcut, pill, state card.
- `navigation/`: route dan navigation graph.
- `theme/`: warna dan tema Material.
- `tutorial/`: overlay bantuan halaman.

Komponen di `ui/` boleh dipakai banyak fitur.

## `docs/`

Folder `docs/` berisi dokumentasi proyek yang lebih formal.

```text
docs/
+-- api/
+-- contract/
+-- demo/
+-- diagram/
+-- srs/
+-- testing/
```

Isi utama:

- Dokumentasi API SIMPBB OP.
- Contract MVP.
- SRS.
- Skenario demo end-to-end.
- Catatan testing fungsional, non-fungsional, dan manual.

## `presentation asset/`

Folder ini disiapkan untuk bahan presentasi.

Isi saat ini:

- `tech_stack_dan_alur_sistem.md`
- `konsep_sistem_pajak_pbb.md`
- `manajemen_susunan_folder.md`

Folder ini dapat diisi asset baru secara manual, seperti screenshot, diagram, atau bahan presentasi lain.

## `scripts/`

Berisi script bantu.

Contoh:

- `run_android_e2e.sh`: menyiapkan emulator/device, install APK debug, lalu membuka aplikasi.

## Aturan Praktis Pengelolaan Folder

- Source Android utama masuk ke `apps/android`.
- Dokumentasi formal masuk ke `docs`.
- Materi presentasi masuk ke `presentation asset`.
- Script otomasi masuk ke `scripts`.
- Jangan commit secret atau credential.
- File lokal seperti `.env` dan `local.properties` tidak dipakai sebagai dokumentasi publik.
- Perubahan UI per fitur sebaiknya tetap berada di folder `feature/<nama-fitur>`.
- Komponen yang dipakai berulang sebaiknya dipindahkan ke `ui/component`.
