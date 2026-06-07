#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Menjalankan PBB-Ku dari state fresh: onboarding -> login -> beranda."

RESET_APP=1 exec bash "${SCRIPT_DIR}/run_android_e2e.sh"
