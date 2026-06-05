# Pengembangan Aplikasi Mobile: PBB-Ku Mobile Portal

Final Project Presentation [4 Juni 2026]

Final Project Pengembangan Aplikasi Mobile • Kelas KOM  
Program Studi Ilmu Komputer, FMIPA UGM

## Anggota Kelompok

- Mikail Achmad | 24/542370/PA/23026
- Pison Golda Mountera | 24/543770/PA/23107
- Muhammad Rayyan Buna Satria | 24/543564/PA/23096

4 Juni 2026

---

# Tautan Project

## Github Repository

https://github.com/pisondev/pbbku-mobile-portal

## Dokumen SRS

Tautan menyusul.

---

# Agenda Presentasi

Struktur pembahasan dari penjelasan aplikasi, teck stack, alur sistem, fitur utama hingga pengujian.

1. Penjelasan Aplikasi PBB-Ku
2. Penjelasan Sistem Pajak
3. Tech Stack & Alur Sistem
4. Daftar Fitur Utama
5. Aplikasi Kami
6. Kesimpulan

---

# 1. Penjelasan Aplikasi PBB-Ku

Penjelasan proyek akhir yang telah kami kerjakan dalam bentuk Aplikasi PBB-Ku (Pajak Bumi Bangunan) Mobile Portal. Penjelasan difokuskan dalam 2 bagian: Tujuan Perancangan & Fokus Teknis.

## Aplikasi Dirancang untuk?

PBB-Ku Mobile Portal adalah proyek aplikasi mobile Android untuk portal wajib pajak PBB-P2. Aplikasi dirancang sebagai client yang membantu wajib pajak melihat informasi objek pajak, data bangunan, histori SPPT, tunggakan, dan pengingat pembayaran dengan mengambil data dari SIMPBB OP API.

Proyek ini disusun untuk kebutuhan Proyek 2 mata kuliah Pengembangan Aplikasi Mobile.

## Fokus teknis

Pada fase awal, PBB-Ku tidak membangun backend internal dan tidak menyimpan data resmi PBB di database sendiri. Data resmi berasal dari SIMPBB OP API, sementara aplikasi hanya menyimpan data lokal yang dibutuhkan untuk session simulatif, cache, preferensi, notifikasi lokal, dan draft fitur prototipe.

Akan tetapi, karena kebutuhan tertentu, kami memilih membuat internal API sendiri untuk mempermudah manajemen sistem dengan server Go dan basis data PostgreSQL.

---

# 2.1 Penjelasan Sistem Pajak

Penjelasan istilah-istilah dan gambaran alur dari Sistem Pajak berdasarkan hasil riset dan literasi.

## Istilah Utama

- NIK
- NOP
- NJOP
- SPPT
- SSPD

## Alur dan Keterangan

- NIK digunakan untuk mengidentifikasi wajib pajak.
- NIK terhubung dengan NOP sebagai identitas objek pajak.
- Objek Pajak berisi data tanah dan/atau bangunan.
- NJOP menjadi dasar perhitungan nilai pajak.
- SPPT menampilkan tagihan PBB tahunan.
- Status pembayaran menentukan apakah tagihan lunas atau menjadi tunggakan.
- Tagihan lunas memiliki bukti pembayaran SSPD.
- Jika data bangunan berubah, pengguna dapat membuat laporan untuk diverifikasi petugas.

---

# 2.2 Visualisasi Sistem Pajak

Alur sistem pajak dimulai dari NIK wajib pajak sebagai identitas wajib pajak. NIK terhubung ke NOP atau Nomor Objek Pajak, kemudian mengarah ke Objek Pajak berupa tanah dan/atau bangunan.

Dari Objek Pajak, alur terbagi menjadi dua bagian. Bagian pertama berkaitan dengan nilai dan tagihan pajak. Objek Pajak menjadi dasar untuk menentukan NJOP sebagai dasar nilai pajak. NJOP kemudian digunakan dalam SPPT sebagai tagihan tahunan PBB. Setelah SPPT terbentuk, status pembayaran menentukan alur berikutnya.

Jika status pembayaran belum lunas, tagihan menjadi tunggakan berupa tagihan aktif atau denda. Setelah itu, pengguna diarahkan ke informasi pembayaran melalui kanal resmi pemerintah daerah atau mitra. Jika status pembayaran lunas, pengguna memiliki SSPD sebagai bukti pembayaran.

Bagian kedua berkaitan dengan perubahan data bangunan. Dari Objek Pajak, pengguna dapat membuat laporan perubahan data bangunan. Laporan tersebut diverifikasi petugas sebelum data resmi berubah.

## Urutan Alur

