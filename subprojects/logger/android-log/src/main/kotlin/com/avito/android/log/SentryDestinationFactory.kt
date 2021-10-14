package com.avito.android.log

import com.avito.logger.LoggingDestination
import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(metadata: AndroidTestMetadata): LoggingDestination {
        return SentryDestination(metadata.toMap())
    }
}
