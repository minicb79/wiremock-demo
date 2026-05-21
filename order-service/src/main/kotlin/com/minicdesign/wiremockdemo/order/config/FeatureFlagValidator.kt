package com.minicdesign.wiremockdemo.order.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component

@Component
class FeatureFlagValidator(
    private val environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(FeatureFlagValidator::class.java)

    @PostConstruct
    fun validateFeatureFlags() {
        val rawViaInterceptor = environment.getProperty("feature.wiremock-via-interceptor", Boolean::class.java)
        val rawAsProxy = environment.getProperty("feature.wiremock-as-proxy", Boolean::class.java)

        val viaInterceptor = rawViaInterceptor ?: false
        val asProxy = rawAsProxy ?: false

        val viaInterceptorLog = if (rawViaInterceptor == null) "false (default)" else "$viaInterceptor (explicit)"
        val asProxyLog = if (rawAsProxy == null) "false (default)" else "$asProxy (explicit)"

        val isProd = environment.acceptsProfiles(Profiles.of("prod"))

        logger.info(
            "Feature flag validation at startup - Profile: prod={}, wiremock-via-interceptor={}, wiremock-as-proxy={}",
            isProd,
            viaInterceptorLog,
            asProxyLog,
        )

        if (isProd) {
            if (viaInterceptor || asProxy) {
                val errorMsg =
                    "Startup Failed: WireMock mocking features are not allowed in production! " +
                        "Active config: wiremock-via-interceptor=$viaInterceptor, wiremock-as-proxy=$asProxy"
                logger.error(errorMsg)
                throw IllegalStateException(errorMsg)
            }
        } else {
            if (viaInterceptor && asProxy) {
                val errorMsg =
                    "Startup Failed: Invalid feature flag configuration. " +
                        "feature.wiremock-via-interceptor and feature.wiremock-as-proxy cannot both be enabled simultaneously."
                logger.error(errorMsg)
                throw IllegalStateException(errorMsg)
            }
        }
    }
}
