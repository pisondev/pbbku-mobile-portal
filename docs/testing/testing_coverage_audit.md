# Audit Coverage Testing PBB-Ku

Tanggal audit: 5 Juni 2026

## Ringkasan

Automated testing utama sudah tersedia dan dapat dijalankan dari satu script:

```zsh
zsh scripts/test.sh
```

Hasil verifikasi terakhir:

| Area | Suite | Status |
|---|---|---|
| API unit | `go test ./tests/unit -count=1` | Pass |
| API integration | `go test ./tests/integration -count=1` | Pass untuk SQLite; PostgreSQL optional skip jika `PBBKU_TEST_POSTGRES_URL` kosong |
| API functional | `go test ./tests/functional -count=1` | Pass |
| Android JVM | `:app:testDebugUnitTest --offline` | 46 pass, 0 fail |
| Android lint | `:app:lintDebug --offline` | Pass |

## Cakupan yang Sudah Ada

- Unit test Android mencakup parser NOP, validasi/masking NIK, wrapper oRPC, mapper wilayah/objek pajak/LSPOP/SPPT, dan validasi form laporan.
- Functional contract test Android mencakup alur MVP pada level logic/contract tanpa emulator.
- Nonfunctional contract test Android mencakup konfigurasi debounce, limit, formatter, error message, label prototipe, dan kebijakan logging.
- API integration test mencakup migrasi dan seed SQLite serta opsi PostgreSQL jika DSN test tersedia.
- API functional test mencakup health, wilayah, pencarian objek pajak, detail NOP, bangunan, fasilitas, SPPT, tunggakan, helper, REST read-only, security header, CORS, body limit, admin key, transaksi save, dan concurrency upsert.

## Batas Coverage

Testing otomatis sudah memadai untuk bukti final MVP dan screenshot laporan. Namun, UI instrumentation test Android penuh belum dibuat. Alur emulator seperti onboarding, login, pencarian, detail objek, dan logout masih dicatat sebagai manual/runtime test di `manual_test_notes.md` dan dapat disiapkan lewat:

```zsh
zsh scripts/test.sh --e2e
```

Mode `--e2e` menyiapkan emulator/install/open app, tetapi validasi visual tetap manual.
