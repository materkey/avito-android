package com.avito.android

import com.avito.utils.fileFromJarResources
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class TestSuiteLoaderIntegrationTest {

    private lateinit var testSuiteLoader: TestSuiteLoader

    @BeforeEach
    fun setUp() {
        testSuiteLoader = TestSuiteLoaderImpl()
    }

    
    fun `load test suite - returns all test cases - without annotation filter`() {
        val actualTests = testSuiteLoader.loadTestSuite(getTestApk()).getOrThrow()

        val expectedTests = listOf(
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.TestClass",
                methodName = "someTest1",
                annotations = listOf(AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)))
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.TestClass",
                methodName = "someTest2",
                annotations = listOf(AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)))
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.TestClass",
                methodName = "ignoredTest1",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("org.junit.Ignore", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.TestClass",
                methodName = "ignoredTest2",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("org.junit.Ignore", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.AnnotatedClass",
                methodName = "someTest1",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.example.dimorinny.test.FunctionalTest", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.AnnotatedClass",
                methodName = "someTest2",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.example.dimorinny.test.FunctionalTest", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.AnnotatedClass",
                methodName = "ignoredTest1",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.example.dimorinny.test.FunctionalTest", emptyMap()),
                    AnnotationData("org.junit.Ignore", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.AnnotatedClass",
                methodName = "ignoredTest2",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.example.dimorinny.test.FunctionalTest", emptyMap()),
                    AnnotationData("org.junit.Ignore", emptyMap())
                )
            ),
            TestInApk.createStubInstance(
                className = "com.example.dimorinny.test.IgnoredClass",
                methodName = "someTest1",
                annotations = listOf(
                    AnnotationData("com.example.dimorinny.test.CaseId", mapOf("value" to 0)),
                    AnnotationData("org.junit.Ignore", emptyMap())
                )
            )
        )

        assertThat(expectedTests).containsAtLeastElementsIn(actualTests.toTypedArray())
    }

    @Suppress("MaxLineLength")
    
    fun `get tests - pass - when parsed apk contains test classes without CaseId annotation and missed case if is allowed`() {
        testSuiteLoader = TestSuiteLoaderImpl()
        val suite = testSuiteLoader.loadTestSuite(getTestApkWithoutAnnotations()).getOrThrow()
        assertThat(suite.size).isEqualTo(9)
    }

    
    fun `get tests - returns annotation with values`() {
        testSuiteLoader = TestSuiteLoaderImpl()
        val suite = testSuiteLoader.loadTestSuite(getTestApkWithAnnotationWithValues()).getOrThrow()
        assertThat(suite.size).isEqualTo(1)
        val loadedTest = suite.first()

        val expectedClassAnnotation = AnnotationData(
            name = "com.example.dimorinny.AnnotationWithValues",
            values = mapOf(
                "intValue" to 248,
                "stringValue" to "on class",
                "multipleIntValue" to listOf(1, 2, 3)
            )
        )

        val expectedMethodAnnotation = AnnotationData(
            name = "com.example.dimorinny.AnnotationWithValues",
            values = mapOf(
                "intValue" to 1,
                "stringValue" to "on method",
                "multipleIntValue" to listOf(10, 20, 30)
            )
        )

        assertThat(loadedTest.annotations).containsExactly(expectedClassAnnotation, expectedMethodAnnotation)
    }

    /**
     * APK contains 1 instrumentation test class:
     * <blockquote><pre>
     * {@code
     *
     *  @AnnotationWithValues(
     *       intValue = 248,
     *       stringValue = "on class",
     *       multipleIntValue = [1,2,3]
     *       )
     *   class ClassWithAnnotation {
     *
     *       @AnnotationWithValues(
     *           intValue = 1,
     *           stringValue = "on method",
     *           multipleIntValue = [10,20,30]
     *       )
     *       
     *       fun methodWithAnnotation() { }
     *   }
     *
     */
    private fun getTestApkWithAnnotationWithValues(): File {
        return fileFromJarResources<TestSuiteLoaderIntegrationTest>("test-annotation-with-values.apk")
    }

    /**
     * APK contains 3 instrumentation test classes:
     * <blockquote><pre>
     * {@code
     *
     * class TestClass {
     *       fun someTest1()
     *       fun someTest2()
     *      @Ignore  fun ignoredTest1()
     *      @Ignore  fun ignoredTest2()
     * }
     *
     * @FunctionalTest
     * class AnnotatedClass {
     *       fun someTest1()
     *       fun someTest2()
     *      @Ignore  fun ignoredTest1()
     *      @Ignore  fun ignoredTest2()
     * }
     *
     * @Ignore
     * class IgnoredClass {
     *       fun someTest1()
     * }
     *
     * }
     * </pre></blockquote>
     */
    private fun getTestApkWithoutAnnotations(): File {
        return fileFromJarResources<TestSuiteLoaderIntegrationTest>("test-without-annotations.apk")
    }

    /**
     * APK contains 3 instrumentation test classes:
     * <blockquote><pre>
     * {@code
     *
     * @CaseId(0)
     * class TestClass {
     *      fun someTest1() {}
     *      fun someTest2() {}
     *     @Ignore  fun ignoredTest1() {}
     *     @Ignore  fun ignoredTest2() {}
     * }
     *
     * @FunctionalTest
     * @CaseId(0)
     * class AnnotatedClass {
     *      fun someTest1() {}
     *      fun someTest2() {}
     *     @Ignore  fun ignoredTest1() {}
     *     @Ignore  fun ignoredTest2() {}
     * }
     *
     * @Ignore
     * @CaseId(0)
     * class IgnoredClass {
     *      fun someTest1() {}
     * }
     *
     * }
     * </pre></blockquote>
     */
    private fun getTestApk() = fileFromJarResources<TestSuiteLoaderIntegrationTest>("test.apk")
}
