package dev.prooflens.shared.logic

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class VerdictLogicTest {
    @Test
    fun parsesErrorsWarningsAndContinuation() {
        val diagnostics = parseLeanOutput(
            "Scratch/a.lean:3:7: error: unknown identifier 'x'\n" +
                "  expected a declaration\n" +
                "Scratch/a.lean:4:1: warning: declaration uses 'sorry'\n",
        )
        assertEquals(2, diagnostics.size)
        assertEquals(3, diagnostics[0].line)
        assertEquals("unknown identifier 'x'\nexpected a declaration", diagnostics[0].message)
        assertEquals("warning", diagnostics[1].severity)
    }

    @Test
    fun acceptsCleanOutput() {
        assertTrue(isOk(emptyList(), ""))
    }

    @Test
    fun rejectsErrorsAndSorry() {
        assertFalse(isOk(listOf(dev.prooflens.shared.model.Diagnostic(1, 1, "error", "bad")), ""))
        assertFalse(isOk(emptyList(), "declaration uses 'sorry'"))
    }
}
