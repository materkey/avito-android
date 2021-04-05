package com.avito.instrumentation.internal.report.listener

internal class StubTestArtifactsUploader : TestArtifactsUploader {

    override suspend fun uploadLogcat(logcat: String): String = "stub"
}
