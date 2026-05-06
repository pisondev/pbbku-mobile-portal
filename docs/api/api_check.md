# Laporan Hasil Testing API Eksternal SIMPBB

Tanggal pengujian: 2026-05-06  

Laporan ini merangkum hasil pengujian API eksternal SIMPBB sebagai bahan evaluasi integrasi aplikasi dan bahan diskusi dengan pemilik API.

## Overview API

Keterangan status:

- `✓` berarti endpoint sudah terbukti menghasilkan data sesuai kebutuhan pada skenario yang diuji dan belum membutuhkan pertanyaan lanjutan.
- `✗` berarti endpoint masih perlu dikonfirmasi ke pemilik API. Ini tidak selalu berarti endpoint rusak; bisa juga karena response kosong, cakupan data belum jelas, atau perlu contoh data tambahan.

| Status | Endpoint | Keputusan | Catatan singkat |
|---:|---|---|---|
| `✗` | `wilayah/listPropinsi` | Perlu konfirmasi | Endpoint berhasil, tetapi hanya mengembalikan `51 - BALI`, sementara ada data lain yang masih muncul pada endpoint objek pajak. Perlu kepastian cakupan wilayah resmi. |
| `✗` | `wilayah/listDati2` | Perlu konfirmasi | Berhasil untuk `kdPropinsi=51`, tetapi kosong untuk `kdPropinsi=32`. Perlu kepastian wilayah yang didukung. |
| `✗` | `wilayah/listKecamatan` | Perlu konfirmasi | Berhasil untuk `51/71`, tetapi kosong untuk `32/04`. |
| `✗` | `wilayah/listKelurahan` | Perlu konfirmasi | Berhasil untuk `51/71/010`, tetapi kosong untuk `32/04/010`. |
| `✗` | `wilayah/listBlok` | Perlu konfirmasi | Dapat mengembalikan blok untuk `32/04/010/001` walaupun daftar wilayah induknya kosong. Ini perlu dijelaskan. |
| `✗` | `objekPajak/search` | Perlu konfirmasi | Berhasil untuk query nama WP tertentu, tetapi kosong untuk query `DENPASAR`. Perlu definisi field pencarian. |
| `✓` | `objekPajak/listDetails` | Siap dipakai | Berhasil mengembalikan daftar objek pajak pada wilayah aktif `51/71`, termasuk objek dengan bangunan. |
| `✓` | `objekPajak/getByNop` | Siap dipakai | Berhasil mengembalikan detail objek pajak lengkap untuk NOP target `51.71.010.001.054.0032.0`. |
| `✓` | `lspop/listByNop` | Siap dipakai | Berhasil mengembalikan daftar bangunan untuk NOP target dengan `noBng=1`. |
| `✓` | `lspop/getBuilding` | Siap dipakai | Berhasil mengembalikan detail bangunan dan konsisten dengan `lspop/listByNop`. |
| `✗` | `lspop/listFasilitas` | Perlu konfirmasi | Endpoint aktif, tetapi response kosong pada bangunan yang diuji. Perlu contoh bangunan yang memang memiliki fasilitas. |
| `✗` | `sppt/list` | Perlu konfirmasi | Berhasil untuk `kdPropinsi=51` tahun 2023-2025, tetapi kosong untuk 2026 dan kosong untuk `kdPropinsi=32`. Perlu kepastian cakupan tahun dan wilayah. |
| `✓` | `sppt/listByNop` | Siap dipakai | Berhasil mengembalikan SPPT 2025, 2024, dan 2023 untuk NOP target. |
| `✓` | `sppt/get` | Siap dipakai | Berhasil mengembalikan detail SPPT tahun 2023 untuk NOP target. |
| `✓` | `objekPajak/getSpptHistory` | Siap dipakai | Berhasil mengembalikan histori SPPT yang konsisten dengan `sppt/listByNop`. |
| `✗` | `objekPajak/getTunggakan` | Perlu konfirmasi | Endpoint aktif, tetapi response kosong pada NOP target. Perlu contoh NOP yang memang memiliki tunggakan. |

## Ringkasan Eksekutif

Secara teknis API eksternal dapat diakses: seluruh endpoint yang diuji mengembalikan HTTP 200. Namun hasil pengujian menunjukkan bahwa keberhasilan data sangat bergantung pada wilayah dan kandidat NOP yang digunakan.

Pengujian awal dengan wilayah `32/04` menghasilkan sebagian data objek pajak, tetapi banyak endpoint turunan seperti bangunan, SPPT, histori SPPT, dan tunggakan kosong. Setelah retest memakai wilayah yang ditemukan dari API wilayah, yaitu `51/71` atau Bali/Kota Denpasar, data menjadi jauh lebih lengkap dan endpoint utama dapat berjalan sesuai ekspektasi.

Kesimpulan sementara: API eksternal layak dipakai untuk integrasi jika proyek diarahkan ke dataset aktif `51/71`. Untuk kebutuhan produksi yang stabil, tetap perlu klarifikasi kontrak data dari pemilik API, terutama cakupan wilayah, ketersediaan data SPPT per tahun, arti status pembayaran, dan kemungkinan response kosong.