1. NIK Wajib Pajak
2. NOP / Nomor Objek Pajak
3. Objek Pajak / Tanah dan Bangunan
4. NJOP / Dasar nilai pajak
5. SPPT / Tagihan tahunan PBB
6. Status pembayaran
7. Belum lunas → Tunggakan / Tagihan aktif atau denda → Informasi pembayaran / Kanal resmi Pemda atau mitra
8. Lunas → SSPD / Bukti pembayaran
9. Objek Pajak → Laporan perubahan data bangunan → Verifikasi petugas sebelum data resmi berubah

---

# 2.3 Lebih Lanjut tentang NOP & NJOP

## NOP

- NOP memiliki arti susunan nomor identitas objek pajak.
- NOP digunakan sebagai sarana dalam proses terjadinya administrasi dalam perpajakan.
- Dengan adanya NOP, warga Indonesia dapat lebih mudah untuk mengetahui lokasi dan posisi dari objek pajak yang menjadi kewajibannya.
- NOP juga dapat digunakan untuk mempermudah dalam melakukan proses pengambilan terhadap Surat Pemberitahuan Objek Pajak (SPOP) yang resmi.

## NJOP

- NJOP adalah harga rata-rata yang diperoleh dari transaksi jual beli yang terjadi secara wajar dan jika tidak terdapat transaksi jual beli.
- NJOP ditentukan melalui perbandingan harga objek lain yang sejenis atau nilai perolehan baru atau NJOP pengganti.
- Pajak akan dihitung dengan dasar Nilai Jual Kena Pajak (NJKP) yang ditetapkan serendah-rendahnya 20% dan setinggi-tingginya 100% dari NJOP.

---

# 3.1 Penjelasan Tech Stack & Alur Sistem

## Tech Stack

- Android
- Kotlin
- Jetpack Compose
- Retrofit + OkHttp
- kotlinx.serialization
- DataStore Preferences
- Room
- WorkManager
- Android 8.0/API 26
- feature-based dengan layer ringan

## Penjelasan lanjutan

- Android Kotlin dan Jetpack Compose sebagai dasar.
- ViewModel dan Repository mengatur state serta alur data aplikasi.
- Retrofit dan OkHttp digunakan untuk komunikasi ke API.
- Room/SQLite menyimpan cache dan draft data lokal.
- DataStore menyimpan session, NIK, dan preferensi pengguna.
- WorkManager mengatur reminder jatuh tempo SPPT.
- Backend SIMPBB OP API menangani request melalui REST/oRPC.
- Go Service menjalankan business logic dan validasi data.
- PostgreSQL menyimpan database resmi PBB.

---

# 3.2 Visualisasi Tech Stack & Alur Sistem

Alur sistem dimulai dari User atau Wajib Pajak yang menggunakan Mobile App PBB-Ku. Di dalam aplikasi mobile, Android App dibangun menggunakan Kotlin dan Jetpack Compose. Aplikasi berhubungan dengan App State yang dikelola oleh ViewModel dan Repository.

ViewModel dan Repository terhubung dengan API Client yang menggunakan Retrofit dan OkHttp. API Client mengirim dan menerima data dari Backend atau SIMPBB OP API. Pada bagian backend, terdapat REST/oRPC API dan Go Service yang menjalankan business logic dan validation.

Backend kemudian terhubung ke SIMPBB Core System. Di dalam SIMPBB Core System terdapat Core Service sebagai otoritas data resmi dan PostgreSQL sebagai SIMPBB Core Database.

Pada perangkat pengguna, aplikasi juga memakai local storage. Local storage berisi SQLite melalui Room Database, DataStore Preferences untuk session, NIK, dan preferensi, serta WorkManager untuk reminder jatuh tempo.

## Urutan Alur

1. User / Wajib Pajak
2. Mobile App PBB-Ku
3. Android App / Kotlin + Jetpack Compose
4. App State / ViewModel & Repository
5. API Client / Retrofit + OkHttp
6. Backend / SIMPBB OP API
7. REST/oRPC API
8. Go Service / Business Logic & Validation
9. SIMPBB Core System
10. Core Service / Otoritas Data Resmi
11. PostgreSQL / SIMPBB Core Database

## Local Storage di Perangkat

- SQLite via Room Database
- DataStore Preferences: Session, NIK, Preferensi
- WorkManager: Reminder Jatuh Tempo

---

# 3.3 Struktur Manajemen Folder

Bagian ini kami mencoba menjelaskan terkait bagaimana struktur folder kami kelola dan bagaimana hierarki kaitannya.

```text
pbbku-mobile-portal/
├── apps/
│   ├── android/
│   │   └── Source Android
│   │       ├── core/      Utility umum
│   │       ├── data/      Akses data
│   │       ├── domain/    Model bisnis
│   │       ├── feature/   Fitur aplikasi
│   │       └── ui/        Komponen tampilan
│   └── api/               API pendukung / lokal
├── docs/                  Dokumentasi proyek
├── presentation asset/    Bahan presentasi
├── scripts/               Script otomasi
└── File konfigurasi root: README, .gitignore, .env
```

