package com.avito.math

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PercentKtTest {


    fun `percent of int - round is correct - no decimal`() {
        assertThat(4.percentOf(8).toInt()).isEqualTo(50)
    }


    fun `percent of int - round is correct - decimal`() {
        assertThat(5.percentOf(8).toInt()).isEqualTo(62)
    }


    fun `percent of int - two decimal string is correct - no decimal`() {
        assertThat(4.percentOf(8).twoDigitsString()).isEqualTo("50%")
    }


    fun `percent of int - two decimal string is correct - decimal`() {
        assertThat(5.percentOf(8).twoDigitsString()).isEqualTo("62.5%")
    }


    fun `double percent - two decimal string is correct - decimal`() {
        assertThat(0.2345.percent().twoDigitsString()).isEqualTo("23.45%")
    }


    fun `float percent - two decimal string is correct - decimal`() {
        assertThat(0.2345F.percent().twoDigitsString()).isEqualTo("23.45%")
    }


    fun `percent - throws exception - not in range`() {
        assertThrows<IllegalArgumentException> {
            23.45.percent()
        }
    }
}
