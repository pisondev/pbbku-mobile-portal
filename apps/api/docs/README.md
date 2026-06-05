# PBB-Ku API Docs

Folder ini berisi dokumentasi teknis khusus API internal PBB-Ku.

## Isi

- `postman/`: Postman Collection dan Environment untuk deployment `https://pbbku-api.tierratie.com`.
- `diagram/pbbku_internal_api.dbml`: DBML database internal untuk generate ERD di dbdiagram.io.

API internal memakai kontrak oRPC kompatibel SIMPBB pada `/api/rpc` dan REST read-only pada `/api/v1`. Detail endpoint, mode database, security, dan deployment tersedia di `../README.md`.