---

# 4. Daftar Fitur Utama

Fitur pokok aplikasi Mobile PBB-Ku yang sudah dapat digunakan:

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

---

# 5.1 Aplikasi Kami: Logo Kami

Logo PBB-Ku dirancang untuk mencerminkan aplikasi pajak yang modern, aman, dan berfokus pada kemudahan pengguna.

## Simbolisme Visual

### Garis Lengkung (Kubah)

Mewakili elemen Bangunan. Secara kiasan, bentuk ini juga melambangkan sebuah pintu masuk bagi wajib pajak untuk mengakses data mereka.

### Daun Empat Helai

Mewakili elemen Bumi atau tanah. Jika digabungkan dengan kubah, kedua simbol ini merangkum objek utama dari aplikasi: Pajak Bumi dan Bangunan.

---

# 5.2 Aplikasi Kami: Berhasil Dijalankan

## Tampilan Onboarding

Tampilan onboarding memperkenalkan aplikasi sebagai portal wajib pajak PBB-P2. Judul aplikasi yang tampil adalah PBB-Ku dengan keterangan bahwa aplikasi menyediakan akses layanan PBB-P2 dari satu aplikasi Android.

### Cari Objek Pajak

Temukan objek PBB berdasarkan NOP atau nama wajib pajak, lalu buka detail data yang tersedia.

Tombol: Lanjut.

### Lihat SPPT dan Tunggakan

Pantau histori SPPT, nominal tagihan, status pembayaran, jatuh tempo, dan tunggakan aktif.

Tombol: Lanjut.

### Aktifkan Pengingat Lokal

Simpan reminder di perangkat untuk membantu mengingat jatuh tempo pembayaran PBB.

Tombol: Lanjut.

### Buat Draft Laporan

Susun draft perubahan data bangunan tanpa mengubah data resmi SIMPBB.

Tombol: Masuk.

---

# 5.2 Aplikasi Telah Berhasil Dijalankan

## Tampilan Login & OTP

### Login Wajib Pajak

Halaman login menampilkan judul PBB-Ku dengan keterangan portal PBB-P2 untuk cek objek, tagihan, tunggakan, dan laporan perubahan.

Bagian login berjudul Masuk. Pengguna diminta menggunakan NIK 16 digit. Data objek, tagihan, dan laporan demo dibatasi sesuai NIK session.

NIK demo yang tersedia:

- 3276010101010001
- 3276010101010002
- 3276010101010003

Input yang tersedia:

- NIK
- 0/16 digit

Tombol: Lanjut ke OTP.

### Verifikasi OTP

Bagian verifikasi menampilkan Kode OTP 123456.

Pengguna diminta memasukkan kode OTP untuk membuka Beranda.

Input yang tersedia:

- OTP
- 0/6 digit

Tombol: Verifikasi.

---

# 5.2 Aplikasi Telah Berhasil Dijalankan

## Tampilan Beranda

Halaman beranda menampilkan sapaan Halo, Siti Rahayu dan lokasi halaman PBB-Ku / Beranda. Bagian utama menunjukkan status Sesi aktif.

## Beranda

NOP, SPPT, tunggakan, reminder, dan draft laporan perubahan bangunan tersedia dalam satu alur.

### Informasi Penting

Gunakan aplikasi ini untuk memantau data PBB-P2. Pembayaran resmi tetap dilakukan melalui kanal pemerintah daerah atau mitra yang ditunjuk.

### Panduan Pemula

Langkah Utama PBB-Ku digunakan sebagai carousel untuk jalur cepat saat demo atau eksplorasi data.

#### Cari objek pajak

Mulai dari NOP atau nama wajib pajak, lalu pilih hasil yang sesuai.

Tautan: Buka Cari >

Tombol:

- Previous
- Next

### Bantuan Cepat

Istilah PBB dapat diklik untuk membuka penjelasan singkat.

Istilah yang tersedia:

- NOP — Lihat
- NJOP — Lihat
- SPPT — Lihat
- SSPD — Lihat
- Tunggakan — Lihat

Navigasi bawah menampilkan menu:

- Beranda
- Cari
- Notif
- Setelan

---

# 5.2 Aplikasi Telah Berhasil Dijalankan

## Halaman “Cari”

Halaman Cari menampilkan sapaan Halo, Siti Rahayu dan lokasi halaman PBB-Ku / Cari Objek.

### Cari Objek

Label: Pencarian SIMPBB.

