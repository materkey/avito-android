package com.avito.instrumentation.internal.report.listener

internal interface TestArtifactsUploader {

    suspend fun uploadLogcat(logcat: String): String
}
