# Catatan Pengujian Non-Fungsional Tahap 16

Pengujian Tahap 16 dijalankan melalui unit test dan inspeksi konfigurasi agar aspek performa dasar, keandalan parsing, keamanan log, maintainability, dan kualitas data dapat diverifikasi tanpa emulator atau koneksi jaringan.

## Perintah Verifikasi

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:testDebugUnitTest :app:assembleDebug --offline
./gradlew :app:lintDebug --offline
```

## Hasil Terakhir

- Unit test: 45 test lulus, 0 gagal, 0 error.
- Build debug: berhasil.
- Lint debug: berhasil.
- Runtime emulator tidak dijalankan ulang pada tahap ini.

## Mapping Kriteria Non-Fungsional

| Kriteria | Bukti |
|---|---|
| Request pencarian memakai limit | `NonFunctionalContractTest.nf_searchAndListRequestsUseLimitAndPagination` |
| Daftar objek/SPPT memakai pagination | `NonFunctionalContractTest.nf_searchAndListRequestsUseLimitAndPagination` dan `nf_spptListRequestSupportsPaginationForLargeData` |
| Pencarian memakai debounce | `NonFunctionalContractTest.nf_searchUsesDebounceMinimumLengthAndBoundedLimit` |
| Aplikasi tidak crash pada response kosong/null | `NonFunctionalContractTest.nf_emptyAndNullPayloadsDoNotCrashMappers` |
| Pesan saat internet mati | `NonFunctionalContractTest.nf_internetUnavailableMessageIsUserReadable` |
| Base URL tidak tersebar | `NonFunctionalContractTest.nf_baseUrlAndEndpointsAreCentralizedInConfig` |
| Format rupiah/tanggal/status benar | `NonFunctionalContractTest.nf_rupiahDateAndStatusFormattingUseIndonesianDisplay` |
| Data resmi dan dummy/prototipe dibedakan | `NonFunctionalContractTest.nf_demoAndPrototypeLabelsStayExplicitInUserVisibleText` |
| Tidak ada body request/response di log debug | `NonFunctionalContractTest.nf_debugLoggingNeverUsesBodyLevel` |

## Catatan Scope

- Kriteria seperti waktu tampil Beranda, loading muncul cepat secara visual, request tidak memblokir UI secara runtime, cache lokal tampil cepat, dan usability di perangkat nyata tetap lebih tepat diverifikasi dengan runtime emulator, profiling, atau UI test otomatis.
- Secara implementasi, request API dipanggil dari ViewModel menggunakan coroutine `viewModelScope`, base URL berada di `SimpbbApiConfig`, dan logging OkHttp debug berada di level `BASIC`, bukan `BODY`.
