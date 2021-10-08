package com.avito.logger

import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(): LoggingDestination = SentryDestination()

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
