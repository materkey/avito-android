package com.avito.runner.scheduler.suite.filter

// it makes [name] instance field. It needs for gson
internal class ExcludeByReplaceTestListFilter(
    private val replaceTestList: List<String>?
) : TestsFilter {

    override val name = "ExcludeByReplaceTestListFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            replaceTestList == null -> TestsFilter.Result.Included
            replaceTestList.contains(test.name) -> TestsFilter.Result.Included
            else -> {
                TestsFilter.Result.Excluded.ExcludedByTestList(
                    name,
                    test.deviceName.name
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other.hashCode() == hashCode() && other.javaClass.isInstance(this)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
