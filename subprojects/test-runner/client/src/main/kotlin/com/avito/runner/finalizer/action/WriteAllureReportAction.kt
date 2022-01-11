package com.avito.runner.finalizer.action

import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.runner.finalizer.verdict.Verdict
import io.qameta.allure.kotlin.model.Label
import io.qameta.allure.kotlin.model.Stage
import io.qameta.allure.kotlin.model.Status
import io.qameta.allure.kotlin.model.StatusDetails
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.qameta.allure.kotlin.model.TestResult as AllureTestResult
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
internal class WriteAllureReportAction(
    private val allureDir: File
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        val existingAllureResultFileItems = getAllureResultFileItems(allureDir)

        val environmentXml = buildString(capacity = 100) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            appendLine("<environment>")

            verdict.testResults.forEach { test ->
                val noAllureResult = existingAllureResultFileItems.find { it.name == test.name.methodName } == null
                if (noAllureResult) {
                    addAllureResult(test, allureDir)
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
                        labels = getLabels(test),
                    ).apply {
                        name = test.name.methodName
                        stage = Stage.FINISHED
                        status = if (incident.type == Incident.Type.INFRASTRUCTURE_ERROR) Status.BROKEN else Status.FAILED
                        statusDetails = StatusDetails(
                            message = incident.chain.firstOrNull()?.message ?: "Unknown",
                            trace = incident.trace.joinToString(separator = "\n")
                        )
                        historyId = md5(test.name.className + test.name.methodName)
                    }
                } else {
                    AllureTestResult(
                        uuid = UUID.randomUUID().toString(),
                        fullName = test.name.toString(),
                        labels = getLabels(test),
                    ).apply {
                        name = test.name.methodName
                        stage = Stage.FINISHED
                        status = Status.PASSED
                        historyId = md5(test.name.className + test.name.methodName)
                    }
                }
            }
            is AndroidTest.Lost -> {
                val incident = test.incident
                AllureTestResult(
                    uuid = UUID.randomUUID().toString(),
                    fullName = test.name.toString(),
                    labels = getLabels(test),
                ).apply {
                    name = test.name.methodName
                    stage = Stage.FINISHED
                    status = Status.BROKEN
                    statusDetails = StatusDetails(
                        message = incident?.chain?.firstOrNull()?.message ?: "Unknown",
                        trace = incident?.trace?.joinToString(separator = "\n")
                    )
                    historyId = md5(test.name.className + test.name.methodName)
                }
            }
            is AndroidTest.Skipped -> {
                if (test.skipReason.contains("SkipDevice")) return
                AllureTestResult(
                    uuid = UUID.randomUUID().toString(),
                    fullName = test.name.toString(),
                    labels = getLabels(test),
                ).apply {
                    name = test.name.methodName
                    stage = Stage.FINISHED
                    status = Status.SKIPPED
                    statusDetails = StatusDetails(
                        message = test.ignoreText + "\n" + test.skipReason,
                    )
                    historyId = md5(test.name.className + test.name.methodName)
                }
            }
        }

        createAllureResultFile(
            allureDir,
            Json.encodeToString(allureResultItem),
            allureResultItem.uuid + ALLURE_RESULT_FILE_POSTFIX
        )
    }

    private fun getLabels(test: AndroidTest): MutableList<Label> {
        return mutableListOf(
            Label(
                name = "package",
                value = test.name.packageName
            ),
            Label(
                name = "testClass",
                value = test.name.className
            ),
            Label(
                name = "testMethod",
                value = test.name.methodName
            ),
            Label(
                name = "suite",
                value = test.name.className
            ),
            Label(
                name = "host",
                value = test.device.name
            ),
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

    private fun md5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
    }

    private companion object {
        const val ALLURE_RESULT_FILE_POSTFIX = "-result.json"
    }
}
