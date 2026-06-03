# AI Agent VPS Operations Notes

Dokumen ini merangkum pola kerja, jebakan command, dan checklist verifikasi saat agent perlu memeriksa deployment API di VPS. File ini sengaja dibuat general agar bisa diduplikasi ke project lain.

## Tujuan

- Memisahkan masalah lokal, Android/client, DNS, reverse proxy, container, dan database.
- Mengurangi trial-and-error saat shell Windows, PowerShell, MSYS2, SSH, Docker, dan curl punya perilaku quoting berbeda.
- Menjaga inspeksi VPS tetap read-only kecuali user memang meminta deploy/perubahan.

## Urutan Aman Saat Audit Deployment

1. Validasi aplikasi dari luar VPS lebih dulu.
   - Cek `https://domain/health`.
   - Cek endpoint utama dengan payload yang sama seperti client.
   - Cek endpoint yang seharusnya ditolak, misalnya write endpoint tanpa admin key.
   - Cek security headers dengan `curl -I`.

2. Validasi integrasi client lokal.
   - Pastikan base URL client mengarah ke domain production yang benar.
   - Pastikan path endpoint client cocok dengan route backend.
   - Pastikan format request dan response sama, misalnya wrapper `{ "json": ... }`.

3. Baru masuk VPS.
   - Cek folder project.
   - Cek container/process.
   - Cek port binding.
   - Cek env non-sensitif.
   - Cek log startup.
   - Cek reverse proxy mengarah ke port yang sama.

4. Ulangi smoke test publik setelah hasil VPS terlihat konsisten.

## Catatan Shell dan SSH

Pada Windows, jangan mengasumsikan semua SSH client membaca konfigurasi yang sama.

- MSYS2 `zsh` bisa membaca `~/.ssh/config` milik environment MSYS2.
- Windows OpenSSH bisa gagal membaca alias yang sama, misalnya `vps-hestia`, karena path home/config berbeda.
- Windows OpenSSH juga bisa memakai `known_hosts` berbeda dan memunculkan peringatan `REMOTE HOST IDENTIFICATION HAS CHANGED`.
- Jangan bypass host key warning secara otomatis. Jika host key berubah, laporkan fingerprint dan minta user memastikan perubahan itu sah.

Contoh command MSYS2 yang stabil:

```powershell
C:\msys64\usr\bin\zsh.exe -lc "ssh -o BatchMode=yes -o ConnectTimeout=10 vps-hestia pwd"
C:\msys64\usr\bin\zsh.exe -lc "ssh -o BatchMode=yes -o ConnectTimeout=10 vps-hestia docker ps"
C:\msys64\usr\bin\zsh.exe -lc "ssh -o BatchMode=yes -o ConnectTimeout=10 vps-hestia ls -la projects"
```

Jika command remote mengandung quote kompleks, kurangi kompleksitasnya. Hindari format string Docker yang penuh kurung kurawal dari PowerShell menuju MSYS2 karena mudah pecah. Mulai dari `docker ps` biasa, lalu persempit dengan command yang lebih sederhana.

## Catatan Quoting Payload HTTP

PowerShell dan `curl.exe` mudah merusak payload JSON nested jika body ditulis manual di argumen command. Gejalanya bisa berupa error API seperti:

```text
invalid character 'j' looking for beginning of object key string
invalid character '\\' looking for beginning of object key string
```

Untuk payload non-trivial di PowerShell, lebih aman pakai `Invoke-RestMethod` dan `ConvertTo-Json`:

```powershell
Invoke-RestMethod `
  -Uri 'https://example.com/api/rpc/objekPajak/search' `
  -Method Post `
  -ContentType 'application/json' `
  -Body (@{json=@{query='BUDI';limit=5}} | ConvertTo-Json -Compress) |
  ConvertTo-Json -Depth 8 -Compress
```

Untuk payload kosong sederhana, `curl.exe` masih cukup:

