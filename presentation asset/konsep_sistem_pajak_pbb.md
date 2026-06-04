# Konsep Sistem Pajak PBB dalam PBB-Ku

Dokumen ini menjelaskan istilah dan hubungan data PBB-P2 yang dipakai dalam aplikasi PBB-Ku.

## PBB-P2

PBB-P2 adalah Pajak Bumi dan Bangunan Perdesaan dan Perkotaan. Objek pajaknya berupa tanah dan/atau bangunan yang dimiliki, dikuasai, atau dimanfaatkan oleh wajib pajak.

Dalam aplikasi PBB-Ku, PBB-P2 direpresentasikan sebagai alur:

```text
NIK Wajib Pajak
  -> relasi ke NOP
  -> objek pajak dan bangunan
  -> NJOP
  -> SPPT
  -> status pembayaran/tunggakan
  -> informasi pembayaran atau SSPD
```

## NIK

NIK adalah Nomor Induk Kependudukan. Dalam konteks aplikasi, NIK dipakai untuk session login simulatif dan membatasi daftar objek pajak demo yang dapat diakses.

Peran NIK:

- Mengidentifikasi pengguna aplikasi.
- Menghubungkan pengguna dengan objek pajak yang relevan.
- Menjadi dasar pembatasan akses agar pengguna tidak melihat semua data.
- Ditampilkan dengan sensor di tengah untuk mengurangi risiko paparan data pribadi.

## NOP

NOP adalah Nomor Objek Pajak. NOP mengidentifikasi satu objek pajak secara unik.

Format umum NOP:

```text
PP.KK.CCC.LLL.BBB.NNNN.J
```

Makna segmen:

- `PP`: kode provinsi.
- `KK`: kode kabupaten/kota.
- `CCC`: kode kecamatan.
- `LLL`: kode kelurahan/desa.
- `BBB`: kode blok.
- `NNNN`: nomor urut objek.
- `J`: kode jenis objek.

NOP harus diperlakukan sebagai string, bukan angka, karena leading zero penting.

## Objek Pajak

Objek pajak adalah tanah dan/atau bangunan yang dikenai PBB.

Data objek pajak biasanya memuat:

- NOP.
- Alamat objek.
- Luas bumi.
- Luas bangunan.
- Jenis bumi.
- Status objek.
- Data subjek/wajib pajak terkait.

## Subjek Pajak dan Wajib Pajak

Subjek pajak adalah pihak yang memiliki, menguasai, atau memanfaatkan objek pajak. Wajib pajak adalah pihak yang memiliki kewajiban membayar pajak atas objek tersebut.

Dalam banyak kasus, subjek pajak dan wajib pajak dapat merujuk pada orang yang sama, tetapi dalam sistem resmi tetap perlu dicatat sesuai data administratif.

## NJOP

NJOP adalah Nilai Jual Objek Pajak. NJOP menjadi dasar perhitungan PBB.

Komponen umum:

- NJOP bumi: nilai tanah.
- NJOP bangunan: nilai bangunan.
- NJOP total: gabungan bumi dan bangunan.
- NJOPTKP: Nilai Jual Objek Pajak Tidak Kena Pajak.

PBB dihitung dari nilai objek pajak setelah mengikuti aturan tarif dan pengurang yang berlaku di pemerintah daerah.

## LSPOP dan Data Bangunan

LSPOP adalah data lampiran yang menjelaskan bangunan pada objek pajak.

Data bangunan dapat berisi:

- Nomor bangunan.
- Jenis penggunaan bangunan.
- Luas bangunan.
- Jumlah lantai.
- Tahun bangun/renovasi.
- Kondisi bangunan.
- Konstruksi, atap, dinding, lantai, dan langit-langit.
- Fasilitas bangunan.

Dalam PBB-Ku, data bangunan menjadi dasar fitur laporan perubahan bangunan.

## SPPT

SPPT adalah Surat Pemberitahuan Pajak Terutang. SPPT diterbitkan per tahun pajak untuk menyampaikan jumlah PBB yang harus dibayar.

SPPT biasanya memuat:

- Tahun pajak.
- NOP.
- Nama wajib pajak.
- Nominal PBB terutang.
- Tanggal jatuh tempo.
- Status pembayaran.
- Denda bila ada.

Halaman Histori SPPT menampilkan daftar SPPT per tahun agar pengguna dapat melihat riwayat tagihan.

## Tunggakan

Tunggakan adalah tagihan yang belum lunas atau melewati jatuh tempo.

Dalam aplikasi:

- Tunggakan ditampilkan terpisah agar lebih mudah diprioritaskan.
- Total tunggakan aktif membantu pengguna memahami beban pembayaran.
- Denda ditampilkan bila tersedia dari data SPPT.

## Informasi Pembayaran

PBB-Ku menampilkan arahan pembayaran non-transaksional. Aplikasi tidak memproses pembayaran, tidak membuat virtual account, dan tidak menjadi payment gateway.

Pembayaran resmi tetap dilakukan melalui kanal yang ditetapkan pemerintah daerah atau mitra resmi.

## SSPD

SSPD adalah Surat Setoran Pajak Daerah. Dalam konteks aplikasi, SSPD ditampilkan sebagai bukti visual/prototipe untuk tagihan yang berstatus lunas.

SSPD berkaitan dengan:

- Bukti pembayaran.
- Tahun pajak.
- NOP.
- Nominal.
- Tanggal pembayaran.

Pada MVP, tampilan SSPD tidak menggantikan dokumen resmi yang diterbitkan sistem pemerintah daerah.

## Hubungan Antar Istilah

```text
NIK
  -> mengakses satu atau lebih NOP

NOP
  -> mewakili objek pajak
  -> memiliki data bumi dan bangunan
  -> memiliki histori SPPT

NJOP
  -> dihitung dari nilai bumi dan bangunan
  -> menjadi dasar PBB terutang

SPPT
  -> diterbitkan per tahun pajak
  -> memuat nominal, jatuh tempo, status

Tunggakan
  -> SPPT yang belum lunas/overdue

SSPD
  -> bukti pembayaran untuk tagihan yang sudah lunas
```

## Perubahan Data Bangunan

Jika pengguna menemukan data bangunan tidak sesuai, aplikasi menyediakan draft laporan perubahan.

Contoh perubahan:

- Perubahan luas bangunan.
- Perubahan jumlah lantai.
- Perubahan data bangunan lain.

Draft ini menyusun permohonan verifikasi. Data resmi tetap menunggu pemeriksaan petugas.
