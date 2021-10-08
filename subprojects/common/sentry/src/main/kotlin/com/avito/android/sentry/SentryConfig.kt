package com.avito.android.sentry

import java.io.Serializable

/**
 * Some payloads are bigger than default 400 symbols but helpful for troubleshooting
 *
 * https://github.com/getsentry/sentry-java/issues/543
 */
private const val DEFAULT_SENTRY_MAX_STRING: Int = 50_000

/**
 * Default config for SentryClient
 */
sealed class SentryConfig : Serializable {

    object Disabled : SentryConfig()

    data class Enabled(
        val dsn: String,
        val environment: String,
        val serverName: String,
        val release: String?,
        val tags: Map<String, String>,
        val maxStringLength: Int = DEFAULT_SENTRY_MAX_STRING
    ) : SentryConfig()
}
