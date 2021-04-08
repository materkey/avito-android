package com.avito.runner.scheduler.listener

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device

interface TestLifecycleListener {

    fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    )

    fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    )
}
