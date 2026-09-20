package dev.prooflens.shared.demo

import dev.prooflens.shared.model.CheckAttempt
import dev.prooflens.shared.model.CheckResponse
import dev.prooflens.shared.model.Diagnostic
import dev.prooflens.shared.model.Verdict
import dev.prooflens.shared.model.VerifyResponse

private fun accepted(lean: String) = VerifyResponse(true, emptyList(), "", 42)
private fun rejected(lean: String, message: String) = VerifyResponse(
    false, listOf(Diagnostic(1, 1, "error", message)), "Scratch/demo.lean:1:1: error: $message", 42,
)

private fun result(claim: String, verdict: Verdict, lean: String, explanation: String, verify: VerifyResponse): CheckResponse =
    CheckResponse(
        claim = claim,
        verdict = verdict,
        attempts = listOf(CheckAttempt(1, lean, explanation, verify, verdict == Verdict.REFUTED)),
        summary = when (verdict) {
            Verdict.VERIFIED -> "Lean accepted the proposed proof."
            Verdict.REFUTED -> "Lean accepted a proof of the negation."
            else -> "The proof could not be verified."
        },
    )

val demoExamples: List<Pair<String, CheckResponse>> = listOf(
    "For all natural numbers n, n + 0 = n" to result(
        "For all natural numbers n, n + 0 = n", Verdict.VERIFIED,
        "theorem add_zero (n : Nat) : n + 0 = n := by simp",
        "Addition by zero is an identity for natural numbers.", accepted(""),
    ),
    "2 + 2 = 5" to result(
        "2 + 2 = 5", Verdict.REFUTED,
        "theorem not_two_plus_two : ¬ (2 + 2 = 5) := by decide",
        "The negation is decidable and Lean confirms it.", accepted(""),
    ),
    "The sum of two even numbers is even" to result(
        "The sum of two even numbers is even", Verdict.VERIFIED,
        "theorem even_sum (a b : Nat) (ha : ∃ x, a = x + x) (hb : ∃ y, b = y + y) : ∃ z, a + b = z + z := by\n  cases ha with\n  | intro x hx =>\n    cases hb with\n    | intro y hy =>\n      subst a\n      subst b\n      refine ⟨x + y, ?_⟩\n      simp [Nat.add_left_comm, Nat.add_comm]",
        "Writing both even numbers as doubles gives an even sum.", accepted(""),
    ),
    "Every natural number is greater than 0" to result(
        "Every natural number is greater than 0", Verdict.REFUTED,
        "theorem not_all_positive : ¬ (∀ n : Nat, 0 < n) := by\n  intro h\n  have hzero : 0 < 0 := h 0\n  simpa using hzero",
        "Zero is a counterexample.", accepted(""),
    ),
    "For all lists l, l.reverse.reverse = l" to result(
        "For all lists l, l.reverse.reverse = l", Verdict.VERIFIED,
        "theorem reverse_reverse (l : List Nat) : l.reverse.reverse = l := by\n  simp",
        "List reversal is an involution.", accepted(""),
    ),
)

fun demoForClaim(claim: String): CheckResponse? = demoExamples.firstOrNull { it.first == claim }?.second
