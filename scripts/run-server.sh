#!/usr/bin/env bash
set -euo pipefail
export PATH="$HOME/.elan/bin:$PATH"
export LEAN_PROJECT_DIR="${LEAN_PROJECT_DIR:-$(pwd)/lean}"
./gradlew :server:run