```powershell
curl.exe -s -X POST -H 'Content-Type: application/json' --data '{"json":{}}' https://example.com/api/rpc/wilayah/listPropinsi
```

## Checklist Docker dan Reverse Proxy

Saat API dijalankan di Docker pada VPS, cek hal berikut:

- Container API `Up`, bukan restart loop.
- Container database `healthy` jika memakai healthcheck.
- Port API dibind ke loopback jika reverse proxy yang membuka akses publik, misalnya `127.0.0.1:3012->8080/tcp`.
- Database tidak perlu terbuka publik; idealnya juga bind loopback atau network internal.
- Env API minimal:
  - listen address, misalnya `PBBKU_API_ADDR=:8080`
  - database driver, misalnya `postgres`
  - path migrasi dan seed
  - body limit
  - CORS allowed origins
  - flag public write tidak aktif
- Reverse proxy Hestia/Nginx mengarah ke port host yang sama dengan Docker publish.

Contoh command read-only:

```zsh
ssh vps-hestia docker ps
ssh vps-hestia docker exec api-api-1 printenv PBBKU_DB_DRIVER PBBKU_API_ADDR PBBKU_MIGRATIONS_PATH PBBKU_SEED_FILE PBBKU_MAX_BODY_BYTES PBBKU_ALLOWED_ORIGINS PBBKU_ALLOW_PUBLIC_SAVE
ssh vps-hestia docker logs --tail 30 api-api-1
ssh vps-hestia curl -s http://127.0.0.1:3012/health
ssh vps-hestia grep -R 3012 conf/web/example.com web/example.com 2>/dev/null
```

Jangan mencetak env sensitif seperti database password, admin API key, token, atau private key ke chat kecuali user secara eksplisit meminta dan memang perlu.

## Checklist Public Smoke Test

Minimal cek dari mesin lokal:

```powershell
curl.exe -s https://example.com/health
curl.exe -s -I https://example.com/health
```

Lalu cek endpoint read-only utama dengan payload client asli:

- daftar referensi dasar
- search/list
- detail resource utama
- detail turunan
- skenario data kosong
- write endpoint tanpa key harus ditolak
- POST tanpa `Content-Type: application/json` harus ditolak

Untuk API publik di balik Cloudflare, header `Server: cloudflare` dan `cf-cache-status: DYNAMIC` wajar. Yang penting response aplikasi, security headers, dan status endpoint sesuai kontrak.

## Red Flags

- Public domain OK, tetapi `curl` lokal VPS gagal ke `127.0.0.1:port`: reverse proxy mungkin melayani service lain atau cache lama.
- Container API publish ke `0.0.0.0` padahal sudah ada reverse proxy: permukaan serangan lebih besar.
- Write endpoint publik aktif tanpa admin key.
- Body size tidak dibatasi.
- CORS wildcard pada API yang punya credential atau write endpoint.
- Database publish ke publik tanpa kebutuhan jelas.
- Windows SSH host key warning muncul setelah mencoba IP langsung: jangan bypass otomatis.

## Catatan dari Audit PBB-Ku

Temuan spesifik yang berguna sebagai contoh:

- Domain publik `pbbku-api.tierratie.com` sehat di `/health`.
- Android memakai base URL `https://pbbku-api.tierratie.com/api/rpc/`.
- API container berjalan di Docker sebagai `api-api-1`.
- Postgres berjalan sebagai `api-postgres-1` dan healthy.
- API bind ke `127.0.0.1:3012->8080/tcp`.
- Hestia/Nginx proxy pass ke `http://127.0.0.1:3012`.
- API memakai `PBBKU_DB_DRIVER=postgres`.
- `PBBKU_ALLOW_PUBLIC_SAVE` kosong, sehingga write endpoint publik tidak dibuka.
- Endpoint write `objekPajak/save` menolak request tanpa admin key.
- Security headers publik tersedia: `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, dan `Cache-Control`.
