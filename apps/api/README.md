# PBB-Ku Internal API

Server Go ini menggantikan ketergantungan aplikasi pada API eksternal SIMPBB untuk kebutuhan demo, testing, dan deployment mandiri. Kontrak endpoint dibuat kompatibel dengan pola oRPC eksternal:

```json
{
  "json": {
    "kdPropinsi": "51"
  }
}
```

Semua endpoint utama tersedia di prefix:

```text
http://localhost:8080/api/rpc
```

Dokumentasi tambahan:

- `docs/postman/`: Postman Collection dan Environment deployment PBB-Ku.
- `docs/diagram/pbbku_internal_api.dbml`: DBML database internal untuk generate ERD di dbdiagram.io.

## Menjalankan Lokal

Mode lokal default memakai SQLite:

```powershell
cd apps/api
go run ./cmd/server serve
```

Default `serve` akan menjalankan migrasi dan mengisi seed demo jika database masih kosong. Database default tersimpan di `apps/api/data/pbbku.db`.

Perintah terpisah:

```powershell
go run ./cmd/server migrate
go run ./cmd/server seed
go run ./cmd/server serve -addr :8080
```

Konfigurasi environment:

```text
PBBKU_API_ADDR=:8080
PBBKU_DB_DRIVER=sqlite
PBBKU_DB_PATH=data/pbbku.db
PBBKU_MIGRATIONS_PATH=migrations
PBBKU_SEED_FILE=seeds/demo.sql
```

## Database

API mendukung dua mode database:

- `sqlite`: default lokal, cocok untuk pengembangan cepat, fixture demo, dan pola data lokal/mobile/offline.
- `postgres`: mode server/VPS, cocok untuk deployment multi-user dan persistensi produksi.

Menjalankan dengan PostgreSQL tanpa Docker:

```powershell
$env:PBBKU_DB_DRIVER="postgres"
$env:PBBKU_DATABASE_URL="postgres://pbbku:pbbku@localhost:5432/pbbku?sslmode=disable"
go run ./cmd/server serve
```

Catatan mobile: aplikasi Android sudah memakai Room/SQLite untuk cache, session, preferensi, notifikasi lokal, dan draft. Server API ini tetap menyediakan mode SQLite agar data dummy/offline bisa dijalankan ringan saat development atau pengujian mobile, sedangkan deployment VPS diarahkan ke PostgreSQL.

## Docker

Jalankan API plus PostgreSQL:

```powershell
cd apps/api
docker compose up --build
```

Service yang berjalan:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`, database/user/password `pbbku`

Port host bisa diganti tanpa mengubah file compose:

```powershell
$env:PBBKU_API_PORT="3012"
$env:PBBKU_POSTGRES_PORT="5435"
docker compose up -d --build
```

Port API dan PostgreSQL dibind ke `127.0.0.1` agar tidak terbuka langsung ke internet. Untuk produksi, buka akses publik lewat reverse proxy HTTPS.

Container API memakai environment:

```text
PBBKU_DB_DRIVER=postgres
PBBKU_DATABASE_URL=postgres://pbbku:pbbku@postgres:5432/pbbku?sslmode=disable
```

## Security

Default API aman untuk mode baca publik dan menutup endpoint write:

- Semua response diberi header `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, dan `Cache-Control`.
- Request `POST` wajib memakai `Content-Type: application/json`.
- Body request dibatasi oleh `PBBKU_MAX_BODY_BYTES`, default 1 MB.
- CORS bisa dibatasi dengan `PBBKU_ALLOWED_ORIGINS`, format comma-separated.
- Endpoint `POST /api/rpc/objekPajak/save` disabled secara default kecuali diberi admin key.

Environment security:

```text
PBBKU_ALLOWED_ORIGINS=https://app.example.com,http://localhost:3000
PBBKU_ADMIN_API_KEY=change-this-long-random-secret
PBBKU_MAX_BODY_BYTES=1048576
```

Untuk mengakses endpoint write, kirim header:

```text
X-Admin-API-Key: change-this-long-random-secret
```

`PBBKU_ALLOW_PUBLIC_SAVE=true` hanya untuk testing lokal, bukan deployment.

## REST API

Kontrak oRPC lama tetap tersedia di `/api/rpc` untuk kompatibilitas Android. API juga menyediakan REST read-only di `/api/v1`:

