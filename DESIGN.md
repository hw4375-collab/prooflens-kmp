# ProofLens — Design Spec (hackathon build)

**One-liner:** ProofLens turns AI answers into Lean 4 theorems and lets the Lean kernel decide
whether they are true. AI proposes, Lean verifies. Cross-platform via Kotlin Multiplatform.

Hackathon: Build with JetBrains @ NYUAD, Kotlin Multiplatform Challenge (>= 2 platforms).
Judging: Working Product 35%, Problem & Insight 25%, Technical Execution 25%, Pitch 15%.

## Problem
LLMs hallucinate confidently, especially in math/logic. Students and researchers can't tell a
correct AI answer from a fluent wrong one. Formal verification (Lean 4 + Mathlib) gives a
machine-checked yes/no, but is inaccessible to non-experts and lives on desktop only.

## Solution
A KMP app (Android + Desktop JVM + Web/Wasm) with a Kotlin Ktor backend that:
1. **Formalize** — sends a natural-language claim (or a pasted AI answer) to an LLM, asks for a
   Lean 4 `theorem` with a proof.
2. **Verify** — compiles it with the real Lean 4 toolchain on the server.
3. **Repair loop** — if Lean rejects, feed the diagnostics back to the LLM, retry up to N=3.
4. **Report** — VERIFIED / REFUTED (negation proved) / UNVERIFIED, with Lean code and
   diagnostics shown to the user. Every step of the loop is visible ("AI proposes, Lean decides").
5. **Learn Lean** — bite-size lesson cards; each has a Lean snippet the user can edit and run
   against the same verifier.

## Repo layout (Gradle multi-module, Kotlin DSL, version catalog)

```
prooflens-kmp/
  settings.gradle.kts            # repos: google(), maven-central GCS mirror, mavenCentral()
  gradle/libs.versions.toml
  shared/                        # KMP library: android, jvm, wasmJs
    src/commonMain/kotlin/dev/prooflens/shared/
      model/   Models.kt         # data classes below (kotlinx.serialization)
      api/     ProofLensApi.kt   # Ktor client wrapper: formalize/verify/health
      lessons/ Lessons.kt        # hard-coded Learn-Lean content (List<Lesson>)
      demo/    DemoExamples.kt   # bundled examples incl. pre-baked results for offline demo
      logic/   VerdictLogic.kt   # pure functions: parse Lean output -> Diagnostics, decide Verdict
    src/commonTest/kotlin/...    # tests for VerdictLogic
  composeApp/                    # Compose Multiplatform app: androidApp target, desktop (jvm), wasmJs
    src/commonMain/kotlin/dev/prooflens/app/
      App.kt                     # root: theme + bottom nav (Verify | Learn | History | Settings)
      screens/VerifyScreen.kt, LearnScreen.kt, HistoryScreen.kt, SettingsScreen.kt
      ui/ VerdictBadge.kt, LeanCodeBlock.kt, StepTimeline.kt
      state/ AppViewModel.kt     # plain class with StateFlow, no platform VM deps
    src/androidMain/  MainActivity.kt, AndroidManifest.xml (INTERNET permission, usesCleartextTraffic=true)
    src/desktopMain/  main.kt (Window title "ProofLens")
    src/wasmJsMain/   main.kt + resources/index.html
  server/                        # Ktor JVM server (Netty), CORS open, JSON
    src/main/kotlin/dev/prooflens/server/
      Application.kt             # routes: GET /health, POST /formalize, POST /verify, POST /check (full loop)
      LeanRunner.kt              # writes temp .lean into lean/ project, runs `lake env lean <file>` with timeout 60s
      OpenAiFormalizer.kt        # OpenAI chat completions (model gpt-4o-mini default, env OPENAI_MODEL), reads OPENAI_API_KEY
      Prompts.kt                 # system prompt: Lean 4 syntax, no Mathlib unless available, return JSON {lean, explanation}
  lean/                          # Lake project "ProofLens" (Lean 4 stable via elan; lean-toolchain pinned)
    lakefile.toml, lean-toolchain, ProofLens/Basic.lean (smoke theorem)
  scripts/ run-server.sh, run-desktop.sh, build-apk.sh, build-web.sh
  README.md, PITCH.md, DEMO.md
```

