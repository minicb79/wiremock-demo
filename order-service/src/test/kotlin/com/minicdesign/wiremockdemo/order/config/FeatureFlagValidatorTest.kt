package com.minicdesign.wiremockdemo.order.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.env.MockEnvironment

class FeatureFlagValidatorTest {
    @Test
    fun `should pass when both flags are disabled in non-prod environment`() {
        val env = MockEnvironment() // non-prod by default
        val validator = FeatureFlagValidator(env)

        assertDoesNotThrow {
            validator.validateFeatureFlags()
        }
    }

    @Test
    fun `should pass when only via-interceptor is enabled in non-prod environment`() {
        val env = MockEnvironment()
        env.setProperty("feature.wiremock-via-interceptor", "true")
        val validator = FeatureFlagValidator(env)

        assertDoesNotThrow {
            validator.validateFeatureFlags()
        }
    }

    @Test
    fun `should pass when only as-proxy is enabled in non-prod environment`() {
        val env = MockEnvironment()
        env.setProperty("feature.wiremock-as-proxy", "true")
        val validator = FeatureFlagValidator(env)

        assertDoesNotThrow {
            validator.validateFeatureFlags()
        }
    }

    @Test
    fun `should fail when both flags are enabled in non-prod environment`() {
        val env = MockEnvironment()
        env.setProperty("feature.wiremock-via-interceptor", "true")
        env.setProperty("feature.wiremock-as-proxy", "true")
        val validator = FeatureFlagValidator(env)

        val exception =
            assertThrows<IllegalStateException> {
                validator.validateFeatureFlags()
            }
        assertEquals(
            "Startup Failed: Invalid feature flag configuration. " +
                "feature.wiremock-via-interceptor and feature.wiremock-as-proxy cannot both be enabled simultaneously.",
            exception.message,
        )
    }

    @Test
    fun `should pass when both flags are disabled in prod environment`() {
        val env = MockEnvironment()
        env.setActiveProfiles("prod")
        val validator = FeatureFlagValidator(env)

        assertDoesNotThrow {
            validator.validateFeatureFlags()
        }
    }

    @Test
    fun `should fail when via-interceptor is enabled in prod environment`() {
        val env = MockEnvironment()
        env.setActiveProfiles("prod")
        env.setProperty("feature.wiremock-via-interceptor", "true")
        val validator = FeatureFlagValidator(env)

        val exception =
            assertThrows<IllegalStateException> {
                validator.validateFeatureFlags()
            }
        assertEquals(
            "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=true, wiremock-as-proxy=false",
            exception.message,
        )
    }

    @Test
    fun `should fail when as-proxy is enabled in prod environment`() {
        val env = MockEnvironment()
        env.setActiveProfiles("prod")
        env.setProperty("feature.wiremock-as-proxy", "true")
        val validator = FeatureFlagValidator(env)

        val exception =
            assertThrows<IllegalStateException> {
                validator.validateFeatureFlags()
            }
        assertEquals(
            "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=false, wiremock-as-proxy=true",
            exception.message,
        )
    }

    @Test
    fun `should fail when both flags are enabled in prod environment`() {
        val env = MockEnvironment()
        env.setActiveProfiles("prod")
        env.setProperty("feature.wiremock-via-interceptor", "true")
        env.setProperty("feature.wiremock-as-proxy", "true")
        val validator = FeatureFlagValidator(env)

        val exception =
            assertThrows<IllegalStateException> {
                validator.validateFeatureFlags()
            }
        assertEquals(
            "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=true, wiremock-as-proxy=true",
            exception.message,
        )
    }
}
