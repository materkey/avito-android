package com.avito.instrumentation.internal.report.listener

import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase

internal class StubTestReporter : TestReporter() {

    override fun report(result: TestResult, test: TestCase, executionNumber: Int) {
        // no op
    }
}
