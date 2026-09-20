package dev.prooflens.server

import dev.prooflens.shared.logic.isOk
import dev.prooflens.shared.logic.parseLeanOutput
import dev.prooflens.shared.model.VerifyResponse
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class LeanRunner(
    private val projectDir: File = File(System.getenv("LEAN_PROJECT_DIR") ?: "../lean").absoluteFile,
) {
    fun verify(source: String): VerifyResponse {
        val scratchDir = projectDir.resolve("Scratch").also { it.mkdirs() }
        val file = scratchDir.resolve("${UUID.randomUUID()}.lean")
        val started = System.currentTimeMillis()
        return try {
            file.writeText(source)
            val process = ProcessBuilder("lake", "env", "lean", file.absolutePath)
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(60, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return VerifyResponse(false, emptyList(), "Lean verification timed out", System.currentTimeMillis() - started)
            }
            val diagnostics = parseLeanOutput(output)
            VerifyResponse(isOk(diagnostics, output) && process.exitValue() == 0, diagnostics, output, System.currentTimeMillis() - started)
        } catch (_: java.io.IOException) {
            VerifyResponse(false, emptyList(), "Lean toolchain not installed", System.currentTimeMillis() - started)
        } finally {
            file.delete()
        }
    }
}
