package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import org.gradle.api.Project
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Used in build.gradle's of avito
public val Project.envArgs: EnvArgs by ProjectProperty.lazy<EnvArgs>(scope = ROOT_PROJECT) { project ->
    EnvArgsImpl(project)
}

public interface EnvArgs {

    public sealed class Build : Serializable {
        /**
         * todo change to string after refactor [TestRunEnvironment.teamcityBuildId]
         */
        public abstract val id: String
        public abstract val url: String
        public abstract val number: String
        public abstract val type: String

        internal class Local(override val id: String) : Build() {
            override val url = "No url. This is local build"
            override val number = "local"
            override val type = "local-$userName"

            companion object {
                private val userName: String? = System.getProperty("user.name")
            }
        }

        public data class Teamcity(
            override val id: String,
            override val url: String,
            override val number: String,
            override val type: String
        ) : Build()
    }

    public val build: Build
}
