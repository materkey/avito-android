package com.avito.logger.destination

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class SentryDestinationTest {

    @Test
    fun `destination is serializable`() {
        val destination = SentryDestination(
            metadata = mapOf("tag" to "LoggingDestinationTest")
        )

        assertSerializable(destination)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
