package dev.prooflens.shared.lessons

import dev.prooflens.shared.model.Lesson

val lessons = listOf(
    Lesson("theorem", "Name a theorem", "A theorem is a named proposition with a proof.", "theorem hello : True := by\n  trivial", "theorem"),
    Lesson("rfl", "Reflexivity", "Use rfl when both sides reduce to the same expression.", "theorem identity (n : Nat) : n = n := by\n  rfl", "rfl"),
    Lesson("decide", "Decidable facts", "decide proves closed propositions with a Decidable instance.", "theorem twoPlusTwo : 2 + 2 = 4 := by\n  decide", "decide"),
    Lesson("simp", "Simplify expressions", "simp applies trusted rewrite lemmas to normalize goals.", "theorem addZero (n : Nat) : n + 0 = n := by\n  simp", "simp"),
    Lesson("omega", "Arithmetic reasoning", "omega handles many linear arithmetic goals over Nat and Int.", "theorem successorPositive (n : Nat) : n < n + 1 := by\n  omega", "omega"),
    Lesson("intro-exact", "Introduce and apply", "intro brings assumptions into context; exact closes a matching goal.", "theorem implication (p : Prop) : p → p := by\n  intro hp\n  exact hp", "intro / exact"),
    Lesson("cases", "Split possibilities", "cases explores the constructors of an inductive value.", "theorem boolCases (b : Bool) : b = true ∨ b = false := by\n  cases b <;> simp", "cases"),
    Lesson("induction", "Prove by induction", "induction reduces a recursive claim to a base case and step.", "theorem listAppendNil (xs : List Nat) : xs ++ [] = xs := by\n  induction xs with\n  | nil => rfl\n  | cons x xs ih => simp [ih]", "induction"),
)
