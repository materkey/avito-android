package com.avito.runner.listener

import com.avito.runner.service.worker.device.Device
import io.qameta.allure.kotlin.model.Label
import io.qameta.allure.kotlin.model.Status
import io.qameta.allure.kotlin.model.TestResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal object AllureTransformer {

    fun transform(allureDir: File, device: Device) {
        allureDir.listFiles()?.filter { it.name.endsWith(ALLURE_RESULT_FILE_POSTFIX) }
            ?.map { resultFile ->
                val allureResult: TestResult = Json.decodeFromString(resultFile.readText())
                allureResult.apply {
                    enrichExistingResultsAndWrite(resultFile, device)
                }
            } ?: emptyList()
    }

    private fun TestResult.enrichExistingResultsAndWrite(resultFile: File, device: Device) {

        labels.removeIf { it.name == "host"}
        labels.add(Label("host", "${device.model}:${device.coordinate.serial.value}"))

        // for allure reports with nonsense "broken" status it can be useful to replace it with "failed"
        if (status == Status.BROKEN) {
            status = Status.FAILED
        }
        resultFile.writeText(Json.encodeToString(this))
    }

    private const val ALLURE_RESULT_FILE_POSTFIX = "-result.json"
}
