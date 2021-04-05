package com.avito.instrumentation.internal.report.listener

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StubStatsdSender
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.retrace.ProguardRetracer
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.time.StubTimeProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class UploadingPostProcessorTest {

    private val loggerFactory = StubLoggerFactory
    private val timeProvider = StubTimeProvider()
    private val statsdSender = StubStatsdSender()

    @Test
    fun test() {
        val testCase = TestCase(className = "com.avito.Test", methodName = "test", deviceName = "29")

        val postProcessor = createUploadingPostProcessor(
            testSuite = mapOf(
                testCase to TestStaticDataPackage.createStubInstance()
            )
        )

        val testResult = postProcessor.process(
            result = TestLifecycleListener.TestResult.Incomplete(
                infraError = TestCaseRun.Result.Failed.InfrastructureError.Timeout(
                    IllegalStateException("timeout")
                )
            ),
            test = testCase,
            executionNumber = 1,
            logcatBuffers = emptyMap()
        )

        assertThat(testResult.name.name).isEqualTo("com.avito.Test.test")
    }

    private fun createUploadingPostProcessor(
        testSuite: Map<TestCase, TestStaticData> = emptyMap()
    ): UploadingPostProcessor {
        return UploadingPostProcessor(
            loggerFactory = loggerFactory,
            testSuite = testSuite,
            metricsSender = InstrumentationMetricsSender(statsdSender, SeriesName.create("")),
            timeProvider = timeProvider,
            retracer = ProguardRetracer.Stub,
            testArtifactsUploader = StubTestArtifactsUploader()
        )
    }
}
