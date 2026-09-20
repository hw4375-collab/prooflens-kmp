#!/usr/bin/env bash
set -euo pipefail
./gradlew :composeApp:assembleDebug
find composeApp/build/outputs/apk -name '*.apk' -print
