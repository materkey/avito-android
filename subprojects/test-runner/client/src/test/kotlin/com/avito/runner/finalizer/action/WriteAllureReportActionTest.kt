package com.avito.runner.finalizer.action

import com.avito.report.model.*
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.test.model.TestName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class WriteAllureReportActionTest {

    private lateinit var file: File
    private lateinit var unskippedDir: File
    private lateinit var skippedDir: File

    @BeforeEach
    fun setup(@TempDir temp: Path) {
        file = temp.toFile()
        unskippedDir = File(file.absolutePath.plus("/unskipped_tests"))
        skippedDir = File(file.absolutePath.plus("/skipped_tests"))
    }

    @Test
    fun `allure report - contains environment`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            name = TestName(
                                "com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest",
                                "resolve_advert_legacyFormat"
                            )
                        )
                    )
                )
            )
        )
        val rawFiles = File(file, "environment.xml")
        assertTrue(rawFiles.exists())
    }

    @Test
    fun `allure report - check md5`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            name = TestName(
                                "com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest",
                                "resolve_advert_legacyFormat"
                            )
                        )
                    )
                )
            )
        )
        val hasMd5 = unskippedDir.listFiles()?.find {
            // md5(className+method) (without dot after className)
            val md5 = "222838125e2da90eaaa8fb6335dc94b4"
            it.readText().contains(md5)
        } != null
        assertTrue(hasMd5)
    }

    @Test
    fun `allure report - contains report file`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            name = TestName(
                                "com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest",
                                "resolve_advert_legacyFormat"
                            )
                        )
                    )
                )
            )
        )
        val listFiles: List<File> = unskippedDir.listFiles()!!.toList()
        val hasResultFile = listFiles.find { it.name.endsWith("-result.json") } != null
        println(listFiles.find { it.name.endsWith("-result.json") }?.readText())
        assertTrue(hasResultFile)
    }

    @Test
    fun `allure report - broken to failed converter works`() {
        unskippedDir.mkdirs()
        File(unskippedDir, "asd67as-2da-3asd4d-asd-result.json").apply {
            writeText(
                "{\n" +
                    "    \"uuid\": \"asd67as-2da-3asd4d-asd\",\n" +
                    "    \"name\": \"resolve_advert_legacyFormat\",\n" +
                    "    \"fullName\": \"com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest\",\n" +
                    "    \"start\": 1638481614640,\n" +
                    "    \"stop\": 1638481671520,\n" +
                    "    \"stage\": \"finished\",\n" +
                    "    \"description\": null,\n" +
                    "    \"descriptionHtml\": null,\n" +
                    "    \"status\": \"broken\",\n" +
                    "    \"statusDetails\": null,\n" +
                    "    \"labels\": [],\n" +
                    "    \"links\": []\n" +
                    "}"
            )
        }
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            name = TestName(
                                "com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest",
                                "resolve_advert_legacyFormat"
                            )
                        ),
                        testRuntimeData = TestRuntimeDataPackage.createStubInstance().copy(
                            incident = Incident(
                                type = Incident.Type.ASSERTION_FAILED,
                                timestamp = 0,
                                trace = listOf(),
                                chain = listOf(),
                                entryList = emptyList()
                            )
                        )
                    )
                )
            )
        )
        unskippedDir.listFiles()?.forEach {
            println(
                it.readText()
            )
            val noBrokenStatus = !it.readText().contains("broken")
            assertTrue(noBrokenStatus)
        }
    }

    private fun mockData(verdict: Verdict) {
        WriteAllureReportAction(
            file,
        ).action(
            verdict = verdict
        )
    }
}
