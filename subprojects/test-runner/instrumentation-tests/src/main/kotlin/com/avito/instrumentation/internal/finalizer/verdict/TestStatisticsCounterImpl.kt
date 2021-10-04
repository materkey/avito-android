package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.duration

internal class TestStatisticsCounterImpl(private val verdict: Verdict) : TestStatisticsCounter {

    override fun overallDurationSec(): Float =
        verdict.testResults
            .filterIsInstance<TestRuntimeData>()
            .map { it.duration }
            .sum()
            .toFloat()

    override fun overallCount(): Int = verdict.testResults.size

    override fun successCount(): Int = completedTests().filter { it.incident == null }.size

    override fun skippedCount(): Int = verdict.testResults.filterIsInstance<AndroidTest.Skipped>().size

    override fun failureCount(): Int =
        if (verdict is Verdict.Failure) verdict.failedTests.size else 0

    override fun notReportedCount(): Int =
        if (verdict is Verdict.Failure) verdict.lostTests.size else 0

    private fun completedTests(): List<AndroidTest.Completed> {
        return verdict.testResults.filterIsInstance<AndroidTest.Completed>()
    }
}
