package com.avito.k8s

import com.avito.android.Result
import com.avito.android.waiter.waitForCondition
import com.avito.k8s.model.KubeContainer
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext

internal class KubernetesApiImpl(
    private val kubernetesClient: KubernetesClient,
    loggerFactory: LoggerFactory,
    override val needForward: Boolean,
) : KubernetesApi {

    private val logger = loggerFactory.create<KubernetesApi>()

    override val namespace: String = kubernetesClient.namespace

    override suspend fun deletePod(podName: String): Boolean {
        return kubernetesClient.pods().withName(podName).withGracePeriod(0).delete()
    }

    override suspend fun getPodLogs(podName: String): String {
        return kubernetesClient.pods().withName(podName).log
    }

    override suspend fun getPodDescription(podName: String): String {
        return try {
            val actualPod = getPod(podName)
            if (actualPod != null) {
                "$actualPod"
            } else {
                "pod doesn't exist"
            }
        } catch (e: Exception) {
            logger.warn("Can't get pod info", e)
            "Error when get pod description, ${e.message}"
        }
    }

    override suspend fun deleteDeployment(deploymentName: String) {
        try {
            logger.debug("Deleting deployment: $deploymentName")
            kubernetesClient.apps().deployments().withName(deploymentName).withGracePeriod(0).delete()
            logger.debug("Deployment: $deploymentName deleted")
        } catch (t: Throwable) {
            logger.warn("Failed to delete deployment $deploymentName", t)
        }
    }

    override suspend fun createDeployment(deployment: Deployment) {
        logger.debug("Deployment.create(): start $deployment")
        kubernetesClient.apps().deployments().create(deployment)
        logger.debug("Deployment.create(): client returned")

        waitForDeploymentCreationDone(
            deploymentName = deployment.metadata.name,
            count = deployment.spec.replicas
        )
    }

    private fun getPod(podName: String): KubePod? {
        return kubernetesClient.pods()
            .withName(podName)
            .get()
            ?.let { KubePod(it) }
    }

    private suspend fun waitForDeploymentCreationDone(
        deploymentName: String,
        count: Int
    ) {
        logger.debug("waitForDeploymentCreationDone name=$deploymentName count=$count")
        waitForCondition(
            conditionName = "Deployment $deploymentName deployed",
            onSuccess = { conditionName: String, durationMs: Long, attempt: Int ->
                logger.debug("$conditionName succeed in $durationMs at attempt=$attempt")
            },
            sleepAction = { frequencyMs: Long -> delay(frequencyMs) }
        ) {
            getPods(deploymentName).flatMap { pods ->
                if (pods.size == count) {
                    Result.Success(pods)
                } else {
                    Result.Failure(IllegalStateException("Pods count is ${pods.size} but expected $count"))
                }
            }
        }.onFailure {
            throw IllegalStateException("Can't create deployment: $deploymentName", it)
        }
    }

    override suspend fun getPods(deploymentName: String): Result<List<KubePod>> {
        return Result.tryCatch {
            // prevent KubernetesClientException
            retry {
                kubernetesClient.pods()
                    .withLabel("deploymentName", deploymentName)
                    .list()
                    .items
                    .map {
                        KubePod(
                            pod = it,
                            portForward = if (needForward) {
                                kubernetesClient.pods().withName(it.metadata.name).portForward(5555)
                            } else null
                        )
                    }
                    .onEach { pod ->
                        val phase = pod.container.phase
                        if (phase is KubeContainer.ContainerPhase.Waiting) {
                            if (phase.hasProblemsGettingImage()) {
                                error("Problems getting container image: ${phase.message}")
                            }
                        }
                    }
            }
        }.onSuccess { pods ->
            logger.debug(
                "Getting pods for deployment $deploymentName:\n" +
                    pods.joinToString(separator = "\n")
            )
        }.onFailure {
            logger.warn("Can't get pods for deployment $deploymentName", it)
        }
    }

    private suspend fun <T> retry(
        times: Int = Int.MAX_VALUE,
        initialDelay: Long = 1000, // 1 second
        maxDelay: Long = 10_000,    // 10 second
        factor: Double = 2.0,
        block: suspend () -> T): T
    {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                // you can log an error here and/or make a more finer-grained
                // analysis of the cause to see if retry is needed
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }
}
