#!/usr/bin/env bash
set -euo pipefail

AVD_NAME="${AVD_NAME:-Pixel_6_API_35}"
APP_ID="${APP_ID:-id.pbbku.mobileportal}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="${REPO_ROOT}/apps/android"

if [[ -n "${ANDROID_HOME:-}" ]]; then
  SDK_DIR="${ANDROID_HOME}"
elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
  SDK_DIR="${ANDROID_SDK_ROOT}"
else
  SDK_DIR="/c/Android/Sdk"
fi

ADB="${SDK_DIR}/platform-tools/adb"
EMULATOR="${SDK_DIR}/emulator/emulator"

if [[ -f "${ADB}.exe" ]]; then
  ADB="${ADB}.exe"
fi

if [[ -f "${EMULATOR}.exe" ]]; then
  EMULATOR="${EMULATOR}.exe"
fi

if [[ ! -x "${ADB}" ]]; then
  echo "adb tidak ditemukan di ${ADB}. Set ANDROID_HOME atau ANDROID_SDK_ROOT."
  exit 1
fi

if [[ ! -x "${EMULATOR}" ]]; then
  echo "emulator tidak ditemukan di ${EMULATOR}. Set ANDROID_HOME atau ANDROID_SDK_ROOT."
  exit 1
fi

has_online_device() {
  "${ADB}" devices | awk 'NR > 1 && $2 == "device" { found = 1 } END { exit found ? 0 : 1 }'
}

echo "Menyiapkan emulator/device untuk PBB-Ku E2E..."

if has_online_device; then
  echo "Device aktif sudah tersedia."
else
  echo "Menyalakan AVD ${AVD_NAME}..."
  "${EMULATOR}" -avd "${AVD_NAME}" -netdelay none -netspeed full >/tmp/pbbku-emulator.log 2>&1 &
fi

"${ADB}" wait-for-device

echo "Menunggu Android selesai boot..."
until [[ "$("${ADB}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
  sleep 2
done

"${ADB}" shell input keyevent 82 >/dev/null 2>&1 || true

cd "${ANDROID_DIR}"
echo "Build dan install debug APK..."
./gradlew :app:installDebug

if [[ "${RESET_APP:-0}" == "1" ]]; then
  echo "Reset data aplikasi..."
  "${ADB}" shell pm clear "${APP_ID}" >/dev/null
fi

echo "Membuka aplikasi ${APP_ID}..."
"${ADB}" shell am start -n "${APP_ID}/.MainActivity" >/dev/null

echo "Siap untuk manual end-to-end testing."