- `GET /api/v1/wilayah/propinsi`
- `GET /api/v1/wilayah/dati2?kdPropinsi=51`
- `GET /api/v1/wilayah/kecamatan?kdPropinsi=51&kdDati2=71`
- `GET /api/v1/wilayah/kelurahan?kdPropinsi=51&kdDati2=71&kdKecamatan=010`
- `GET /api/v1/wilayah/blok?kdPropinsi=51&kdDati2=71&kdKecamatan=010&kdKelurahan=001`
- `GET /api/v1/objek-pajak/search?query=BUDI&limit=10`
- `GET /api/v1/objek-pajak?kdPropinsi=51&kdDati2=71&limit=10&offset=0`
- `GET /api/v1/objek-pajak/51.71.010.001.054.0032.0`
- `GET /api/v1/objek-pajak/51.71.010.001.054.0032.0/bangunan`
- `GET /api/v1/objek-pajak/51.71.010.001.054.0032.0/bangunan/1/fasilitas`
- `GET /api/v1/objek-pajak/51.71.010.001.054.0032.0/sppt`
- `GET /api/v1/objek-pajak/51.71.010.001.054.0032.0/tunggakan`
- `GET /api/v1/sppt/51.71.010.001.054.0032.0/2026`
- `GET /api/v1/sppt?thnPajak=2026&kdPropinsi=51&statusPembayaran=0`

## Endpoint

- `GET /health`
- `POST /api/rpc/wilayah/listPropinsi`
- `POST /api/rpc/wilayah/listDati2`
- `POST /api/rpc/wilayah/listKecamatan`
- `POST /api/rpc/wilayah/listKelurahan`
- `POST /api/rpc/wilayah/listBlok`
- `POST /api/rpc/objekPajak/search`
- `POST /api/rpc/objekPajak/listDetails`
- `POST /api/rpc/objekPajak/getByNop`
- `POST /api/rpc/objekPajak/save`
- `POST /api/rpc/objekPajak/getSpptHistory`
- `POST /api/rpc/objekPajak/getTunggakan`
- `POST /api/rpc/objekPajak/getNextNoUrut`
- `POST /api/rpc/objekPajak/getNextNoFormulir`
- `POST /api/rpc/lspop/listByNop`
- `POST /api/rpc/lspop/getBuilding`
- `POST /api/rpc/lspop/listFasilitas`
- `POST /api/rpc/lspop/nextNoBng`
- `POST /api/rpc/sppt/listByNop`
- `POST /api/rpc/sppt/get`
- `POST /api/rpc/sppt/list`

## Data Demo Penting

NOP lengkap dengan bangunan, fasilitas, SPPT lunas, SPPT belum bayar, dan tunggakan:

```text
51.71.010.001.054.0032.0
```

NOP demo lama dari aplikasi Android juga tersedia dan dibuat lebih lengkap:

```text
32.04.010.001.001.0001.0
```

Wilayah `51/71` dan `32/04` sama-sama lengkap sampai blok, sehingga masalah response kosong pada API eksternal tidak terjadi di server internal.

## Deploy VPS

Rekomendasi VPS: pakai PostgreSQL. Opsi paling sederhana adalah Docker Compose:

```bash
cd apps/api
docker compose up -d --build
```

Atau build binary langsung di VPS:

```bash
cd apps/api
go build -o pbbku-api ./cmd/server
export PBBKU_DB_DRIVER=postgres
export PBBKU_DATABASE_URL='postgres://pbbku:pbbku@localhost:5432/pbbku?sslmode=disable'
./pbbku-api migrate
./pbbku-api seed
./pbbku-api serve -addr :8080
```

Untuk build Windows lokal:

```powershell
cd apps/api
go build -o pbbku-api.exe ./cmd/server
```

Pastikan folder `migrations/` dan `seeds/` ikut dikirim bersama binary, atau jalankan dari root `apps/api`.

## Android Client

Android tetap memakai kontrak oRPC `/api/rpc` agar mapper/repository yang sudah ada tidak perlu diubah. Default build saat ini mengarah ke API produksi:

```text
https://pbbku-api.tierratie.com/api/rpc/
```

Untuk menjalankan Android emulator ke API lokal:

```powershell
cd apps/android
.\gradlew.bat :app:assembleDebug -PPBBKU_API_BASE_URL=http://10.0.2.2:8080/api/rpc/
```

## Postman dan ERD

Import file berikut untuk eksplorasi deployment API internal:

1. `docs/postman/PBBKu_Deployment_API.postman_collection.json`
2. `docs/postman/PBBKu_Deployment_API.postman_environment.json`

Untuk ERD, buka dbdiagram.io lalu import/copy isi:

```text
docs/diagram/pbbku_internal_api.dbml
```

## Testing

Test dibagi menjadi tiga folder:

- `tests/unit`: helper konfigurasi dan placeholder database.
- `tests/integration`: migrasi dan seed SQLite, plus PostgreSQL jika `PBBKU_TEST_POSTGRES_URL` tersedia.
- `tests/functional`: alur endpoint oRPC utama lewat `httptest`.

Menjalankan semua test:

```powershell
cd apps/api
go test ./...
```

Menjalankan per kategori:

```powershell
go test ./tests/unit
go test ./tests/integration
go test ./tests/functional
```

Integration test PostgreSQL opsional:

```powershell
$env:PBBKU_TEST_POSTGRES_URL="postgres://pbbku:pbbku@localhost:5432/pbbku_test?sslmode=disable"
go test ./tests/integration
```
