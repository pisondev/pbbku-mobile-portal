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

print_emulator_log() {
  if [[ -f /tmp/pbbku-emulator.log ]]; then
    echo
    echo "Log emulator terakhir:"
    tail -80 /tmp/pbbku-emulator.log
  fi
}

wait_for_online_device() {
  local emulator_pid="${1:-}"
  local waited=0
  local timeout_seconds="${DEVICE_WAIT_SECONDS:-180}"

  while ! has_online_device; do
    if [[ -n "${emulator_pid}" ]] && ! kill -0 "${emulator_pid}" >/dev/null 2>&1; then
      echo "Emulator berhenti sebelum device siap."
      print_emulator_log
      exit 1
    fi

    if (( waited >= timeout_seconds )); then
      echo "Timeout ${timeout_seconds}s saat menunggu device Android online."
      print_emulator_log
      exit 1
    fi

    sleep 2
    waited=$(( waited + 2 ))
  done
}

wait_for_boot_completed() {
  local emulator_pid="${1:-}"
  local waited=0
  local timeout_seconds="${BOOT_WAIT_SECONDS:-240}"

  until [[ "$("${ADB}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
    if [[ -n "${emulator_pid}" ]] && ! kill -0 "${emulator_pid}" >/dev/null 2>&1; then
      echo "Emulator berhenti sebelum Android selesai boot."
      print_emulator_log
      exit 1
    fi

    if (( waited >= timeout_seconds )); then
      echo "Timeout ${timeout_seconds}s saat menunggu Android selesai boot."
      print_emulator_log
      exit 1
    fi

    sleep 2
    waited=$(( waited + 2 ))
  done
}

echo "Menyiapkan emulator/device untuk PBB-Ku E2E..."

EMULATOR_PID=""
if has_online_device; then
  echo "Device aktif sudah tersedia."
else
  echo "Menyalakan AVD ${AVD_NAME}..."
  "${EMULATOR}" -avd "${AVD_NAME}" -netdelay none -netspeed full >/tmp/pbbku-emulator.log 2>&1 &
  EMULATOR_PID="$!"
fi

wait_for_online_device "${EMULATOR_PID}"

echo "Menunggu Android selesai boot..."
wait_for_boot_completed "${EMULATOR_PID}"

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
