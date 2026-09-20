# ProofLens — AI proposes. Lean decides.

## The problem

AI can explain a mathematical claim convincingly while still producing an
invalid proof. Students, developers, and reviewers need a fast way to
distinguish a plausible answer from a proof accepted by a trusted checker.

## The insight

> **AI proposes, Lean decides**

ProofLens uses AI for the creative step—turning natural language into Lean 4—
but gives the final verdict only to the Lean kernel.

## Architecture

```text
                 KMP shared
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
   Android      Desktop         Web
                    │
                    ▼
              Ktor server
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
       OpenAI              Lean 4 kernel
    (formalization)       (authoritative check)
```

## Three-minute demo

1. Open Verify and explain that the app separates an AI suggestion from the
   kernel verdict.
2. Run **For all natural numbers n, n + 0 = n**: Lean accepts the proof and
   the app shows `VERIFIED`.
3. Run **2 + 2 = 5**: the formalizer proves the negation, Lean accepts it, and
   the app shows `REFUTED`.
4. Run **The sum of two even numbers is even** to demonstrate a natural,
   quantified claim verified by Lean.
5. Try **Every natural number is greater than 0** and discuss the
   counterexample at zero.
6. Finish with **For all lists l, l.reverse.reverse = l**, then open Learn to
   show that users can inspect and run Lean code themselves.

## Mapping to judging criteria

- **Technical implementation:** Kotlin Multiplatform shares models and
  verdict logic across Android, Desktop, and Web; Ktor connects OpenAI to a
  real Lean 4 process.
- **Trust and correctness:** every final result is grounded in Lean kernel
  acceptance, not an unchecked AI response.
- **User experience:** Verify, Learn, History, and Settings make the workflow
  approachable while exposing proof code, diagnostics, attempts, and timing.
- **Innovation:** the product makes the boundary between probabilistic
  generation and deterministic verification visible.
- **Demo impact:** true and false claims produce different, explainable
  outcomes in seconds, with offline bundled examples for reliability.

## What's shared

The `shared/` module contains serializable API models, demo content, lessons,
HTTP abstractions, and verdict logic. `composeApp/commonMain` contains the
responsive screens, state machine, and reusable UI. Platform source sets only
provide launchers, HTTP engines, and target-specific configuration.

Using newline counts from Kotlin files under `shared/src` and
`composeApp/src/commonMain`, there are **1,001 shared/common lines**. The
platform/server source sets contain **320 lines**, so the measured split is
**75.8% shared/common versus 24.2% platform-specific/server**.

## Roadmap

1. **Audit AI reasoning mode:** paste an LLM answer, extract
   premises → conclusion, formalize the argument in Lean, and show
   `VERIFIED`/`REFUTED` with a counterexample where appropriate.
2. **Student Community tab:** post AI answers and claims; peers answer with
   Lean proofs that are auto-labelled by Lean, with likes for useful work.
3. **iOS target:** bring the same shared workflow to iPhone and iPad.
