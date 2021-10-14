package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

public class SentryDestination(
    private val metadata: Map<String, String>
) : LoggingDestination {

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        throwable?.also {
        }
    }
}
