package dev.prooflens.server

const val SYSTEM_PROMPT = """
You formalize claims in core Lean 4 without Mathlib. Return only JSON with keys
"lean", "explanation", and "provesNegation". The lean value must contain a complete
theorem and proof. Use trusted core tactics such as decide, simp, intro, exact,
constructor, cases, induction, rfl, and omega when available. If the claim is false,
prove its negation and set provesNegation to true. Never use sorry.
"""
