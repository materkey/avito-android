package com.avito.ci.steps

import com.avito.cd.AndroidArtifactType
import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildConfig
import com.avito.cd.CdBuildConfig.Deployment
import com.avito.cd.NupokatiProject
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class UploadToQappsConditionTest {

    
    fun `can upload - has no cd config`() {
        val condition = UploadToQappsCondition(null)

        assertThat(condition.canUpload()).isTrue()
    }

    
    fun `regular build - has no cd config`() {
        val condition = UploadToQappsCondition(null)

        assertThat(condition.isReleaseChain()).isFalse()
    }

    
    fun `no upload - has no qapps deployment`() {
        val config = config(
            deployments = listOf(googlePlayDeployment())
        )
        val condition = UploadToQappsCondition(config)

        assertThat(condition.canUpload()).isFalse()
    }

    
    fun `can upload - has qapps deployment`() {
        val config = config(
            deployments = listOf(
                googlePlayDeployment(),
                Deployment.Qapps(isRelease = true)
            )
        )
        val condition = UploadToQappsCondition(config)

        assertThat(condition.canUpload()).isTrue()
    }

    
    fun `regular build - non release qapps deployment`() {
        val config = config(
            deployments = listOf(
                Deployment.Qapps(isRelease = false)
            )
        )
        val condition = UploadToQappsCondition(config)

        assertThat(condition.isReleaseChain()).isFalse()
    }

    
    fun `release build - release qapps deployment`() {
        val config = config(
            deployments = listOf(
                Deployment.Qapps(isRelease = true)
            )
        )
        val condition = UploadToQappsCondition(config)

        assertThat(condition.isReleaseChain()).isTrue()
    }

    private fun config(
        deployments: List<Deployment> = emptyList()
    ) = CdBuildConfig(
        schemaVersion = 1,
        project = NupokatiProject.AVITO,
        releaseVersion = "1.0",
        outputDescriptor = CdBuildConfig.OutputDescriptor(
            path = "http://stub",
            skipUpload = false
        ),
        deployments = deployments
    )

    private fun googlePlayDeployment() = Deployment.GooglePlay(
        artifactType = AndroidArtifactType.BUNDLE,
        buildVariant = BuildVariant.RELEASE,
        track = Deployment.Track.INTERNAL
    )
}
