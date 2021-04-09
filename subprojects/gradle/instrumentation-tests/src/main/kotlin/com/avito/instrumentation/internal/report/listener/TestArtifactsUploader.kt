package com.avito.instrumentation.internal.report.listener

import java.io.File

internal interface TestArtifactsUploader {

    suspend fun uploadLogcat(logcat: String): String

    suspend fun uploadFile(file: File): String
}
