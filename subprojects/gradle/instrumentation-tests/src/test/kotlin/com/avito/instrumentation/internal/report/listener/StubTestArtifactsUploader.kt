package com.avito.instrumentation.internal.report.listener

import java.io.File

internal class StubTestArtifactsUploader : TestArtifactsUploader {

    override suspend fun uploadLogcat(logcat: String): String = "stub"

    override suspend fun uploadFile(file: File): String = "stub"
}
