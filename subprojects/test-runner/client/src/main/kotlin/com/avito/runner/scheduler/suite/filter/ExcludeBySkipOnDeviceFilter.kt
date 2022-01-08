package com.avito.runner.scheduler.suite.filter

import com.avito.android.test.annotations.SkipOnDevice

// it makes [name] instance field. It needs for gson
internal class ExcludeBySkipOnDeviceFilter : TestsFilter {

    override val name = "ExcludeBySkipOnDeviceFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        val testAnnotations = test.annotations
            .find {
                it.name == SkipOnDevice::class.java.name
            }

        @Suppress("UNCHECKED_CAST")
        val skippedDevices = testAnnotations?.values?.get("deviceName") as? Collection<String>

        return if (skippedDevices?.contains(test.deviceName.name) == true) {
            TestsFilter.Result.Excluded.HasSkipDeviceAnnotation(
                name,
                test.deviceName.name
            )
        } else {
            TestsFilter.Result.Included
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other.hashCode() == hashCode() && other.javaClass.isInstance(this)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
