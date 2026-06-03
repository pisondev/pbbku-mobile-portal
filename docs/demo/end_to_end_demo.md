# Skenario Demo End-to-End PBB-Ku MVP

Dokumen ini memetakan halaman UI, tujuan setiap halaman, ekspektasi isi, dan alur demo menyeluruh. Tujuannya agar reviewer bisa memahami user flow aplikasi dan memberi saran UI tanpa menghilangkan fitur inti.

Jangan gunakan NIK pribadi nyata atau screenshot yang memuat data sensitif.

## Ringkasan Aplikasi

PBB-Ku adalah aplikasi Android MVP untuk wajib pajak PBB-P2. Aplikasi membantu pengguna mencari objek pajak, melihat detail objek/subjek pajak, melihat bangunan/LSPOP, melihat histori SPPT dan tunggakan, membaca arahan pembayaran non-transaksional, mengelola reminder lokal, dan membuat draft laporan perubahan bangunan secara lokal.

Jumlah halaman UI unik saat ini: 16 halaman.

Catatan route:

- `ReportDraftScreen` punya dua varian route, dengan dan tanpa `noBng`, tetapi dihitung sebagai satu halaman UI.
- `MainScaffold` dan bottom navigation tidak dihitung sebagai halaman UI, karena hanya shell navigasi.
- `PlaceholderScreen` ada di codebase tetapi tidak dipakai oleh navigation graph saat ini.

## Prasyarat Demo

- APK debug berhasil dibuat dari `apps/android`.
- Emulator atau perangkat Android tersedia.
- Koneksi internet aktif untuk memanggil API internal production.
- API production tersedia di `https://pbbku-api.tierratie.com/api/rpc/`.
- Jika Android 13+, izin notifikasi dapat diminta saat toggle pengingat diaktifkan.

## Data Demo Aman

| Data | Nilai | Catatan |
|---|---|---|
| NIK demo | `3404123456789012` | Hanya untuk login simulatif. |
| OTP demo | `123456` | Ditampilkan sebagai OTP demo. |
| Query pencarian | `BUDI` | Menghasilkan data demo dari API internal. |
| NOP utama | `51.71.010.001.054.0032.0` | Lengkap untuk objek, bangunan, SPPT 2026, dan tunggakan. |
| NOP tambahan | `32.04.010.001.001.0001.0` | Data demo tambahan untuk variasi pencarian. |

## Inventory Halaman UI

