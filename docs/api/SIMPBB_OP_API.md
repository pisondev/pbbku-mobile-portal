# Rangkuman SIMPBB OP API

Dokumen ini merangkum dokumentasi **SIMPBB OP API** untuk kebutuhan integrasi aplikasi **PBB-Ku** dan persiapan Postman Collection. API ini digunakan sebagai sumber data eksternal untuk objek pajak, bangunan/LSPOP, wilayah, SPPT, tunggakan, dan beberapa helper generator.

## 1. Konfigurasi Dasar

| Parameter | Nilai |
|---|---|
| Base URL | `https://simpbb.technosmart.id/api/rpc` |
| Protocol | `oRPC` over HTTP POST |
| Auth Strategy | `PUBLIC` untuk router yang terdokumentasi |
| Content-Type | `application/json` |

Semua endpoint menggunakan method **POST** dan body JSON dengan wrapper oRPC.

## 2. Format Request dan Response

### Request Wrapper

```json
{
  "json": {
    "param1": "value",
    "param2": 123
  }
}
```

### Response Wrapper

```json
{
  "json": {
    "data": [],
    "message": "Success"
  }
}
```

## 3. Format `NOP_OBJECT`

Hampir semua endpoint detail objek pajak, LSPOP, dan SPPT membutuhkan format `NOP_OBJECT`. Semua kode wilayah harus dikirim sebagai **string** agar leading zero tetap terjaga.

```json
{
  "kdPropinsi": "32",
  "kdDati2": "04",
  "kdKecamatan": "010",
  "kdKelurahan": "001",
  "kdBlok": "001",
  "noUrut": "0001",
  "kdJnsOp": "0"
}
```

Catatan penting:

- Kirim kode seperti `"001"`, bukan `"1"`.
- Field luas bumi, luas bangunan, dan nilai/NJOP menggunakan tipe data number.
- Ambil response data dari field `json`, misalnya `result.json`.

## 4. Ringkasan Endpoint

### 4.1 Objek Pajak Router — `objekPajak`

Router utama untuk pengelolaan data SPOP dan Subjek Pajak/WP.

| Endpoint | Kegunaan | Input Utama |
|---|---|---|
| `POST /objekPajak/search` | Mencari NOP atau nama wajib pajak untuk autocomplete UI. | `query`, `limit?` |
| `POST /objekPajak/listDetails` | Menampilkan daftar NOP dengan agregasi luas bumi dan bangunan. | `kdPropinsi?`, `kdDati2?`, `limit`, `offset`, `search?` |
| `POST /objekPajak/getByNop` | Mengambil profil lengkap satu NOP, termasuk data subjek pajak dan status keanggotaan. | `NOP_OBJECT` |
| `POST /objekPajak/save` | Menyimpan atau memperbarui data SPOP dan Subjek Pajak dalam satu transaksi. | `spop`, `subjekPajak`, `anggota?` |
| `POST /objekPajak/getSpptHistory` | Melihat histori tagihan SPPT dari tahun ke tahun untuk satu NOP. | `NOP_OBJECT` |
| `POST /objekPajak/getTunggakan` | Mengambil daftar SPPT belum lunas. | `NOP_OBJECT` |
| `POST /objekPajak/getNextNoUrut` | Mendapatkan nomor urut NOP berikutnya pada satu blok. | Kode wilayah sampai `kdBlok` |
| `POST /objekPajak/getNextNoFormulir` | Mendapatkan nomor formulir SPOP berikutnya untuk tahun berjalan. | `{}` |

### 4.2 LSPOP Router — `lspop`

Router untuk manajemen data bangunan atau Lampiran SPOP.

| Endpoint | Kegunaan | Input Utama |
|---|---|---|
| `POST /lspop/listByNop` | Mengambil semua daftar bangunan pada satu NOP beserta detail JPB. | `NOP_OBJECT` |
| `POST /lspop/getBuilding` | Mengambil detail spesifik satu bangunan. | `NOP_OBJECT`, `noBng` |
| `POST /lspop/listFasilitas` | Mengambil daftar fasilitas pada satu bangunan, seperti AC, lift, kolam renang, dan lainnya. | `NOP_OBJECT`, `noBng` |
| `POST /lspop/nextNoBng` | Mendapatkan nomor bangunan berikutnya untuk satu NOP. | `NOP_OBJECT` |

