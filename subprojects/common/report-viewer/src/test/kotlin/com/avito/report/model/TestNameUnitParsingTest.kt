package com.avito.report.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestNameUnitParsingTest {


    fun `testName unit - parsed for avito data`() {
        assertThat(TestName(name = "com.avito.android.test.geo.MyTest.test").team).isEqualTo(Team("geo"))
    }


    fun `testName unit - parsed for domofond data`() {
        assertThat(TestName(name = "ru.domofond.test.MyTest.test").team).isEqualTo(Team("domofond"))
    }


    fun `unit parsed with subpackages`() {
        assertThat(TestName(name = "com.avito.android.test.auto.some_feature.MyTest.test").team)
            .isEqualTo(Team("auto"))
    }


    fun `unit parsed with underscore`() {
        assertThat(TestName(name = "com.avito.android.test.seller_x.some_feature.MyTest.test").team)
            .isEqualTo(Team("seller-x"))
    }


    fun `unit parsed with underscore with multiple subpackages`() {
        assertThat(TestName(name = "com.avito.android.test.seller_x.some_feature.some_inner_feature.MyTest.test").team)
            .isEqualTo(Team("seller-x"))
    }


    fun `unit undefined for illegal testName`() {
        assertThat(TestName(name = "illegalTestName").team).isEqualTo(Team.UNDEFINED)
    }
}