| No | Halaman | Route/Entry | Tujuan | Ekspektasi isi utama |
|---:|---|---|---|---|
| 1 | Splash | `splash` | Menentukan apakah user sudah login dan mengarahkan ke onboarding atau main app. | Brand `PBB-Ku`, subtitle portal PBB-P2, loading indicator. |
| 2 | Onboarding | `onboarding` | Menjelaskan kemampuan utama sebelum login. | Ringkasan fitur: cari objek, histori SPPT/tunggakan, reminder lokal, draft laporan perubahan. Tombol `Masuk`. |
| 3 | Login | `login` | Memvalidasi NIK demo 16 digit dan membuat pending OTP. | Input NIK numeric 16 digit, counter digit, pesan error, tombol `Lanjut ke OTP`. |
| 4 | OTP | `otp` | Memverifikasi OTP simulatif. | Info OTP demo `123456`, input OTP 6 digit, pesan error, tombol `Verifikasi`. |
| 5 | Beranda | `home` | Dashboard awal setelah login dan titik mulai demo. | NIK masked, status session simulatif, panduan pemula, shortcut cari objek, shortcut notifikasi, istilah PBB. |
| 6 | Cari Objek Pajak | `search` | Mencari objek pajak memakai NOP/nama dan filter wilayah. | Search input minimal 3 karakter, tombol `Daftar demo`, retry, filter wilayah bertingkat, daftar hasil, pagination, state loading/error/empty. |
| 7 | Detail Objek Pajak | `object_detail/{nopDisplay}` | Menampilkan profil objek dan subjek pajak untuk NOP terpilih. | Header data SIMPBB, NOP, objek pajak, subjek pajak, tombol salin NOP, shortcut Bangunan, Histori SPPT, Tunggakan, Laporan Perubahan, state cache bila ada. |
| 8 | Daftar Bangunan | `buildings/{nopDisplay}` | Menampilkan ringkasan bangunan LSPOP untuk NOP. | List bangunan, no bangunan, JPB/jenis, luas, lantai, NJOP bangunan, loading/error/empty. |
| 9 | Detail Bangunan | `building_detail/{nopDisplay}/{noBng}` | Menampilkan detail LSPOP dan fasilitas bangunan. | Data JPB, jenis, luas, lantai, tahun bangun/renovasi, kondisi, konstruksi, atap/dinding/lantai/langit-langit, NJOP bangunan, fasilitas, tombol laporan perubahan. |
| 10 | Histori SPPT | `sppt_history/{nopDisplay}` | Menampilkan seluruh tagihan/SPPT per tahun untuk NOP. | Daftar tahun pajak, status pembayaran, nominal, jatuh tempo, denda, tombol cara bayar untuk tagihan payable. |
| 11 | Tunggakan | `tunggakan/{nopDisplay}` | Menampilkan hanya tagihan aktif/belum lunas. | Total tunggakan aktif, daftar tagihan belum lunas, status, nominal, jatuh tempo, denda, tombol detail/cara bayar. |
| 12 | Detail Tagihan | `tax_bill_detail/{nopDisplay}/{taxYear}` | Menampilkan rincian perhitungan satu SPPT. | Status pembayaran, nominal, jatuh tempo, denda, tanggal pembayaran, NJOP tanah/bangunan/total, NJOPTKP, tarif, PBB terutang, tombol cara bayar jika belum lunas. |
| 13 | Informasi Pembayaran | `payment_info/{nopDisplay}/{taxYear}` | Memberi arahan pembayaran tanpa memproses transaksi. | Status tagihan, nominal, jatuh tempo, denda, arahan pembayaran resmi, kanal pembayaran umum, SSPD prototipe jika status lunas. |
| 14 | Laporan Perubahan Bangunan | `report/{nopDisplay}` atau `report/{nopDisplay}/{noBng}` | Membuat draft lokal perubahan bangunan tanpa mengubah data resmi. | Status draft, identitas NOP/no bangunan, data lama LSPOP, pilihan jenis perubahan, input luas/lantai baru, deskripsi, ringkasan, save draft, tandai simulasi, hapus draft. |
| 15 | Notifikasi | `notifications` | Menampilkan reminder lokal jatuh tempo. | Status reminder aktif/nonaktif, daftar reminder, tagihan tahun, NOP, status jadwal, jatuh tempo, nominal, catatan simulatif bila demo. |
| 16 | Pengaturan | `settings` | Mengelola data lokal, reminder, informasi aplikasi, dan logout. | Toggle pengingat, request izin notifikasi Android 13+, hapus cache, hapus draft laporan, versi aplikasi, debug info jika aktif, tombol keluar. |

## Tujuan UI per Area

### Auth dan Onboarding

Tujuan area ini adalah memberi konteks bahwa aplikasi masih MVP/demo dan login bersifat simulatif. UI perlu membuat pengguna paham bahwa NIK hanya dipakai untuk session demo dan akan dimasked setelah masuk.

Ekspektasi review UI:

- Brand dan fungsi aplikasi harus terbaca dalam 5 detik pertama.
- Login tidak boleh terasa seperti autentikasi produksi yang menjanjikan akses data pribadi resmi.
- Error NIK/OTP harus spesifik dan tidak membuat user menebak.

### Dashboard dan Navigasi Utama

Tujuan Beranda adalah menjadi checklist demo, bukan sekadar landing page. User harus tahu urutan ideal: cari objek, buka detail, cek SPPT/tunggakan, lihat pembayaran, buat laporan, cek notifikasi/pengaturan.

Ekspektasi review UI:

- Shortcut utama `Cari Objek Pajak` harus paling jelas.
- Istilah PBB membantu pengguna awam, tetapi jangan menenggelamkan CTA utama.
- Tutorial overlay harus membantu, bukan menghalangi eksplorasi.

### Pencarian dan Detail Data Pajak

Tujuan area ini adalah membuktikan integrasi API dan kemampuan membaca data objek pajak. User bisa mulai dari nama/NOP atau dari daftar demo dan filter wilayah.

Ekspektasi review UI:

- Hasil pencarian harus cukup informatif untuk memilih objek yang benar: nama WP, NOP, alamat, NJOP bila ada.
- Filter wilayah harus terasa optional; user tidak boleh wajib mengisi semua dropdown untuk mencoba demo.
- Detail objek harus menampilkan fallback `Data tidak tersedia` secara konsisten untuk field nullable.

### Bangunan dan Laporan Perubahan

Tujuan area ini adalah menunjukkan data LSPOP dan workflow draft laporan perubahan. Ini bukan submit resmi.

Ekspektasi review UI:

- Detail bangunan harus jelas membedakan data lama dari input perubahan baru.
- Tombol `Buat Laporan Perubahan` harus muncul setelah user melihat konteks bangunan.
- Label `Draft prototipe lokal` harus tetap terlihat agar tidak disalahartikan sebagai pelaporan resmi.

### SPPT, Tunggakan, dan Pembayaran

Tujuan area ini adalah membantu user memahami status tagihan, nominal, jatuh tempo, denda, dan tindakan lanjutan yang aman.

Ekspektasi review UI:

- Status lunas/belum lunas/overdue harus terlihat cepat.
- Tunggakan harus menonjolkan total aktif dan prioritas bayar.
- Informasi Pembayaran tidak boleh menampilkan QR, VA, atau klaim transaksi karena aplikasi tidak memproses pembayaran.
- SSPD prototipe hanya muncul sebagai bukti visual untuk tagihan lunas dan harus diberi label bukan bukti resmi.

### Notifikasi dan Pengaturan

Tujuan area ini adalah menunjukkan fitur pendukung lokal: reminder jatuh tempo, cache, draft, dan session.

Ekspektasi review UI:

- Reminder perlu dijelaskan sebagai lokal di perangkat.
- Toggle pengingat harus sinkron dengan izin notifikasi Android.
- Aksi hapus cache/draft harus jelas efeknya agar tidak dikira menghapus data resmi.

## Alur Demo Menyeluruh

### Skenario 1 - Masuk dan Orientasi Awal

Tujuan: menunjukkan onboarding, login simulatif, masking NIK, dan dashboard.

1. Buka aplikasi.
2. Tunggu Splash selesai.
3. Di Onboarding, jelaskan empat kemampuan utama aplikasi.
4. Tekan `Masuk`.
5. Masukkan NIK demo `3404123456789012`.
6. Tekan `Lanjut ke OTP`.
7. Masukkan OTP demo `123456`.
8. Pastikan Beranda tampil dan NIK berubah menjadi `34************12`.
9. Tunjukkan checklist `Panduan pemula` dan overlay tutorial.

Fitur terdemokan:

- Splash/session routing.
- Onboarding.
- Validasi NIK.
- OTP simulatif.
- Masking NIK.
- Tutorial dashboard.

### Skenario 2 - Pencarian Objek Pajak

Tujuan: menunjukkan user bisa menemukan objek pajak lewat search atau daftar demo.

1. Dari Beranda, tekan `Cari Objek Pajak`.
2. Tunjukkan input pencarian dan tombol `Daftar demo`.
3. Masukkan query `BUDI`.
4. Pastikan hasil menampilkan minimal NOP, nama wajib pajak, alamat bila ada, dan NJOP bila ada.
5. Tunjukkan filter wilayah bertingkat: Provinsi, Kabupaten/Kota, Kecamatan, Kelurahan, Blok.
6. Tekan salah satu hasil, idealnya NOP `51.71.010.001.054.0032.0`.

Fitur terdemokan:

