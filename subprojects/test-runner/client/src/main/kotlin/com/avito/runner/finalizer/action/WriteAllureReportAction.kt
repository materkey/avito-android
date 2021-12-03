package com.avito.runner.finalizer.action

import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.runner.finalizer.verdict.Verdict
import io.qameta.allure.kotlin.model.Stage
import io.qameta.allure.kotlin.model.Status
import io.qameta.allure.kotlin.model.StatusDetails
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.qameta.allure.kotlin.model.TestResult as AllureTestResult
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.util.*
internal class WriteAllureReportAction(
    private val allureDir: File
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        val existingAllureResultFileItems = getAllureResultFileItems(allureDir)

        val environmentXml = buildString(capacity = 100) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            appendLine("<environment>")

            var index = 0
            verdict.testResults.forEach { test ->
                when (test) {
                    is AndroidTest.Skipped -> {
                        appendLine("<parameter>")
                        append("<key>")
                        append("skipped_${index++}")
                        append("</key>")
                        append("<value>")
                        appendEscapedLine("${test.name} - ${test.skipReason} ${test.ignoreText ?: ""}")
                        append("</value>")
                        appendLine("</parameter>")
                    }
                    else -> {
                        val noAllureResult = existingAllureResultFileItems.find { it.name == test.name.methodName } == null
                        if (noAllureResult) {
                            addAllureResult(test, allureDir)
                        }
                    }
                }
            }

            appendLine("</environment>")
        }


        File(allureDir, "environment.xml").writeText(environmentXml)
    }

    private fun addAllureResult(test: AndroidTest, allureDir: File) {
        val allureResultItem: AllureTestResult = when (test) {
            is AndroidTest.Completed -> {
                val incident = test.incident
                if (incident != null) {
                    AllureTestResult(
                        uuid = UUID.randomUUID().toString(),
                        fullName = test.name.toString(),
                    ).apply {
                        name = test.name.methodName
                        stage = Stage.FINISHED
                        status = if (incident.type == Incident.Type.INFRASTRUCTURE_ERROR) Status.BROKEN else Status.FAILED
                        statusDetails = StatusDetails(
                            message = incident.chain.firstOrNull()?.message ?: "Unknown",
                            trace = incident.trace.joinToString(separator = "\n")
                        )
                    }
                } else {
                    AllureTestResult(
                        uuid = UUID.randomUUID().toString(),
                        fullName = test.name.toString(),
                    ).apply {
                        name = test.name.methodName
                        stage = Stage.FINISHED
                        status = Status.PASSED
                    }
                }
            }
            is AndroidTest.Lost -> {
                val incident = test.incident
                AllureTestResult(
                    uuid = UUID.randomUUID().toString(),
                    fullName = test.name.toString(),
                ).apply {
                    name = test.name.methodName
                    stage = Stage.FINISHED
                    status = Status.BROKEN
                    statusDetails = StatusDetails(
                        message = incident?.chain?.firstOrNull()?.message ?: "Unknown",
                        trace = incident?.trace?.joinToString(separator = "\n")
                    )
                }
            }
            is AndroidTest.Skipped -> { error("skipped can never happen here") }
        }

        createAllureResultFile(
            allureDir,
            Json.encodeToString(allureResultItem),
            allureResultItem.uuid + ALLURE_RESULT_FILE_POSTFIX
        )
    }

    // for allure reports with nonsense "broken" status it can be useful to replace it with "failed"
    private fun AllureTestResult.replaceBrokenWithFailedAndWrite(resultFile: File) {
        if (status == Status.BROKEN) {
            status = Status.FAILED
            resultFile.writeText(Json.encodeToString(this))
        }
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendLine(StringEscapeUtils.escapeXml10(line))
    }

    private fun createAllureResultFile(allureDir: File, text: String, name: String): File =
        File(allureDir, name).apply {
            writeText(text)
        }

    private fun getAllureResultFileItems(allureDir: File): List<AllureTestResult> =
        allureDir.listFiles()?.filter { it.name.endsWith(ALLURE_RESULT_FILE_POSTFIX) }
            ?.map { resultFile ->
                val allureResult: AllureTestResult = Json.decodeFromString(resultFile.readText())
                allureResult.apply {
                    replaceBrokenWithFailedAndWrite(resultFile)
                }
            } ?: emptyList()

    private companion object {
        const val ALLURE_RESULT_FILE_POSTFIX = "-result.json"
    }
}
