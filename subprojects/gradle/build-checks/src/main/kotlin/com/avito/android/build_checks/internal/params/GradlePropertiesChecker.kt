package com.avito.android.build_checks.internal.params

import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.android.build_checks.pluginId
import com.avito.android.plugin.build_metrics.BuildMetricTracker
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.statsd
import org.gradle.api.Project

internal class GradlePropertiesChecker(
    private val project: Project,
    private val envInfo: BuildEnvironmentInfo
) {

    fun check() {
        project.afterEvaluate {
            val tracker = buildTracker(project)
            val propertiesChecks = listOf(
                GradlePropertiesCheck(project, envInfo) // TODO: extract to a task
            )
            propertiesChecks.forEach { checker ->
                checker.getMismatches()
                    .onSuccess {
                        it.forEach { mismatch ->
                            val safeParamName = mismatch.name.replace(".", "-")
                            tracker.track(
                                CountMetric(SeriesName.create("configuration", "mismatch", safeParamName))
                            )
                        }
                    }
                    .onFailure {
                        val checkerName = checker.javaClass.simpleName
                        tracker.track(
                            CountMetric(SeriesName.create("configuration", "mismatch", "failed", checkerName))
                        )
                    }
            }
        }
    }

    private fun buildTracker(project: Project): BuildMetricTracker {
        return BuildMetricTracker(project.environmentInfo(), project.statsd)
    }
}
