package dev.prooflens.shared.logic

import dev.prooflens.shared.model.Diagnostic

private val diagnosticPattern = Regex("""^(.*?):(\d+):(\d+):\s*(error|warning|info):\s*(.*)$""")

fun parseLeanOutput(raw: String): List<Diagnostic> {
    val diagnostics = mutableListOf<Diagnostic>()
    raw.lineSequence().forEach { line ->
        val match = diagnosticPattern.matchEntire(line.trim())
        if (match != null) {
            diagnostics += Diagnostic(
                line = match.groupValues[2].toInt(),
                col = match.groupValues[3].toInt(),
                severity = match.groupValues[4],
                message = match.groupValues[5],
            )
        } else if (line.isNotBlank() && diagnostics.isNotEmpty()) {
            val last = diagnostics.removeLast()
            diagnostics += last.copy(message = "${last.message}\n${line.trim()}")
        }
    }
    return diagnostics
}

fun isOk(diagnostics: List<Diagnostic>, rawOutput: String = ""): Boolean =
    diagnostics.none { it.severity.equals("error", ignoreCase = true) } &&
        !rawOutput.contains("declaration uses 'sorry'", ignoreCase = true) &&
        !rawOutput.contains("sorry", ignoreCase = true)
