package com.avito.android.log

import com.avito.logger.LoggingDestination
import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(): LoggingDestination {
        return SentryDestination()
    }
}
