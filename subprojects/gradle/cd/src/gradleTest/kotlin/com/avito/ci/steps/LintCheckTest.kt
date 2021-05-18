package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class LintCheckTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `lint check - runs lint task`() {
        generateProject()

        val buildResult = runBuild()
        buildResult.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:lintRelease", TaskOutcome.SUCCESS)
    }

    private fun runBuild(): TestResult {
        return gradlew(
            projectDir,
            "app:release",
            "-Pci=true",
        )
    }

    @Suppress("MaxLineLength")
    private fun generateProject() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.cd")
                    },
                    customScript = """
                            builds {
                                release {
                                    useImpactAnalysis = false
                                    lint { }
                                    artifacts {
                                        file("lintReportHtml", "${projectDir.path}/app/build/reports/lint-results-release.html")
                                        file("lintReportXml", "${projectDir.path}/app/build/reports/lint-results-release.xml")
                                    }
                                }
                            }
                        """.trimIndent()
                )
            )
        ).generateIn(projectDir)
    }
}