### 4.3 Wilayah Router — `wilayah`

Router helper untuk data referensi wilayah geografis.

| Endpoint | Kegunaan | Input Utama |
|---|---|---|
| `POST /wilayah/listPropinsi` | Mengambil daftar provinsi. | `{}` |
| `POST /wilayah/listDati2` | Mengambil daftar kabupaten/kota. | `kdPropinsi` |
| `POST /wilayah/listKecamatan` | Mengambil daftar kecamatan. | `kdPropinsi`, `kdDati2` |
| `POST /wilayah/listKelurahan` | Mengambil daftar kelurahan. | `kdPropinsi`, `kdDati2`, `kdKecamatan` |
| `POST /wilayah/listBlok` | Mengambil daftar blok pada satu kelurahan. | `kdPropinsi`, `kdDati2`, `kdKecamatan`, `kdKelurahan` |

### 4.4 SPPT Router — `sppt`

Router untuk akses data tagihan dan histori pembayaran pajak.

| Endpoint | Kegunaan | Input Utama |
|---|---|---|
| `POST /sppt/listByNop` | Mengambil histori tagihan pajak untuk satu NOP dari tahun ke tahun. | `NOP_OBJECT` |
| `POST /sppt/get` | Mengambil detail tagihan satu tahun pajak tertentu. | `NOP_OBJECT`, `thnPajakSppt` |
| `POST /sppt/list` | Pencarian SPPT massal dengan filter tahun, wilayah, atau status bayar. | `thnPajak`, `kdPropinsi`, `statusPembayaran`, `limit`, `offset` |

## 5. Contoh Pola Request

### Search NOP/Nama WP

```bash
curl -X POST {{base_url}}/objekPajak/search \
  -H "Content-Type: application/json" \
  -d '{"json": {"query": "WAYAN SUTARJA", "limit": 5}}'
```

### Detail NOP

```bash
curl -X POST {{base_url}}/objekPajak/getByNop \
  -H "Content-Type: application/json" \
  -d '{
    "json": {
      "kdPropinsi": "32",
      "kdDati2": "04",
      "kdKecamatan": "010",
      "kdKelurahan": "001",
      "kdBlok": "001",
      "noUrut": "0001",
      "kdJnsOp": "0"
    }
  }'
```

### Detail SPPT Tahun Pajak

```bash
curl -X POST {{base_url}}/sppt/get \
  -H "Content-Type: application/json" \
  -d '{
    "json": {
      "kdPropinsi": "32",
      "kdDati2": "04",
      "kdKecamatan": "010",
      "kdKelurahan": "001",
      "kdBlok": "001",
      "noUrut": "0001",
      "kdJnsOp": "0",
      "thnPajakSppt": 2024
    }
  }'
```

## 6. Catatan untuk Integrasi PBB-Ku

- Semua request Postman pada collection menggunakan variable environment seperti `{{base_url}}`, `{{kdPropinsi}}`, `{{kdDati2}}`, dan seterusnya.
- Untuk fitur portal wajib pajak, endpoint yang paling prioritas adalah `objekPajak/search`, `objekPajak/getByNop`, `objekPajak/getSpptHistory`, `objekPajak/getTunggakan`, `lspop/listByNop`, `sppt/listByNop`, dan `sppt/get`.
- Endpoint write seperti `objekPajak/save` disediakan di collection untuk kelengkapan dokumentasi, tetapi sebaiknya tidak digunakan untuk demo portal wajib pajak kecuali memang ada otorisasi dan skenario khusus.
- Karena API menggunakan auth strategy `PUBLIC` pada dokumentasi, environment Postman tidak menambahkan bearer token secara default.