## Versions (pin these; use whatever the Gradle cache + mirror resolves)
- Gradle wrapper 8.9 (or 8.7 if 8.9 can't download; existing cache has 8.7)
- Kotlin 2.1.21, Compose Multiplatform 1.8.2 (needs Kotlin >= 2.1.20 for wasm)
- AGP 8.7.3, compileSdk 34 (only android-34 platform installed), minSdk 26, targetSdk 34, JDK 17
- Ktor 3.1.3 (client: core, content-negotiation, serialization-kotlinx-json; engines: okhttp (android), cio (jvm), js (wasmJs)); server: netty, content-negotiation, cors, call-logging
- kotlinx-serialization 1.8.1, kotlinx-coroutines 1.10.2
- androidx.activity:activity-compose 1.9.3

## Shared models (commonMain, @Serializable)
```kotlin
enum class Verdict { VERIFIED, REFUTED, UNVERIFIED, ERROR }
data class Diagnostic(val line: Int, val col: Int, val severity: String, val message: String)
data class FormalizeRequest(val claim: String, val previousLean: String? = null, val previousErrors: List<Diagnostic> = emptyList())
data class FormalizeResponse(val lean: String, val explanation: String, val model: String)
data class VerifyRequest(val lean: String)
data class VerifyResponse(val ok: Boolean, val diagnostics: List<Diagnostic>, val rawOutput: String, val durationMs: Long)
data class CheckRequest(val claim: String, val maxAttempts: Int = 3)
data class CheckAttempt(val attempt: Int, val lean: String, val explanation: String, val verify: VerifyResponse)
data class CheckResponse(val claim: String, val verdict: Verdict, val attempts: List<CheckAttempt>, val summary: String)
data class HistoryItem(val id: String, val claim: String, val verdict: Verdict, val timestampMs: Long, val response: CheckResponse)
data class Lesson(val id: String, val title: String, val blurb: String, val leanSnippet: String, val tactic: String)
```

## Verdict logic (shared/logic/VerdictLogic.kt, pure, unit-tested)
- `parseLeanOutput(raw: String): List<Diagnostic>` — parse lines like `path:LINE:COL: error: msg` (also `warning:`); multi-line messages append to previous diagnostic.
- `isOk(diagnostics)`: no `error` severity and raw output does not contain `sorry` warning (`declaration uses 'sorry'` counts as failure).
- Verdict decision in the /check loop: if the LLM returns a proof of the claim and Lean accepts -> VERIFIED. Prompt also allows the LLM to return a proof of the **negation** (`theorem ... : ¬ (...)`) with JSON field `"provesNegation": true` -> if Lean accepts -> REFUTED. After maxAttempts without success -> UNVERIFIED. Server/Lean failure -> ERROR.

## Server details
- `LeanRunner`: LEAN_PROJECT_DIR env (default `../lean` relative to server cwd; scripts set it absolute). Writes `Scratch/<uuid>.lean`, runs `lake env lean <file>` in that dir, 60s timeout, captures stdout+stderr, deletes file. If `lake` missing -> respond ERROR with message "Lean toolchain not installed".
- `OpenAiFormalizer`: POST https://api.openai.com/v1/chat/completions, `response_format: {type: json_object}`, temperature 0.2. If OPENAI_API_KEY missing -> 503 with clear message; client then falls back to demo examples.
- `/check` runs formalize -> verify -> (repair with previousLean+previousErrors) up to maxAttempts; returns all attempts so UI can render the timeline.
- Server port 8080, bind 0.0.0.0. CORS anyHost (hackathon).

## Lean project
- Install via elan: `curl -sSf https://raw.githubusercontent.com/leanprover/elan/master/elan-init.sh | sh -s -- -y --default-toolchain none`, then `lean-toolchain` = latest stable `leanprover/lean4:v4.x.y` (pick the current stable release from https://github.com/leanprover/lean4/releases). No Mathlib for v1 (download is multi-GB); prompt instructs the LLM to use core Lean 4 only: `Nat`, `Int`, `List`, `Prop` logic, tactics `simp`, `omega`, `decide`, `intro`, `exact`, `constructor`, `cases`, `induction`, `rfl`, `norm_num` is NOT available. Mathlib is a stretch goal (separate task later).
- `lake build` must pass on `ProofLens/Basic.lean` (`theorem two_add_two : 2 + 2 = 4 := by decide`).

## App UI (Material 3, Compose Multiplatform, single design for all targets)
- Bottom nav (Android/Web) / NavigationRail (Desktop, width > 840dp): **Verify**, **Learn**, **History**, **Settings**.
- **Verify**: TextField "State a claim or paste an AI answer", chips with 5 example claims (from DemoExamples), button "Formalize & Verify". While running: StepTimeline showing attempts (Formalizing → Lean checking → Repairing…). Result card: big VerdictBadge (green VERIFIED / red REFUTED / amber UNVERIFIED / grey ERROR), explanation, LeanCodeBlock (monospace, selectable) of the final attempt, expandable diagnostics, expandable earlier attempts.
- **Learn**: list of ~8 Lesson cards (intro to `theorem`, `rfl`, `decide`, `simp`, `omega`, `intro/exact`, `cases`, `induction`). Tapping opens editor with snippet + "Run in Lean" -> /verify -> shows diagnostics / "Lean accepted".
- **History**: in-memory list of HistoryItems (newest first), tap to re-open result.
- **Settings**: server URL text field (default: Android emulator `http://10.0.2.2:8080`, desktop/web `http://localhost:8080`; expect/actual `defaultServerUrl()`), "Demo mode" toggle (uses DemoExamples pre-baked results without network), health indicator (GET /health shows Lean version).
- Demo examples (claim -> expected verdict): "For all natural numbers n, n + 0 = n" (VERIFIED), "2 + 2 = 5" (REFUTED), "The sum of two even numbers is even" (VERIFIED), "Every natural number is greater than 0" (REFUTED, n=0), "For all lists l, l.reverse.reverse = l" (VERIFIED). Pre-baked CheckResponses so the app works with no server.

## Scripts
- `scripts/run-server.sh`: `LEAN_PROJECT_DIR=$(pwd)/lean ./gradlew :server:run`
- `scripts/run-desktop.sh`: `./gradlew :composeApp:run`
- `scripts/build-apk.sh`: `./gradlew :composeApp:assembleDebug` -> prints APK path
- `scripts/build-web.sh`: `./gradlew :composeApp:wasmJsBrowserDistribution`

## Verification for the build
- `./gradlew :shared:allTests` (jvm tests for VerdictLogic)
- `./gradlew :composeApp:assembleDebug`, `./gradlew :composeApp:packageUberJarForCurrentOS` (or `:composeApp:run` smoke), `./gradlew :composeApp:wasmJsBrowserDistribution`
- `./gradlew :server:installDist` then start server, `curl localhost:8080/health`, `curl -X POST localhost:8080/verify -d '{"lean":"theorem t : 2 + 2 = 4 := by decide"}'` -> ok=true; with `sorry` -> ok=false.