## Parameter Valid yang Terbukti Menghasilkan Data

Wilayah aktif yang ditemukan:

| Level | Kode | Nama |
|---|---:|---|
| Provinsi | `51` | BALI |
| Dati2 | `71` | KOTA DENPASAR |
| Kecamatan | `010` | DENPASAR SELATAN |
| Kelurahan | `001` | SIDAKARYA |

Kandidat NOP lengkap yang berhasil dipakai untuk pengujian detail:

```text
51.71.010.001.054.0032.0
```

Komponen request:

```json
{
  "kdPropinsi": "51",
  "kdDati2": "71",
  "kdKecamatan": "010",
  "kdKelurahan": "001",
  "kdBlok": "054",
  "noUrut": "0032",
  "kdJnsOp": "0"
}
```

## API yang Berhasil dan Sesuai

| Endpoint | Hasil | Bukti hasil |
|---|---|---|
| `wilayah/listPropinsi` | Berhasil | HTTP 200, response non-empty: `51 - BALI`. |
| `wilayah/listDati2` | Berhasil dengan `kdPropinsi=51` | HTTP 200, response memuat `71 - KOTA DENPASAR` dan `72 - KABUPATEN BADUNG`. |
| `wilayah/listKecamatan` | Berhasil dengan `51/71` | HTTP 200, response memuat `010 - DENPASAR SELATAN` dan `020 - DENPASAR TIMUR`. |
| `wilayah/listKelurahan` | Berhasil dengan `51/71/010` | HTTP 200, response memuat `001 - SIDAKARYA` dan `002 - SESETAN`. |
| `objekPajak/listDetails` | Berhasil | Untuk `51/71`, response `rows` non-empty dan memuat banyak objek pajak, termasuk objek dengan bangunan. |
| `objekPajak/getByNop` | Berhasil | Untuk NOP `51.71.010.001.054.0032.0`, response detail objek pajak lengkap, termasuk `subjekPajak`, alamat OP, luas bumi, dan nilai sistem bumi. |
| `lspop/listByNop` | Berhasil | Untuk NOP target, response memuat 1 bangunan dengan `noBng=1`, `luasBng=168`, `jmlLantaiBng=2`, dan `nilaiSistemBng=870132648`. |
| `lspop/getBuilding` | Berhasil | Detail bangunan `noBng=1` ditemukan dan konsisten dengan hasil `lspop/listByNop`. |
| `sppt/list` | Berhasil untuk `kdPropinsi=51`, tahun 2023-2025 | Response `rows` non-empty untuk tahun 2023, 2024, dan 2025, baik tanpa filter status maupun dengan `statusPembayaran` tertentu. |
| `sppt/listByNop` | Berhasil | Untuk NOP target, response memuat SPPT tahun 2025, 2024, dan 2023 dengan nominal pajak dan status pembayaran. |
| `sppt/get` | Berhasil | Untuk NOP target tahun 2023, response detail SPPT lengkap, termasuk `njopSppt=1109000000`, `pbbYgHarusDibayarSppt=1094000`, dan `statusPembayaranSppt=1`. |
| `objekPajak/getSpptHistory` | Berhasil | Untuk NOP target, response histori SPPT tahun 2025, 2024, dan 2023, konsisten dengan `sppt/listByNop`. |

## API yang Berhasil Secara HTTP tetapi Hasil Kosong atau Belum Terbukti Sesuai

| Endpoint | Kondisi | Catatan |
|---|---|---|
| `wilayah/listDati2` | `kdPropinsi=32` | HTTP 200 tetapi `json: []`. Ini menunjukkan wilayah `32` tidak tersedia atau tidak aktif dalam dataset API saat pengujian. |
| `wilayah/listKecamatan` | `kdPropinsi=32`, `kdDati2=04` | HTTP 200 tetapi kosong. |
| `wilayah/listKelurahan` | `kdPropinsi=32`, `kdDati2=04`, `kdKecamatan=010` | HTTP 200 tetapi kosong. |
| `objekPajak/search` | Query `DENPASAR` | HTTP 200 tetapi kosong. Search tampaknya tidak selalu bisa dipakai untuk pencarian berbasis nama wilayah/alamat umum. Query `BUDI EMBER` berhasil pada pengujian awal. |
| `lspop/listByNop` | NOP `32.04.010.001.001.0001.0` | HTTP 200 tetapi kosong. Masih wajar karena `objekPajak/listDetails` untuk NOP ini menunjukkan `totalLuasBng=0`. |
| `lspop/getBuilding` | NOP `32.04.010.001.001.0001.0`, `noBng=1` | HTTP 200 tetapi `json: null`. Wajar jika NOP tersebut memang tidak punya bangunan. |
| `lspop/listFasilitas` | NOP target `51.71.010.001.054.0032.0`, `noBng=1` | HTTP 200 tetapi `json: []`. Belum bisa dipastikan gagal, karena bisa berarti bangunan tersebut memang tidak memiliki fasilitas. Perlu kandidat bangunan yang diketahui memiliki fasilitas. |
| `sppt/list` | `kdPropinsi=32`, tahun 2023-2026 | HTTP 200 tetapi seluruh kombinasi kosong. Wilayah `32` tampaknya bukan dataset aktif untuk SPPT. |
| `sppt/list` | `kdPropinsi=51`, tahun 2026 | HTTP 200 tetapi kosong untuk status `null`, `0`, dan `1`. Kemungkinan data SPPT 2026 belum tersedia. |
| `sppt/listByNop` | NOP `32.04.010.001.001.0001.0` | HTTP 200 tetapi kosong. |
| `sppt/get` | NOP `32.04.010.001.001.0001.0`, tahun 2024 | HTTP 200 tetapi `json: null`. |
| `objekPajak/getSpptHistory` | NOP `32.04.010.001.001.0001.0` | HTTP 200 tetapi kosong. |
| `objekPajak/getTunggakan` | NOP target `51.71.010.001.054.0032.0` | HTTP 200 tetapi `json: []`. Belum bisa dipastikan gagal, karena SPPT target berstatus bayar dan bisa memang tidak memiliki tunggakan. Perlu kandidat NOP dengan tunggakan/status belum bayar. |

