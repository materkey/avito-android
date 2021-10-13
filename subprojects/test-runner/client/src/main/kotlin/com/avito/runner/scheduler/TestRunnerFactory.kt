package com.avito.runner.scheduler

import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.runner.TestRunner
import java.io.File

internal interface TestRunnerFactory {
    fun createTestRunner(
        tests: List<TestStaticData>,
        outputDir: File
    ): TestRunner
}