Pengguna dapat memasukkan NOP atau nama wajib pajak. Hasil dibatasi ke objek yang terhubung dengan NIK login.

### Informasi Penting

Daftar di bawah memuat objek milik NIK session. Gunakan pencarian dan filter wilayah untuk menyaring hasil.

Input pencarian:

- NOP atau nama wajib pajak

Keterangan:

- Daftar otomatis disaring sesuai teks yang diketik.

Tombol:

- Semua Objek
- Coba Lagi

### Filter Wilayah

Filter wilayah digunakan untuk mempersempit hasil berdasarkan wilayah.

Pilihan filter:

- Provinsi: Pilih provinsi
- Kab/Kota: Pilih kab/kota
- Kecamatan: Pilih kecam...
- Kelurahan: Pilih kelurah...
- Blok: Pilih blok

Tombol:

- Sembunyikan
- Tampilkan
- Reset Filter

### Daftar Objek

Daftar Objek: 5 dari 5 hasil.

#### Objek 1

- Nama wajib pajak: SITI RAHAYU
- NOP: 32.04.010.001.001.0001.0
- Alamat: Jl. Merdeka No. 12, Kelurahan Demo Barat
- NJOP bumi: Rp189.000.000,00
- Tombol: Detail

#### Objek 2

- Nama wajib pajak: SITI RAHAYU
- NOP: 32.04.010.001.002.0002.0
- Alamat: Kavling Harmoni Blok C2, Kelurahan Demo Barat
- NJOP bumi: Rp96.000.000,00
- Tombol: Detail

#### Objek 3

- Nama wajib pajak: SITI RAHAYU
- NOP: 32.04.010.001.003.0003.0
- Alamat: Jl. Anggrek Raya No. 21, Kelurahan Demo Barat
- NJOP bumi: Rp132.000.000,00
- Tombol: Detail

---

# 5.2 Aplikasi Telah Berhasil Dijalankan

## Halaman “Cari > Detail Objek”

Halaman Detail Objek menampilkan label Data resmi SIMPBB.

## Detail Objek

Profil subjek, data objek, dan fitur lanjutan untuk NOP yang dipilih.

NOP yang ditampilkan:

32.04.010.001.001.0001.0

### Menu Terkait

Pilih fitur lanjutan untuk objek pajak ini.

- Bangunan — Data bangunan
- Histori SPPT — Tagihan tahunan
- Tunggakan — Belum lunas
- Laporan — Perubahan data

### Subjek Pajak

- Nama profil: SITI RAHAYU
- Keterangan: Profil wajib pajak
- Nama WP: SITI RAHAYU
- Alamat WP: Jl. Melati No. 8, Kota Bandung
- Pekerjaan: Warga demo

### Objek Pajak

- NOP: 32.04.010.001.001.0001.0
- Alamat objek: Jl. Merdeka No. 12, Kelurahan Demo Barat
- Luas bumi: 210 m2
- NJOP bumi: Rp189.000.000,00
- Jenis bumi: Tanah dan bangunan
- Status WP: Pemilik

Navigasi bawah menampilkan menu:

- Beranda
- Cari
- Notif
- Setelan

---

# 5.2 Aplikasi Telah Berhasil Dijalankan

## Halaman “Notifikasi” & “Setelan”

### Notifikasi

Halaman Notifikasi menampilkan status Reminder nonaktif.

Daftar pengingat lokal digunakan untuk jatuh tempo SPPT. Pengingat lokal nonaktif dan dapat diaktifkan dari Pengaturan.

### Prioritas Tagihan

Tagihan prioritas yang ditampilkan:

- Tagihan: 2024
- Nominal: Rp1.145.000,00
- Tanggal jatuh tempo: 30 September 2024
- Aksi: Lihat Detail

### Pengaturan

Halaman Pengaturan menampilkan label Kontrol data lokal.

Pengaturan digunakan untuk mengelola pengingat, cache, draft laporan, informasi aplikasi, dan sesi pengguna.

### Pengingat Jatuh Tempo

Jadwalkan reminder lokal 30, 7, dan 1 hari sebelum jatuh tempo.

### Cache Data Terakhir

Menghapus cache read-only yang dipakai saat data detail berhasil dimuat.

Tombol: Hapus Cache.

### Draft Laporan

Menghapus semua draft laporan perubahan bangunan yang tersimpan lokal.

---

# 6. Kesimpulan

Saat ini, pengembangan MVP dari aplikasi PBB-ku telah selesai dan dapat dijalankan dengan baik pada android. Aplikasi ini sukses mengimplementasikan fitur-fitur krusial seperti login simulatif (NIK dan OTP), pencarian berdasarkan NOP atau nama, peninjauan rinci bangunan (LSPOP), hingga penyaringan berdasarkan wilayah.

---

# Terima kasih
