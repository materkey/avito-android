package com.avito.android.util.matcher

import org.hamcrest.Matcher
import org.junit.jupiter.api.Test

abstract class AbstractMatcherTest {

    protected abstract fun createMatcher(): Matcher<*>

    
    fun `test is null safe`() {
        assertNullSafe(createMatcher())
    }

    
    fun `test copes with unknown types`() {
        createMatcher().matches(UnknownType())
    }
}
