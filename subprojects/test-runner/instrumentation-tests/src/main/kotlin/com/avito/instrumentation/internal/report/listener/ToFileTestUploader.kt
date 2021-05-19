package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File

internal class ToFileTestUploader(
    private val outputDir: File
) : TestArtifactsUploader {

    private val success: Result<HttpUrl>
        get() = Result.Success(
            HttpUrl.Builder()
                .host("http://stub")
                .build()
        )

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        val filename = "instrumentation-artifact-${System.nanoTime()}.${type.toExtension()}"
        val outputFile = File(outputDir, filename)
        return outputFile.appendText(content).let { success }
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        val filename = "instrumentation-artifact-${System.nanoTime()}.${type.toExtension()}"
        val outputFile = File(outputDir, filename)
        return file.copyTo(outputFile, overwrite = true).let { success }
    }

    private fun Entry.File.Type.toExtension(): String {
        return when (this) {
            Entry.File.Type.html -> "html"
            Entry.File.Type.img_png -> "png"
            Entry.File.Type.video -> "mp4"
            Entry.File.Type.plain_text -> "txt"
        }
    }
}
