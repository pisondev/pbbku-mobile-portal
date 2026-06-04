# Dokumentasi API Postman

Folder ini berisi Postman Collection dan Environment untuk API deployment PBB-Ku.

## File

- `PBBKu_Deployment_API.postman_collection.json`
- `PBBKu_Deployment_API.postman_environment.json`

## Cara Pakai

1. Import collection dan environment ke Postman.
2. Pilih environment `PBB-Ku Deployment API - Ready To Run`.
3. Jalankan request dari atas ke bawah, atau klik Run pada folder section tertentu.

Semua request sudah diarahkan ke deployment:

```text
https://pbbku-api.tierratie.com
```

Tidak perlu mengubah base URL, NOP, tahun pajak, atau filter demo. Endpoint write `POST /api/rpc/objekPajak/save` sengaja dites sebagai akses admin tertolak agar collection tetap bisa dijalankan tanpa secret.