- Endpoint `objekPajak/search`.
- Endpoint wilayah untuk filter.
- Pagination/daftar demo.
- Loading/error/empty state bila perlu.
- Navigasi ke detail objek.

### Skenario 3 - Detail Objek dan Shortcut Fitur

Tujuan: menunjukkan pusat informasi NOP dan pintu masuk fitur turunan.

1. Di Detail Objek Pajak, verifikasi NOP yang dipilih.
2. Tunjukkan kartu `Objek Pajak`: alamat, luas bumi, NJOP, jenis bumi, status WP.
3. Tunjukkan kartu `Subjek Pajak`: nama, alamat WP, pekerjaan.
4. Tekan `Salin NOP` bila ingin mendemokan utility kecil.
5. Jelaskan empat shortcut: Bangunan, Histori SPPT, Tunggakan, Laporan Perubahan.

Fitur terdemokan:

- Endpoint `objekPajak/getByNop`.
- Fallback data kosong.
- Cache detail jika pernah dimuat.
- Shortcut lintas fitur.

### Skenario 4 - Bangunan dan Fasilitas

Tujuan: menunjukkan LSPOP dari daftar bangunan sampai detail.

1. Dari Detail Objek, tekan `Bangunan`.
2. Di Daftar Bangunan, pilih bangunan `1` jika tersedia.
3. Di Detail Bangunan, tunjukkan JPB, jenis, luas, lantai, kondisi, konstruksi, material, nilai sistem bangunan.
4. Scroll ke bagian `Fasilitas`.
5. Jika fasilitas kosong, jelaskan empty state sebagai respons data valid.

Fitur terdemokan:

- Endpoint `lspop/listByNop`.
- Endpoint `lspop/getBuilding`.
- Endpoint `lspop/listFasilitas`.
- Empty state fasilitas.
- Navigasi ke laporan perubahan.

### Skenario 5 - Draft Laporan Perubahan Bangunan

Tujuan: menunjukkan alur pelaporan perubahan sebagai draft lokal, bukan submit resmi.

1. Dari Detail Bangunan, tekan `Buat Laporan Perubahan`.
2. Pastikan halaman memuat NOP dan nomor bangunan.
3. Pilih jenis perubahan, misalnya `Perubahan luas bangunan`.
4. Isi `Luas bangunan baru`, `Jumlah lantai baru`, dan deskripsi.
5. Tekan `Simpan Draft`.
6. Tekan `Tampilkan Ringkasan`.
7. Tekan `Tandai Terkirim Simulasi`.
8. Opsional: tekan `Hapus Draft`.

Fitur terdemokan:

- Load data lama LSPOP sebagai konteks.
- Validasi input luas/lantai/deskripsi.
- Room/local database draft.
- Status draft: Draft, Siap Diajukan, Terkirim Simulasi.
- Batasan bahwa data resmi tidak berubah.

### Skenario 6 - Histori SPPT dan Detail Tagihan

Tujuan: menunjukkan riwayat tagihan tahunan dan detail perhitungan.

1. Kembali ke Detail Objek.
2. Tekan `Histori SPPT`.
3. Tunjukkan daftar tagihan per tahun, status, nominal, jatuh tempo, dan denda.
4. Pilih tahun 2026 atau tahun lain yang tersedia.
5. Di Detail Tagihan, tunjukkan nominal tagihan, jatuh tempo, denda, tanggal pembayaran, NJOP tanah/bangunan/total, NJOPTKP, tarif, dan PBB terutang.
6. Jika status belum lunas, tekan `Lihat Cara Bayar`.

Fitur terdemokan:

- Endpoint `objekPajak/getSpptHistory` atau `sppt/listByNop`.
- Endpoint `sppt/get`.
- Status pembayaran.
- Detail perhitungan pajak.
- Navigasi ke pembayaran.

### Skenario 7 - Tunggakan dan Prioritas Pembayaran

Tujuan: menunjukkan tagihan aktif yang perlu perhatian.

