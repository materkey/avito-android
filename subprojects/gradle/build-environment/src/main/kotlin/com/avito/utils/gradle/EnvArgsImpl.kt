package com.avito.utils.gradle

import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable
import java.util.concurrent.TimeUnit

internal class EnvArgsImpl(project: Project) : EnvArgs, Serializable {

    /**
     * todo make local by default?
     */
    override val build: EnvArgs.Build = when (project.getOptionalStringProperty("avito.build", "teamcity")) {
        "teamcity" -> {
            val teamcityBuildId = project.getMandatoryStringProperty("teamcityBuildId")
            EnvArgs.Build.Teamcity(
                id = teamcityBuildId,
                url = project.getMandatoryStringProperty("teamcityUrl") +
                        "/viewLog.html?buildId=$teamcityBuildId&tab=buildLog",
                number = project.getMandatoryStringProperty("buildNumber"),
                type = "teamcity-${project.getMandatoryStringProperty("teamcityBuildType")}"
            )
        }
        "local" -> {
            val id = project.getOptionalStringProperty(
                name = "localBuildId",
                default = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
            )
            EnvArgs.Build.Local(id)
        }
        else ->
            throw IllegalStateException("property avito.build must be 'teamcity' or 'local'")
    }
}
