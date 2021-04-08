package com.avito.runner.scheduler.listener

import com.avito.runner.service.model.TestCaseRun

fun TestResult.Companion.timeout(exceptionMessage: String = "timeout") =
    TestResult.Incomplete(TestCaseRun.Result.Failed.InfrastructureError.Timeout(RuntimeException(exceptionMessage)))
