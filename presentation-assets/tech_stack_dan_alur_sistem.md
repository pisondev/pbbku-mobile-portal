# Tech Stack dan Alur Sistem PBB-Ku

Dokumen ini menjelaskan bagaimana komponen teknis PBB-Ku saling terhubung dari sisi aplikasi Android, API, database resmi, dan penyimpanan lokal.

## Ringkasan Stack

- Android Kotlin: bahasa utama aplikasi mobile.
- Jetpack Compose: UI deklaratif untuk layar onboarding, login, beranda, pencarian, detail objek pajak, SPPT, notifikasi, dan laporan perubahan.
- Navigation Compose: pengatur perpindahan halaman antar fitur.
- Retrofit + OkHttp: client HTTP untuk memanggil SIMPBB OP API.
- kotlinx.serialization: parser JSON request dan response.
- Room Database: penyimpanan lokal terstruktur untuk cache dan draft laporan perubahan.
- DataStore Preferences: penyimpanan session simulatif, preferensi, dan flag sederhana.
- WorkManager: pengingat lokal jatuh tempo SPPT.
- SIMPBB OP API: sumber data resmi objek pajak, subjek pajak, bangunan, SPPT, tunggakan, dan wilayah.
- SIMPBB Core Database: database resmi pemerintah daerah yang berada di belakang SIMPBB OP API.

## Alur Data Utama

```text
User
  -> Android App PBB-Ku
  -> Retrofit/OkHttp Client
  -> SIMPBB OP API
  -> SIMPBB Core System
  -> SIMPBB Core Database
```

Aplikasi tidak langsung membaca database resmi. Semua data resmi diperoleh melalui SIMPBB OP API. Pola ini menjaga aplikasi mobile tetap sebagai client, sedangkan validasi dan otoritas data tetap berada di sistem SIMPBB.

## API dan Android Client

Source konfigurasi API berada di:

```text
apps/android/app/src/main/java/id/pbbku/mobileportal/data/api/
```

Peran file utama:

- `SimpbbApiConfig.kt`: base URL API.
- `SimpbbApiClient.kt`: konfigurasi Retrofit, OkHttp, dan serializer.
- `SimpbbApiService.kt`: daftar endpoint oRPC yang dipakai aplikasi.
- `SensitiveHeaderInterceptor.kt`: perlindungan logging header sensitif.

Request API menggunakan pola oRPC via HTTP POST. Response utama dibaca dari field `json`, kemudian dipetakan dari DTO ke model domain.

## Dua Lapisan Database

### 1. SIMPBB Core Database

Ini adalah sumber kebenaran resmi. Database ini berada di belakang SIMPBB Core System dan tidak diakses langsung oleh aplikasi mobile.

Data yang berasal dari lapisan ini:

- Data objek pajak.
- Data subjek pajak.
- Data bangunan/LSPOP.
- Histori SPPT.
- Tunggakan.
- Referensi wilayah.

### 2. Room Local Database

Room dipakai di perangkat pengguna untuk data lokal yang aman disimpan oleh aplikasi.

Data yang disimpan lokal:

- Cache data read-only agar tampilan lebih stabil.
- Draft laporan perubahan bangunan.
- Status draft seperti draft, siap diajukan, dan menunggu verifikasi.

Room bukan database resmi PBB. Data di Room hanya membantu pengalaman pengguna dan tidak otomatis mengubah data di SIMPBB.

## DataStore dan Session

DataStore digunakan untuk data kecil yang tidak membutuhkan tabel:

- Session login simulatif.
- NIK aktif.
- Token session demo.
- Preferensi reminder.
- Status onboarding/pengaturan ringan.

DataStore berbeda dari Room karena tidak dipakai untuk relasi data kompleks.

## WorkManager dan Notifikasi

WorkManager menjadwalkan pengingat lokal berdasarkan data SPPT/tunggakan yang memiliki tanggal jatuh tempo.

Alur ringkas:

```text
SPPT/Tunggakan dari API
  -> Repository menghitung jadwal H-30, H-7, H-1
  -> WorkManager menjadwalkan reminder lokal
  -> Halaman Notifikasi menampilkan status reminder
```

Notifikasi bersifat lokal di perangkat. Aplikasi tidak mengirim pesan dari server.

## Alur Fitur Pencarian Sampai Detail

```text
Cari NOP/nama WP
  -> SearchViewModel
  -> SimpbbRepository
  -> SimpbbApiService
  -> SIMPBB OP API
  -> DTO
  -> Mapper
  -> Domain model
  -> Compose UI
```

Mapper menjaga agar format API tidak langsung bocor ke UI. UI cukup membaca model domain yang sudah bersih.

## Alur Laporan Perubahan

```text
Detail Bangunan
  -> Laporan Perubahan
  -> Load data lama LSPOP dari API
  -> User isi perubahan
  -> Simpan Draft ke Room
  -> Kirim Permohonan Verifikasi sebagai status lokal
```

Pada MVP, laporan perubahan belum mengubah data resmi. UI dibuat menyerupai proses permohonan agar reviewer memahami alur target, tetapi keputusan perubahan resmi tetap membutuhkan verifikasi petugas.

## Prinsip Desain Sistem

- SIMPBB API menjadi sumber data resmi.
- Android App hanya client mobile.
- Room menyimpan cache dan draft lokal.
- DataStore menyimpan session dan preferensi.
- WorkManager menangani reminder lokal.
- Mapper memisahkan DTO API dari model domain.
- UI Compose membaca state dari ViewModel, bukan langsung dari API.
