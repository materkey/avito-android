package com.avito.logger.destination

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class SentryDestinationTest {


    fun `destination is serializable`() {
        val destination = SentryDestination()

        assertSerializable(destination)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
