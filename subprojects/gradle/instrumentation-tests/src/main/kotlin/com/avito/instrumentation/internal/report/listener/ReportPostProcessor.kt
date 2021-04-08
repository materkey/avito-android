package com.avito.instrumentation.internal.report.listener

import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.AndroidTest
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.retrace.ProguardRetracer
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileReader

internal interface ReportPostProcessor {

    fun process(
        result: TestResult,
        test: TestCase,
        executionNumber: Int,
        logcatBuffers: Map<Pair<TestCase, Int>, LogcatBuffer>
    ): AndroidTest
}

internal class UploadingPostProcessor(
    loggerFactory: LoggerFactory,
    private val testSuite: Map<TestCase, TestStaticData>,
    private val metricsSender: InstrumentationMetricsSender,
    private val timeProvider: TimeProvider,
    private val retracer: ProguardRetracer,
    private val testArtifactsUploader: TestArtifactsUploader,
) : ReportPostProcessor {

    private val logger = loggerFactory.create<UploadingPostProcessor>()

    // todo reuse/pass from common report module
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    override fun process(
        result: TestResult,
        test: TestCase,
        executionNumber: Int,
        logcatBuffers: Map<Pair<TestCase, Int>, LogcatBuffer>
    ): AndroidTest {
        val testFromSuite = requireNotNull(testSuite[test]) { "Can't find test in suite: ${test.testName}" }
        val key = test to executionNumber

        return when (result) {
            is TestResult.Complete -> {
                result.artifacts.fold(
                    { reportDir ->
                        val reportJson = File(reportDir, REPORT_JSON_ARTIFACT)

                        try {
                            val testRuntimeData: TestRuntimeData = gson.fromJson<TestRuntimeDataPackage>(
                                FileReader(reportJson)
                            )

                            // send only for failed tests
                            val (stdout: String, stderr: String) = if (testRuntimeData.incident != null) {
                                logcatBuffers.getLogcat(key)
                            } else {
                                "" to ""
                            }

                            AndroidTest.Completed.create(
                                testStaticData = testFromSuite,
                                testRuntimeData = testRuntimeData,
                                stdout = stdout,
                                stderr = stderr
                            )
                        } catch (throwable: Throwable) {
                            val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                            val errorMessage = "Can't parse testRuntimeData: ${test.testName}; ${reportJson.readText()}"

                            logger.warn(errorMessage, throwable)

                            metricsSender.sendReportFileParseErrors()

                            createLostTest(
                                testFromSuite = testFromSuite,
                                errorMessage = errorMessage,
                                stdout = stdout,
                                stderr = stderr,
                                throwable = throwable
                            )
                        }
                    },
                    { throwable ->
                        val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                        val errorMessage = "Can't get report from file: $test"

                        logger.warn(errorMessage, throwable)

                        metricsSender.sendReportFileNotAvailable()

                        createLostTest(
                            testFromSuite = testFromSuite,
                            errorMessage = errorMessage,
                            stdout = stdout,
                            stderr = stderr,
                            throwable = throwable
                        )
                    }
                )
            }
            is TestResult.Incomplete -> {
                val (stdout: String, stderr: String) = logcatBuffers.getLogcat(key)

                with(result.infraError) {
                    logger.warn("${error.message} while executing ${test.testName}", this.error)

                    when (this) {
                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnParsing ->
                            metricsSender.sendFailedOnParsingInstrumentation()

                        is TestCaseRun.Result.Failed.InfrastructureError.FailedOnStart ->
                            metricsSender.sendFailedOnStartInstrumentation()

                        is TestCaseRun.Result.Failed.InfrastructureError.Timeout ->
                            metricsSender.sendTimeOut()

                        is TestCaseRun.Result.Failed.InfrastructureError.Unexpected ->
                            metricsSender.sendUnexpectedInfraError()
                    }

                    createLostTest(
                        testFromSuite = testFromSuite,
                        errorMessage = error.message ?: "Empty message",
                        stdout = stdout,
                        stderr = stderr,
                        throwable = error
                    )
                }
            }
        }
    }

    private fun createLostTest(
        testFromSuite: TestStaticData,
        errorMessage: String,
        stdout: String,
        stderr: String,
        throwable: Throwable
    ): AndroidTest {
        return AndroidTest.Lost.fromTestMetadata(
            testFromSuite,
            startTime = 0,
            lastSignalTime = 0,
            stdout = stdout,
            stderr = stderr,
            incident = Incident(
                type = Incident.Type.INFRASTRUCTURE_ERROR,
                timestamp = timeProvider.nowInSeconds(),
                trace = throwable.stackTraceToList(),
                chain = listOf(
                    IncidentElement(
                        message = errorMessage
                    )
                ),
                entryList = emptyList()
            )
        )
    }

    private fun Map<Pair<TestCase, Int>, LogcatBuffer>.getLogcat(test: Pair<TestCase, Int>): Pair<String, String> {
        val logcatBuffer = get(test)
        return if (logcatBuffer != null) {
            logcatBuffer
                .getLogs()
                .let { (stdout, stderr) ->
                    runBlocking {
                        val stdoutFuture = uploadLogcat(stdout)
                        val stderrFuture = uploadLogcat(stderr)
                        stdoutFuture to stderrFuture
                    }
                }
        } else {
            logger.critical("Can't find logBuffer", IllegalStateException("No logBuffer by key:$test"))
            return "" to ""
        }
    }

    private suspend fun uploadLogcat(logcat: List<String>): String {
        return testArtifactsUploader.uploadLogcat(retracer.retrace(logcat.joinToString(separator = "\n")))
    }

    companion object {
        // todo should be passed with instrumentation params, see [ExternalStorageTransport]
        private const val REPORT_JSON_ARTIFACT = "report.json"
    }
}
