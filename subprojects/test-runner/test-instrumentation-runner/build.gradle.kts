plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(libs.androidXTestRunner)
    implementation(libs.uiAutomator)
    implementation(project(":common:logger"))
    api("io.qameta.allure:allure-kotlin-model:2.2.6")
    api("io.qameta.allure:allure-kotlin-commons:2.2.6")
    api("io.qameta.allure:allure-kotlin-junit4:2.2.6")
    api("io.qameta.allure:allure-kotlin-android:2.2.6")
}
