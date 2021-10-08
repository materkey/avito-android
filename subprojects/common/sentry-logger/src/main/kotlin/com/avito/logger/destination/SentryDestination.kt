package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

class SentryDestination : LoggingDestination {

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
    }
}
