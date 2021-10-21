package com.avito.runner.finalizer.action

import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.duration
import com.avito.runner.finalizer.verdict.TestStatisticsCounterFactory
import com.avito.runner.finalizer.verdict.Verdict
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class WriteJUnitReportAction(
    private val testSuiteNameProvider: TestSuiteNameProvider,
    private val reportLinksGenerator: ReportLinksGenerator,
    private val destination: File
) : FinalizeAction {

    private val estimatedTestRecordSize = 150

    override fun action(verdict: Verdict) {

        val testStatisticsCounter = TestStatisticsCounterFactory.create(verdict)

        val lostTests: Collection<AndroidTest.Lost> = verdict.testResults
            .filterIsInstance<AndroidTest.Lost>()
        val completedTests: Collection<AndroidTest.Completed> = verdict.testResults
            .filterIsInstance<AndroidTest.Completed>()

        val lostTestsWithCompletedTries: Collection<AndroidTest.Lost> = lostTests.filter { test ->
            completedTests.firstOrNull {
                it.name == test.name
            } != null
        }

        val testCountOverall = testStatisticsCounter.overallCount()
        val testCountFailures = testStatisticsCounter.failureCount()
        val testCountErrors =
            (testStatisticsCounter.notReportedCount() - lostTestsWithCompletedTries.size)
                .coerceAtLeast(0)
        val testCountSkipped = testStatisticsCounter.skippedCount()

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${testSuiteNameProvider.getName()}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""errors="$testCountErrors" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="${testStatisticsCounter.overallDurationSec()}" """)
            appendLine(">")

            appendLine("<properties/>")

            verdict.testResults.forEach { test ->
                val isNotLostWithCompletedTries =
                    test !is AndroidTest.Lost || !lostTestsWithCompletedTries.contains(test)
                if (isNotLostWithCompletedTries) {
                    appendTest(test)
                }
            }

            appendLine("</testsuite>")
        }

        destination.writeText(xml)
    }

    private fun StringBuilder.appendTest(test: AndroidTest) {
        append("<testcase ")
        append("""classname="${test.name.className}" """)
        append("""name="${test.name.methodName}" """)
        append("""caseId="${test.testCaseId}" """)

        if (test is TestRuntimeData) {
            append("""time="${test.duration.toFloat()}"""")
        } else {
            append("""time="-1.0"""")
        }

        appendLine(">")

        when (test) {
            is AndroidTest.Skipped -> {
                appendLine("<skipped/>")
                appendLine("<system-out>")
                appendEscapedLine("Тест не запускался: ${test.skipReason}")
                appendLine("</system-out>")
            }
            is AndroidTest.Completed -> {
                val incident = test.incident
                if (incident != null) {
                    appendLine("<failure>")
                    appendEscapedLine(incident.errorMessage)
                    appendLine("</failure>")
                }
            }
            is AndroidTest.Lost -> {
                appendLine("<error>")
                appendLine("LOST (no info in report)")
                val incident = test.incident
                if (incident != null) {
                    appendEscapedLine(incident.errorMessage)
                }
                appendLine("$test")

                appendLine("</error>")
            }
        }

        appendLine("</testcase>")
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendLine(StringEscapeUtils.escapeXml10(line))
    }
}
