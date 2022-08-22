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
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit

internal class WriteAllureReportAction(
    private val allureDir: File
) : FinalizeAction {
    private val skippedTests = File(allureDir.absolutePath.plus("/skipped-tests")).apply { mkdirs() }
    private val unskippedTests = File(allureDir.absolutePath.plus("/unskipped-tests")).apply { mkdirs() }

    override fun action(verdict: Verdict) {
        editExistingResultFiles(unskippedTests)
        val existingAllureResultFileItems = getAllureResultFileItems(unskippedTests)

        val environmentXml = buildString(capacity = 100) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            appendLine("<environment>")

            verdict.testResults.forEach { test ->
                val noAllureResult = existingAllureResultFileItems.find { it.name == test.name.methodName } == null
                if (noAllureResult) {
                    addAllureResult(test)
                }
            }

            appendLine("</environment>")
        }


        File(allureDir, "environment.xml").writeText(environmentXml)
    }

    private fun addAllureResult(test: AndroidTest) {
        val allureResultItem: AllureTestResult = when (test) {
            is AndroidTest.Completed -> {
                val incident = test.incident
                val startTimeMillis = TimeUnit.SECONDS.toMillis(test.startTime)
                val endTimeMillis = TimeUnit.SECONDS.toMillis(test.endTime).let { endTimeMillis ->
                    if (test.startTime == test.endTime) endTimeMillis + 500 else endTimeMillis // fix empty allure report timeline
                }
                if (incident != null) {
                    AllureTestResult(
                        uuid = UUID.randomUUID().toString(),
                        fullName = test.name.toString(),
                        labels = getLabels(test),
                    ).apply {
                        name = test.name.methodName
                        start = startTimeMillis
                        stop = endTimeMillis
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
                        start = startTimeMillis
                        stop = endTimeMillis
                        stage = Stage.FINISHED
                        status = Status.PASSED
                        historyId = md5(test.name.className + test.name.methodName)
                    }
                }
            }
            is AndroidTest.Lost -> {
                val startTimeMillis = TimeUnit.SECONDS.toMillis(test.startTime)

                val incident = test.incident
                AllureTestResult(
                    uuid = UUID.randomUUID().toString(),
                    fullName = test.name.toString(),
                    labels = getLabels(test),
                ).apply {
                    name = test.name.methodName
                    start = startTimeMillis
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
                if (test.skipReason.contains("RunDevice")) {
                    return
                } else {
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
        }

        val rawResultsTarget = when (test) {
            is AndroidTest.Skipped -> skippedTests
            else -> unskippedTests
        }

        createAllureResultFile(
            rawResultsTarget,
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

    private fun createAllureResultFile(allureDir: File, text: String, name: String): File =
        File(allureDir, name).apply {
            writeText(text)
        }

    // for allure reports with nonsense "broken" status it can be useful to replace it with "failed"
    private fun AllureTestResult.replaceBrokenWithFailedAndWrite(resultFile: File) {
        if (status == Status.BROKEN) {
            status = Status.FAILED
            resultFile.writeText(Json.encodeToString(this))
        }
    }

    private fun getAllureResultFileItems(allureDir: File): List<AllureTestResult> =
        allureDir.listFiles()?.filter { it.name.endsWith(ALLURE_RESULT_FILE_POSTFIX) }
            ?.map { resultFile ->
                val allureResult: AllureTestResult = Json.decodeFromString(resultFile.readText())
                allureResult.apply {
                    replaceBrokenWithFailedAndWrite(resultFile)
                }
            } ?: emptyList()

    private fun editExistingResultFiles(allureDir: File) =
        allureDir.listFiles()?.filter { it.name.endsWith(ALLURE_RESULT_FILE_POSTFIX) }
            ?.forEach { resultFile ->
                val allureResult: AllureTestResult = Json.decodeFromString(resultFile.readText())
                allureResult.apply {
                    statusDetails?.message = statusDetails?.message?.take(240) ?: ""
                }
                resultFile.writeText(Json.encodeToString(allureResult))
            }

    private fun md5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
    }

    private companion object {
        const val ALLURE_RESULT_FILE_POSTFIX = "-result.json"
    }
}
