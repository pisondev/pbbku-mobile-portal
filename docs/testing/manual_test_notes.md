# Catatan Pengujian Manual dan Runtime MVP

Dokumen ini merangkum bukti pengujian manual/runtime yang sudah tercatat selama progres MVP. Pengujian manual terakhir tidak dijalankan ulang pada Tahap 17 karena tahap ini fokus pada dokumentasi final dan acceptance criteria.

## Runtime Test yang Pernah Berhasil

Environment:

- Emulator: `Pixel_6_API_35` headless.
- APK: debug build dari module `apps/android/app`.
- NIK demo: `3404123456789012`.
- OTP demo: `123456`.
- Query pencarian: `WAYAN`.

Hasil yang pernah berhasil:

- Install debug ke emulator.
- Fresh app membuka onboarding.
- Login NIK demo berhasil.
- OTP demo berhasil masuk Beranda.
- NIK tampil masked sebagai `34************12`.
- Logout kembali ke Login.
- Tab `Cari` dapat dibuka.
- Query `WAYAN` memanggil API dan menampilkan hasil.
- Hasil pencarian menampilkan contoh `I WAYAN SUTARJA` dengan NOP `32.04.010.001.001.0001.0`.
- Item hasil pencarian dapat membuka Detail Objek Pajak.

## Pengujian Otomatis Pendukung

Pengujian otomatis terakhir:

- `:app:testDebugUnitTest`: 46 test lulus, 0 gagal, 0 error.
- `:app:assembleDebug`: berhasil.
- `:app:lintDebug`: berhasil.

Catatan detail:

- Pengujian fungsional unit-level tersedia di `docs/testing/functional_unit_test_notes.md`.
- Pengujian non-fungsional unit-level tersedia di `docs/testing/nonfunctional_unit_test_notes.md`.

## Sisa Risiko Manual

Skenario berikut tetap ideal diuji ulang langsung di emulator/perangkat sebelum presentasi final:

- Permission dialog notifikasi Android 13+.
- Visual scroll dan layout pada beberapa ukuran layar.
- Hapus cache dan hapus draft dari UI Pengaturan.
- Reminder WorkManager pada waktu nyata.
- Rekaman screenshot/video demo tanpa data pribadi nyata.
