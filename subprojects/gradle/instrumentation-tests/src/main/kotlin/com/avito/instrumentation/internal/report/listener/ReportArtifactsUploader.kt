package com.avito.instrumentation.internal.report.listener

import com.avito.report.ReportFileProvider
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.Step
import com.avito.report.model.Video

internal class ReportArtifactsUploader(
    private val testArtifactsUploader: TestArtifactsUploader,
    private val reportFileProvider: ReportFileProvider
) {

    suspend fun processVideo(video: Video?): Video? {
        return if (video != null) {
            val filePath = reportFileProvider.fromUploadPlaceholder(video.link)
            if (filePath != null) {
                val videoUrl = testArtifactsUploader.uploadFile(reportFileProvider.getFile(filePath))
                video.copy(link = videoUrl)
            } else {
                video
            }
        } else {
            null
        }
    }

    suspend fun processStepList(
        stepList: List<Step>
    ): List<Step> {
        return stepList.map { step ->
            step.copy(
                entryList = processEntryList(step.entryList)
            )
        }
    }

    @Suppress("IfThenToElvis")
    suspend fun processIncident(
        incident: Incident?
    ): Incident? {
        return if (incident != null) {
            incident.copy(entryList = processEntryList(incident.entryList))
        } else {
            incident
        }
    }

    private suspend fun processEntryList(
        entryList: List<Entry>
    ): List<Entry> {
        return entryList.map { entry -> processEntry(entry) }
    }

    private suspend fun processEntry(entry: Entry): Entry {
        return when (entry) {
            is Entry.File -> {
                val filePath = reportFileProvider.fromUploadPlaceholder(entry.fileAddress)
                if (filePath != null) {
                    val fileUrl = testArtifactsUploader.uploadFile(reportFileProvider.getFile(filePath))
                    entry.copy(fileAddress = fileUrl)
                } else {
                    entry
                }
            }
            else -> entry
        }
    }
}
