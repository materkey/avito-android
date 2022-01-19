package com.avito.android.provider

import com.avito.android.model.CdBuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Transformer
import org.gradle.api.file.ProjectLayout
import java.io.File

internal class CdBuildConfigTransformer(
    private val rootProjectLayout: ProjectLayout,
    private val validator: CdBuildConfigValidator,
) : Transformer<CdBuildConfig, String> {

    override fun transform(configFilePath: String): CdBuildConfig {
        val configFile = rootProjectLayout.projectDirectory.file(configFilePath).asFile

        return deserializeToCdBuildConfig(configFile).also {
            // todo test on failed validation
            validator.validate(it)
        }
    }

    private fun deserializeToCdBuildConfig(configFile: File): CdBuildConfig {
        require(configFile.exists()) { "Can't find cd config file in $configFile" }
        return uploadCdGson.fromJson(configFile.reader(), CdBuildConfig::class.java)
    }
}

internal val uploadCdGson: Gson by lazy {
    GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .registerTypeAdapter(CdBuildConfig.Deployment::class.java, DeploymentDeserializer)
        .create()
}