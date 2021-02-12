plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

extra["artifact-id"] = "runner-service"

dependencies {
    api(project(":subprojects:common:coroutines-extension"))
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(libs.funktionaleTry)
    implementation(libs.ddmlib)
    implementation(libs.rxJava)

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
