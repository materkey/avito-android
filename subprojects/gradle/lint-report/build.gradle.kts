plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-gradle-plugin")
    id("nebula.integtest")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(libs.funktionaleTry)
    implementation(libs.kotlinHtml)
    implementation(libs.okhttp)

    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:slack"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("lintReport") {
            id = "com.avito.android.lint-report"
            implementationClass = "com.avito.android.lint.LintReportPlugin"
            displayName = "Lint reports merge"
        }
    }
}
