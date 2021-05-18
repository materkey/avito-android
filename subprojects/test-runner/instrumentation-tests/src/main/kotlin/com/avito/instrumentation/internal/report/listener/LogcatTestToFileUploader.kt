package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.http.toPlainText
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File
import kotlin.random.Random
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody

internal class LogcatTestToFileUploader(
    private val outputDir: File
) : TestArtifactsUploader {

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        val outputFile = File(outputDir, "content-instrumentation-logcat-${Random.nextInt()}.txt")
        return outputFile.appendText(content).let {
            Result.Success(
                HttpUrl.Builder()
                    .scheme("https")
                    .host("stub")
                    .build()
            )
        }
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        val outputFile = File(outputDir, "file-instrumentation-logcat-${Random.nextInt()}.txt")
        return outputFile.appendText(file.asRequestBody("text/plain".toMediaType()).toPlainText() ?: "null").let {
            Result.Success(
                HttpUrl.Builder()
                    .scheme("https")
                    .host("stub")
                    .build()
            )
        }
    }
}