## API yang Gagal

Tidak ada endpoint yang gagal secara transport pada pengujian ini. Semua request tercatat mengembalikan HTTP 200.

Namun ada beberapa hasil yang perlu dikategorikan sebagai "belum memenuhi kebutuhan data" karena response kosong/null pada skenario tertentu:

- Data wilayah `32/04` tidak konsisten untuk dipakai sebagai basis integrasi, karena daftar Dati2/Kecamatan/Kelurahan kosong.
- SPPT untuk `kdPropinsi=32` kosong untuk semua tahun yang diuji.
- SPPT tahun 2026 untuk `kdPropinsi=51` kosong.
- Fasilitas bangunan belum terbukti karena kandidat bangunan yang diuji mengembalikan fasilitas kosong.
- Tunggakan belum terbukti karena kandidat NOP yang diuji tidak memiliki tunggakan.

## Catatan Konsistensi Data

Ada beberapa hal yang perlu diminta klarifikasinya ke pemilik API:

1. `wilayah/listPropinsi` hanya mengembalikan `51 - BALI`, tetapi beberapa endpoint objek pajak masih bisa mengembalikan data untuk kode `32/04`. Perlu penjelasan apakah `32/04` adalah data dummy, legacy, atau dataset yang tidak lengkap.
2. `wilayah/listBlok` pada pengujian awal mengembalikan blok untuk `32/04/010/001`, walaupun daftar Dati2/Kecamatan/Kelurahan untuk `32/04` kosong. Ini perlu dikonfirmasi karena bisa memengaruhi alur dropdown wilayah di aplikasi.
3. `objekPajak/search` berhasil untuk query nama wajib pajak tertentu (`BUDI EMBER`), tetapi kosong untuk query `DENPASAR`. Perlu definisi field apa saja yang dicari oleh endpoint search.
4. `sppt/list` berhasil untuk `kdPropinsi=51` tahun 2023-2025, tetapi kosong untuk 2026. Perlu kepastian tahun pajak yang tersedia.
5. Response kosong pada `getTunggakan` dan `listFasilitas` tidak otomatis berarti endpoint rusak. Butuh data contoh dari pemilik API yang memang memiliki tunggakan dan fasilitas.

## Rekomendasi untuk Keberlangsungan Project

API eksternal masih bisa dipakai untuk prototipe dan pengembangan fitur utama jika aplikasi memakai dataset aktif `51/71`. Endpoint inti untuk wilayah, objek pajak, bangunan, SPPT, detail SPPT, dan histori SPPT sudah terbukti berjalan dengan data yang saling konsisten.

Untuk project yang membutuhkan stabilitas data, sebaiknya jangan bergantung penuh pada API eksternal tanpa kontrak data tertulis. Minimal perlu:

- daftar wilayah yang resmi tersedia;
- contoh NOP untuk skenario lengkap: punya bangunan, punya fasilitas, punya SPPT lunas, punya SPPT belum bayar, dan punya tunggakan;
- definisi status pembayaran/tagihan;
- kepastian ketersediaan tahun pajak;
- dokumentasi field yang nullable dan format request tiap endpoint.

Jika pemilik API tidak bisa menyediakan kepastian tersebut, opsi membuat API internal sendiri layak dipertimbangkan. API internal akan lebih aman untuk demo, testing, dan kontrol alur aplikasi, terutama untuk skenario yang saat ini belum terbukti dari API eksternal seperti fasilitas bangunan dan tunggakan.

## Kesimpulan

Status integrasi saat ini: layak lanjut dengan catatan.

Endpoint utama berhasil saat menggunakan dataset aktif `51/71`, tetapi API eksternal belum cukup kuat dijadikan satu-satunya sumber data tanpa klarifikasi tambahan. Untuk mitigasi risiko project, pendekatan paling pragmatis adalah tetap mendukung API eksternal, tetapi siapkan API internal atau data fixture lokal sebagai fallback untuk skenario penting yang belum tersedia atau belum konsisten.
