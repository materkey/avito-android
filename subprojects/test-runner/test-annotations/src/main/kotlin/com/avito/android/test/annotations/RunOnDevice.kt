package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class RunOnDevice(
    vararg val deviceName: String,
    val message: String = ""
)
