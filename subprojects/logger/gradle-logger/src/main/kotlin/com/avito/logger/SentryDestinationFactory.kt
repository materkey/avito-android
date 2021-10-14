package com.avito.logger

import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(
        metadata: LoggerMetadata
    ): LoggingDestination = SentryDestination(
        metadata = metadata.toMap()
    )

    private fun LoggerMetadata.toMap(): Map<String, String> {
        val result = mutableMapOf("tag" to tag)

        if (!pluginName.isNullOrBlank()) {
            result["plugin_name"] = pluginName
        }

        if (!projectPath.isNullOrBlank()) {
            result["project_path"] = projectPath
        }

        if (!taskName.isNullOrBlank()) {
            result["task_name"] = taskName
        }

        return result
    }
}
