#!/usr/bin/env zsh
set -u

export PATH="/usr/bin:/bin:/mingw64/bin:/c/Program Files/Go/bin:/c/Windows/System32:/c/Windows:/c/Windows/System32/Wbem:${PATH:-}"
if [[ -f "${HOME:-}/.zshrc" ]]; then
  source "${HOME}/.zshrc" >/dev/null 2>&1 || true
fi

SCRIPT_DIR="${0:A:h}"
REPO_ROOT="${SCRIPT_DIR:h}"
API_DIR="${REPO_ROOT}/apps/api"
ANDROID_DIR="${REPO_ROOT}/apps/android"
RUN_E2E=0

if [[ "${1:-}" == "--e2e" ]]; then
  RUN_E2E=1
fi

if [[ -t 1 ]]; then
  RED=$'\033[31m'
  GREEN=$'\033[32m'
  YELLOW=$'\033[33m'
  CYAN=$'\033[36m'
  GRAY=$'\033[90m'
  WHITE=$'\033[37m'
  RESET=$'\033[0m'
else
  RED=""
  GREEN=""
  YELLOW=""
  CYAN=""
  GRAY=""
  WHITE=""
  RESET=""
fi

PASS_SUITES=0
FAIL_SUITES=0
TOTAL_PASS=0
TOTAL_FAIL=0
TOTAL_SKIP=0
TOTAL_SECONDS=0

hr() {
  print -P "${GRAY}--------------------------------------------------------------------------------------------${RESET}"
}

duration_text() {
  local seconds="$1"
  awk -v seconds="${seconds}" 'BEGIN { printf "%.4fs", seconds + 0 }'
}

run_suite() {
  local name="$1"
  local workdir="$2"
  local parser="$3"
  shift 3

  local tmp
  tmp="$(mktemp)"
  local start end elapsed exit_code
  start="$(date +%s.%N)"

  (
    cd "${workdir}" &&
    "$@"
  ) >"${tmp}" 2>&1
  exit_code=$?

  end="$(date +%s.%N)"
  elapsed="$(awk -v start="${start}" -v end="${end}" 'BEGIN { printf "%.4f", end - start }')"
  TOTAL_SECONDS="$(awk -v total="${TOTAL_SECONDS}" -v elapsed="${elapsed}" 'BEGIN { printf "%.4f", total + elapsed }')"

  local parsed passed failed skipped detail
  parsed="$("${parser}" "${tmp}")"
  passed="${parsed%%|*}"
  parsed="${parsed#*|}"
  failed="${parsed%%|*}"
  parsed="${parsed#*|}"
  skipped="${parsed%%|*}"
  detail="${parsed#*|}"

  if (( exit_code == 0 && failed == 0 )); then
    PASS_SUITES=$(( PASS_SUITES + 1 ))
    TOTAL_PASS=$(( TOTAL_PASS + passed ))
    TOTAL_SKIP=$(( TOTAL_SKIP + skipped ))
    printf "%s%-28s %-6s %8s  %s%s\n" "${GREEN}" "${name}" "PASS" "$(duration_text "${elapsed}")" "${detail}" "${RESET}"
  else
    FAIL_SUITES=$(( FAIL_SUITES + 1 ))
    TOTAL_PASS=$(( TOTAL_PASS + passed ))
    TOTAL_FAIL=$(( TOTAL_FAIL + failed ))
    TOTAL_SKIP=$(( TOTAL_SKIP + skipped ))
    printf "%s%-28s %-6s %8s  %s%s\n" "${RED}" "${name}" "FAIL" "$(duration_text "${elapsed}")" "${detail}" "${RESET}"
    print -P "${YELLOW}  Ringkasan output terakhir:${RESET}"
    tail -n 25 "${tmp}" | sed 's/^/  /'
  fi

  rm -f "${tmp}"
}