1. Dari Detail Objek, tekan `Tunggakan`.
2. Tunjukkan `Total Tunggakan Aktif`.
3. Tunjukkan daftar tagihan belum lunas, status, nominal, jatuh tempo, dan denda.
4. Pilih salah satu tunggakan untuk detail.
5. Tekan `Lihat Cara Bayar`.

Fitur terdemokan:

- Endpoint `objekPajak/getTunggakan`.
- Totalisasi tunggakan aktif.
- CTA pembayaran untuk tagihan payable.
- State kosong jika NOP tidak punya tunggakan.

### Skenario 8 - Informasi Pembayaran Non-Transaksional

Tujuan: menunjukkan aplikasi memberi arahan pembayaran tanpa transaksi.

1. Dari Detail Tagihan atau Tunggakan, buka `Informasi Pembayaran`.
2. Tunjukkan status tagihan, nominal, jatuh tempo, denda, dan tanggal pembayaran bila ada.
3. Jelaskan kartu `Arahan Pembayaran`.
4. Jelaskan kartu `Kanal Pembayaran`.
5. Jika tagihan lunas, tunjukkan `Bukti/SSPD Prototipe` dan label `PROTOTIPE - bukan bukti pembayaran resmi`.

Fitur terdemokan:

- Payment info read-only.
- Tidak ada QR/VA/payment gateway.
- Bukti prototipe hanya visual, bukan bukti resmi.

### Skenario 9 - Notifikasi dan Reminder Lokal

Tujuan: menunjukkan reminder jatuh tempo sebagai fitur lokal perangkat.

1. Buka tab `Notif`.
2. Tunjukkan status reminder aktif/nonaktif.
3. Tunjukkan daftar reminder demo atau empty state.
4. Jelaskan bahwa reminder muncul dari data jatuh tempo dan setting lokal.

Fitur terdemokan:

- Reminder lokal.
- Jadwal 30/7/1 hari sebelum jatuh tempo.
- State simulatif jika data asli belum lengkap.

### Skenario 10 - Pengaturan, Data Lokal, dan Logout

Tujuan: menunjukkan kontrol lokal dan akhir session.

1. Buka tab `Setelan`.
2. Toggle `Pengingat jatuh tempo`.
3. Jika Android 13+, izinkan atau tolak permission notifikasi untuk mendemokan state izin.
4. Tekan `Hapus Cache`.
5. Tekan `Hapus Draft Laporan`.
6. Tunjukkan informasi aplikasi/version.
7. Tekan `Keluar`.
8. Pastikan kembali ke Login.

Fitur terdemokan:

- DataStore reminder setting.
- Permission notification.
- Clear cache.
- Clear draft report.
- Session logout.

## Alur Demo Cepat 10 Menit

Gunakan alur ini jika waktu review terbatas:

1. Login demo dengan NIK dan OTP.
2. Dari Beranda buka Cari.
3. Search `BUDI`, pilih `51.71.010.001.054.0032.0`.
4. Di Detail Objek, buka Bangunan, lalu Detail Bangunan.
5. Buat draft laporan perubahan, simpan, tampilkan ringkasan.
6. Kembali ke Detail Objek, buka Histori SPPT.
7. Buka Detail Tagihan 2026, lalu Informasi Pembayaran.
8. Buka Tunggakan dan tunjukkan total aktif.
9. Buka Notifikasi.
10. Buka Pengaturan, toggle reminder, hapus data lokal, logout.

## Batasan Demo

- Login NIK dan OTP bersifat simulatif.
- Aplikasi tidak melakukan pembayaran nyata.
- Bukti/SSPD yang tampil adalah prototipe bila tidak berasal dari endpoint resmi.
- Laporan perubahan bangunan hanya draft/prototipe lokal dan tidak mengubah data SIMPBB.
- Endpoint write resmi seperti `objekPajak/save` tidak digunakan oleh aplikasi Android.
- Reminder bersifat lokal di perangkat dan tergantung data jatuh tempo yang tersedia.

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

API smoke test production:

```powershell
curl.exe -s https://pbbku-api.tierratie.com/health
```
