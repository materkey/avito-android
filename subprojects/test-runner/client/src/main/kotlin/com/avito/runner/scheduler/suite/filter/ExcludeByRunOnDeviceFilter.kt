package com.avito.runner.scheduler.suite.filter

import com.avito.android.test.annotations.RunOnDevice

// it makes [name] instance field. It needs for gson
internal class ExcludeByRunOnDeviceFilter : TestsFilter {

    override val name = "ExcludeByRunOnDeviceFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        val testAnnotations = test.annotations
            .find {
                it.name == RunOnDevice::class.java.name
            }

        @Suppress("UNCHECKED_CAST")
        val allowedDevices = testAnnotations?.values?.get("deviceName") as? Collection<String>

        return if (allowedDevices?.contains(test.deviceName.name) == true) {
            TestsFilter.Result.Included

        } else {
            TestsFilter.Result.Excluded.HasRunDeviceAnnotation(
                name,
                test.deviceName.name
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other.hashCode() == hashCode() && other.javaClass.isInstance(this)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
