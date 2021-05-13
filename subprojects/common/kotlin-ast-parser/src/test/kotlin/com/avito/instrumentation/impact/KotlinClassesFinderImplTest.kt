package com.avito.instrumentation.impact

import com.avito.test.gradle.file
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Tests could be improved with real compilation in temp directory
 * and following assertion between KotlinClassesFinder and compile results
 */
internal class KotlinClassesFinderImplTest {

    private lateinit var dir: File

    private lateinit var noClassFile: File
    private lateinit var anotherTestFile: File
    private lateinit var multipleClassesTestFile: File

    @BeforeEach
    fun setup(@TempDir dir: File) {
        this.dir = dir

        noClassFile = dir.file(
            "TestGroup.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            
            fun testOne() {
            }
            
            
            fun testTwo() {
            }
        """.trimIndent()
        )

        anotherTestFile = dir.file(
            "AnotherTest.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class AnotherTest {
            
                
                fun test() {
                }
            }
        """.trimIndent()
        )

        multipleClassesTestFile = dir.file(
            "YetAnotherTest.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class TestClassInFileOne {
            
                
                fun test() {
                }
            }
            
            class TestClassInFileTwo {
            
                
                fun test() {
                }
            }
        """.trimIndent()
        )
    }


    fun `full class name - simple class file`() {
        assertThat(findClasses(anotherTestFile)).containsExactly("com.avito.android.test.AnotherTest")
    }


    fun `finds all tests in modified class`() {
        assertThat(findClasses(noClassFile)).containsExactly("com.avito.android.test.TestGroupKt")
    }


    fun `finds all tests in modified file with multiple classes`() {
        assertThat(findClasses(multipleClassesTestFile)).containsExactly(
            "com.avito.android.test.TestClassInFileOne",
            "com.avito.android.test.TestClassInFileTwo"
        )
    }

    private fun findClasses(file: File): List<String> {
        val kotlinClassesFinder = KotlinClassesFinderImpl()
        return kotlinClassesFinder.findClasses(file).toList().map { it.toString() }
    }
}
