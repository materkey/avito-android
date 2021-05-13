package com.avito.kotlin.dsl

import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class GetOptionalStringPropertyTest {

    // todo null by default
    
    fun `getOptionalStringProperty - returns empty string - on empty string by default`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = ""

        val value = project.getOptionalStringProperty("someProperty")
        assertThat(value).isEqualTo("")
    }

    
    fun `getOptionalStringProperty - returns null - on no property`() {
        val project = ProjectBuilder.builder().build()

        val value = project.getOptionalStringProperty("someProperty")
        assertThat(value).isNull()
    }

    
    fun `getOptionalStringProperty - returns correct value`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = "12345"

        val value = project.getOptionalStringProperty("someProperty")
        assertThat(value).isEqualTo("12345")
    }

    
    fun `getOptionalStringProperty - returns default - on no value by default`() {
        val project = ProjectBuilder.builder().build()

        val value = project.getOptionalStringProperty("someProperty", default = "4321")
        assertThat(value).isEqualTo("4321")
    }

    
    fun `getOptionalStringProperty - returns default - on no value with defaultIfBlank=false`() {
        val project = ProjectBuilder.builder().build()

        val value = project.getOptionalStringProperty("someProperty", default = "4321", defaultIfBlank = false)
        assertThat(value).isEqualTo("4321")
    }

    @Suppress("MaxLineLength")
    
    fun `getOptionalStringProperty - returns empty string instead of default - on empty value with defaultIfBlank=false`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["someProperty"] = ""

        val value = project.getOptionalStringProperty("someProperty", default = "4321", defaultIfBlank = false)
        assertThat(value).isEqualTo("")
    }
}
