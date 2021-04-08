package com.avito.instrumentation.internal.report.listener

import com.avito.android.runner.report.Report
import com.avito.report.model.AndroidTest
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import java.io.File

internal class LogcatTestLifecycleListener(
    private val logcatDir: File,
    private val reportPostProcessor: ReportPostProcessor,
    private val report: Report
) : TestLifecycleListener {

    private val logcatBuffers = mutableMapOf<Pair<TestCase, Int>, LogcatBuffer>()

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
        val logcatFile = File(logcatDir, "${device.coordinate.serial}.txt")

        val key = test to executionNumber
        logcatBuffers[key] = LogcatBuffer.Impl(
            logcatFile = logcatFile
        )
    }

    override fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        val testReport = reportPostProcessor.process(result, test, executionNumber, logcatBuffers)

        when (testReport) {
            is AndroidTest.Completed -> report.sendCompletedTest(testReport)
            is AndroidTest.Lost -> report.sendLostTests(listOf(testReport))
            is AndroidTest.Skipped -> {
                /* do nothing */
            }
        }

        val key = test to executionNumber
        logcatBuffers.remove(key)?.stop()
    }
}
