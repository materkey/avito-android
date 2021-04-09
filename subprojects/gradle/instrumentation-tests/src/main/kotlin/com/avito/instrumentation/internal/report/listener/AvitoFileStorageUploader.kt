package com.avito.instrumentation.internal.report.listener

import com.avito.filestorage.FutureValue
import com.avito.filestorage.HttpRemoteStorage
import com.avito.filestorage.RemoteStorage
import java.io.File

internal class AvitoFileStorageUploader(
    private val remoteStorage: RemoteStorage
) : TestArtifactsUploader {

    override suspend fun uploadLogcat(logcat: String): String {
        return remoteStorage.upload(
            RemoteStorage.Request.ContentRequest.AnyContent(
                content = logcat,
                extension = "log"
            ),
            comment = "logcat"
        ).getUrl()
    }

    override suspend fun uploadFile(file: File): String {
        return "" // not used
    }

    private fun FutureValue<RemoteStorage.Result>.getUrl(): String {
        return when (val result = get()) {
            is RemoteStorage.Result.Success -> remoteStorageFullUrl(result)
            is RemoteStorage.Result.Error -> "Failed to upload logcat: ${result.t.message}"
        }
    }

    private fun remoteStorageFullUrl(result: RemoteStorage.Result.Success): String {
        check(remoteStorage is HttpRemoteStorage)
        return remoteStorage.fullUrl(result)
    }
}
