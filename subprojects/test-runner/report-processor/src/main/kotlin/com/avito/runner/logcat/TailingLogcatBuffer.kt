package com.avito.runner.logcat

import com.avito.android.Problem
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

internal class TailingLogcatBuffer(
    logcatFile: File,
    readFileFromEnd: Boolean = true
) : LogcatBuffer {

    private val buffer = mutableListOf<String>()

    private val tailerListener = object : TailerListenerAdapter() {

        override fun handle(line: String?) {
            if (line != null) {
                buffer.add(line)
            }
        }
    }

    private val frequencyMs = 100L

    private val tailer = Tailer.create(
        logcatFile,
        tailerListener,
        frequencyMs,
        readFileFromEnd
    )

    override fun stop() {
        tailer.stop()
    }

    override fun getLogs(): LogcatResult {
        return if (buffer.isNotEmpty()) {
            try {
                LogcatResult.Success(
                    output = buffer.joinToString(separator = "\n")
                )
            } catch (e: ConcurrentModificationException) {
                LogcatResult.Unavailable(
                    reason = Problem(
                        shortDescription = "No logs fetched during test execution",
                        context = "TailingLogcatBuffer: getting logs for test report. buffer isNotEmpty",
                        because = "It's a bug that need to be fixed. At least some log lines are printed during any test execution" +
                            e.message + "\n" + e.cause
                    )
                )
            }
        } else {
            LogcatResult.Unavailable(
                reason = Problem(
                    shortDescription = "No logs fetched during test execution",
                    context = "TailingLogcatBuffer: getting logs for test report",
                    because = "It's unexpected, probably a bug: " +
                        "at least some log lines are printed during any test execution"
                )
            )
        }
    }
}