parse_go_json() {
  local file="$1"
  local pass fail skip packages
  pass="$(awk '/"Test":/ && /"Action":"pass"/ { count++ } END { print count + 0 }' "${file}")"
  fail="$(awk '/"Test":/ && /"Action":"fail"/ { count++ } END { print count + 0 }' "${file}")"
  skip="$(awk '/"Test":/ && /"Action":"skip"/ { count++ } END { print count + 0 }' "${file}")"
  packages="$(awk -F'"Package":"' '/"Package":/ { split($2, a, "\""); seen[a[1]]=1 } END { for (item in seen) count++; print count + 0 }' "${file}")"
  print "${pass}|${fail}|${skip}|${pass} pass, ${fail} fail, ${skip} skip, ${packages} package"
}

parse_android_xml() {
  local result_dir="${ANDROID_DIR}/app/build/test-results/testDebugUnitTest"
  if [[ ! -d "${result_dir}" ]]; then
    print "0|1|0|JUnit XML belum tersedia"
    return
  fi

  local totals
  totals="$(awk '
    BEGIN { tests=0; failures=0; errors=0; skipped=0 }
    /<testsuite / {
      if (match($0, /tests="([0-9]+)"/, a)) tests += a[1]
      if (match($0, /failures="([0-9]+)"/, a)) failures += a[1]
      if (match($0, /errors="([0-9]+)"/, a)) errors += a[1]
      if (match($0, /skipped="([0-9]+)"/, a)) skipped += a[1]
    }
    END {
      failed = failures + errors
      passed = tests - failed - skipped
      printf "%d|%d|%d|%d pass, %d fail, %d skip, JVM unit/contract", passed, failed, skipped, passed, failed, skipped
    }
  ' "${result_dir}"/TEST-*.xml)"
  print "${totals}"
}

parse_static() {
  local _file="$1"
  print "0|0|0|lintDebug clean"
}

parse_e2e() {
  local _file="$1"
  print "0|0|0|emulator install/open flow completed"
}

print -P "${CYAN}PBB-Ku Test Report${RESET}"
print -P "${GRAY}Generated: $(date '+%Y-%m-%d %H:%M:%S %z')${RESET}"
print -P "${GRAY}Repo     : ${REPO_ROOT}${RESET}"
hr
printf "%s%-28s %-6s %8s  %s%s\n" "${WHITE}" "Suite" "Status" "Durasi" "Insight" "${RESET}"
hr

run_suite "API unit" "${API_DIR}" parse_go_json go test ./tests/unit -count=1 -json
run_suite "API integration" "${API_DIR}" parse_go_json go test ./tests/integration -count=1 -json
run_suite "API functional" "${API_DIR}" parse_go_json go test ./tests/functional -count=1 -json
run_suite "Android JVM tests" "${ANDROID_DIR}" parse_android_xml ./gradlew :app:testDebugUnitTest --offline
run_suite "Android lint" "${ANDROID_DIR}" parse_static ./gradlew :app:lintDebug --offline

if (( RUN_E2E == 1 )); then
  run_suite "Android manual E2E prep" "${REPO_ROOT}" parse_e2e bash scripts/run_android_e2e.sh
fi

hr
if (( FAIL_SUITES == 0 )); then
  print -P "${GREEN}OVERALL PASS  Suites ${PASS_SUITES}/${PASS_SUITES}, assertions ${TOTAL_PASS} pass, ${TOTAL_FAIL} fail, ${TOTAL_SKIP} skip, duration $(duration_text "${TOTAL_SECONDS}")${RESET}"
  print -P "${CYAN}Coverage: API unit/integration/functional, Android JVM unit/functional/nonfunctional contracts, Android lint.${RESET}"
  if (( RUN_E2E == 0 )); then
    print -P "${CYAN}Optional: jalankan ./scripts/test.sh --e2e untuk install/open emulator dan bukti runtime manual.${RESET}"
  fi
  exit 0
fi

TOTAL_SUITES=$(( PASS_SUITES + FAIL_SUITES ))
print -P "${RED}OVERALL FAIL  Suites ${PASS_SUITES}/${TOTAL_SUITES}, assertions ${TOTAL_PASS} pass, ${TOTAL_FAIL} fail, ${TOTAL_SKIP} skip, duration $(duration_text "${TOTAL_SECONDS}")${RESET}"
exit 1
