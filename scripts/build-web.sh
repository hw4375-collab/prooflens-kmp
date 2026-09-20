#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${1:-$ROOT_DIR/composeApp/build/dist/wasmJs/productionExecutable}"

"$ROOT_DIR/gradlew" :composeApp:wasmJsBrowserDistribution
echo "Wasm distribution: $DIST_DIR"
echo "Serve with: python3 -m http.server 8081 -d $DIST_DIR"
