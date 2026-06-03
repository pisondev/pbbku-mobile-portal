# Skenario Demo End-to-End PBB-Ku MVP

Dokumen ini adalah panduan demo aman untuk reviewer. Jangan gunakan NIK pribadi nyata atau screenshot yang memuat data sensitif.

## Prasyarat

- APK debug berhasil dibuat dari `apps/android`.
- Emulator atau perangkat Android tersedia.
- Koneksi internet aktif untuk memanggil SIMPBB OP API.
- Jika Android 13+, izin notifikasi dapat diminta saat toggle pengingat diaktifkan.

## Data Demo Aman

| Data | Nilai |
|---|---|
| NIK demo | `3404123456789012` |
| OTP demo | `123456` |
| Query pencarian aman | `BUDI` |
| Contoh NOP dari runtime test sebelumnya | `32.04.010.001.001.0001.0` |

Catatan: query `BUDI` dan NOP contoh berasal dari eksplorasi API/runtest sebelumnya. Jangan gunakan data ini untuk screenshot publik jika memuat nama/alamat yang dianggap sensitif.

## Langkah Demo

1. Buka aplikasi PBB-Ku.
2. Pada onboarding, pilih masuk.
3. Input NIK demo `3404123456789012`.
4. Input OTP demo `123456`.
5. Pastikan Beranda tampil dengan NIK masked `34************12`.
6. Pada Beranda, perlihatkan checklist `Panduan pemula` dan overlay tutorial. Gunakan `Next` untuk menjelaskan langkah, atau `Skip` jika reviewer ingin langsung demo.
7. Tekan tombol `Cari Objek Pajak` dari checklist/overlay atau buka tab `Cari`.
8. Pada halaman pencarian, perlihatkan overlay yang menyorot input pencarian dan tombol `Daftar demo`.
9. Input query `BUDI`.
10. Pastikan hasil pencarian tampil dengan NOP, nama wajib pajak, dan alamat bila tersedia.
11. Pilih salah satu hasil untuk membuka `Detail Objek Pajak`.
12. Pastikan detail objek pajak menampilkan NOP, alamat objek, luas/NJOP bila tersedia, dan data subjek pajak dengan fallback `Data tidak tersedia`.
13. Buka `Bangunan`, lalu buka salah satu detail bangunan jika tersedia.
14. Dari detail bangunan, pilih `Buat Laporan Perubahan`.
15. Isi luas/jumlah lantai baru dan deskripsi, lalu simpan draft atau tampilkan ringkasan simulasi.
16. Kembali ke detail objek pajak, buka `Histori SPPT`.
17. Buka salah satu detail tagihan jika tersedia.
18. Jika tagihan belum lunas, pilih `Lihat Cara Bayar`.
19. Pastikan halaman pembayaran menyatakan aplikasi tidak memproses pembayaran nyata.
20. Buka `Tunggakan` dan pastikan empty/error/success state tampil tanpa crash sesuai data API.
21. Buka tab `Notifikasi`; jika belum ada data jatuh tempo, aplikasi dapat menampilkan reminder simulatif.
22. Buka tab `Pengaturan`.
23. Coba toggle pengingat, hapus cache, hapus draft laporan, dan logout.
24. Pastikan logout kembali ke halaman login.

## Batasan Demo

- Login NIK dan OTP bersifat simulatif.
- Aplikasi tidak melakukan pembayaran nyata.
- Bukti/SSPD yang tampil adalah prototipe bila tidak berasal dari endpoint resmi.
- Laporan perubahan bangunan hanya draft/prototipe lokal dan tidak mengubah data SIMPBB.
- Endpoint write resmi seperti `objekPajak/save` tidak digunakan.

## Verifikasi Pendukung

Sebelum demo, jalankan:

```zsh
source ~/.zshrc
cd /c/programming/4th-sem/mobapps/pbbku-mobile-portal/apps/android
./gradlew :app:testDebugUnitTest :app:assembleDebug --offline
./gradlew :app:lintDebug --offline
```

APK debug tersedia di:

```text
apps/android/app/build/outputs/apk/debug/app-debug.apk
```
