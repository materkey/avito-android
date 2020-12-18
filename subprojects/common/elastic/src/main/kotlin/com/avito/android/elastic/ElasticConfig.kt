package com.avito.android.elastic

import java.io.Serializable
import java.net.URL

sealed class ElasticConfig : Serializable {

    object Disabled : ElasticConfig()

    data class Enabled(
        val endpoint: URL,
        val indexPattern: String,
        val buildId: String
    ) : ElasticConfig()
}