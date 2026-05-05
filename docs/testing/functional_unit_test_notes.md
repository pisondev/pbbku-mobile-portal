# Catatan Pengujian Fungsional Tahap 15

Pengujian Tahap 15 dijalankan sebagai unit-level functional test suite agar skenario MVP dapat diverifikasi cepat dan konsisten tanpa bergantung pada emulator atau koneksi SIMPBB API.

## Perintah Verifikasi

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:testDebugUnitTest :app:assembleDebug --offline
./gradlew :app:lintDebug --offline
```

## Hasil Terakhir

- Unit test: 36 test lulus, 0 gagal, 0 error.
- Build debug: berhasil.
- Lint debug: berhasil.
- Runtime emulator tidak dijalankan ulang pada tahap ini.

## Mapping Skenario Kontrak

| Skenario | Bukti Unit Test |
|---|---|
| Login NIK valid 16 digit | `MvpFunctionalContractTest.tc001_tc004_loginNikAndOtpRules_areDeterministicForDemo` |
| NIK kurang/lebih dari 16 digit | `MvpFunctionalContractTest.tc002_tc003_invalidNikInputs_areRejected` |
| NIK berisi karakter non-angka | `MvpFunctionalContractTest.tc002_tc003_invalidNikInputs_areRejected` |
| OTP demo benar | `MvpFunctionalContractTest.tc001_tc004_loginNikAndOtpRules_areDeterministicForDemo` |
| Masking NIK | `MvpFunctionalContractTest.tc015_maskingNik_neverReturnsFullNik` |
| Search NOP/nama WP | `MvpFunctionalContractTest.tc003_searchRequest_usesJsonWrapperQueryAndLimit` dan `tc003_tc004_searchAndDetail_mapsPartialDataWithoutCrash` |
| Detail NOP dari hasil pencarian | `MvpFunctionalContractTest.tc003_tc004_searchAndDetail_mapsPartialDataWithoutCrash` |
| Leading zero pada request dan UI | `MvpFunctionalContractTest.tc005_tc016_nopLeadingZeroAndApiWrapper_arePreserved` |
| Daftar/detail/fasilitas bangunan | `MvpFunctionalContractTest.tc009_tc010_tc011_buildingListDetailAndFacilities_areMapped` |
| Histori SPPT/detail SPPT/tunggakan | `MvpFunctionalContractTest.tc006_tc007_tc008_spptHistoryDetailAndTunggakanStatus_areMapped` |
| Informasi pembayaran non-transaksional | `MvpFunctionalContractTest.tc017_paymentInfoRequest_keepsSpptYearAndDoesNotUseWriteEndpointPayload` |
| Form laporan perubahan bangunan | `MvpFunctionalContractTest.tc014_tc020_reportDraftValidation_allowsDraftButRequiresDescriptionForSimulation` |
| Data null/parsial | `MvpFunctionalContractTest.tc003_tc004_searchAndDetail_mapsPartialDataWithoutCrash` |
| API error dan tidak ada koneksi internet | `MvpFunctionalContractTest.tc023_tc024_apiErrorAndNoConnection_mapToUserReadableMessages` |

## Catatan Scope

- Pengujian ini menutup aturan domain, request wrapper oRPC, mapper response, validasi form, status pembayaran, dan error mapping.
- Pengujian interaksi UI penuh seperti tap bottom navigation, logout visual, hapus cache visual, dan permission dialog notifikasi tetap lebih tepat diverifikasi melalui runtime emulator atau UI test otomatis pada tahap lanjutan.
