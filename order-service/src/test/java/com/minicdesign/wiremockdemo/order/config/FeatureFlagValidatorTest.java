package com.minicdesign.wiremockdemo.order.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureFlagValidatorTest {

    @Test
    public void shouldPassWhenBothFlagsAreDisabledInNonProdEnvironment() {
        MockEnvironment env = new MockEnvironment(); // non-prod by default
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        assertDoesNotThrow(validator::validateFeatureFlags);
    }

    @Test
    public void shouldPassWhenOnlyViaInterceptorIsEnabledInNonProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("feature.wiremock-via-interceptor", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        assertDoesNotThrow(validator::validateFeatureFlags);
    }

    @Test
    public void shouldPassWhenOnlyAsProxyIsEnabledInNonProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("feature.wiremock-as-proxy", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        assertDoesNotThrow(validator::validateFeatureFlags);
    }

    @Test
    public void shouldFailWhenBothFlagsAreEnabledInNonProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("feature.wiremock-via-interceptor", "true");
        env.setProperty("feature.wiremock-as-proxy", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateFeatureFlags);
        assertEquals(
                "Startup Failed: Invalid feature flag configuration. " +
                "feature.wiremock-via-interceptor and feature.wiremock-as-proxy cannot both be enabled simultaneously.",
                exception.getMessage()
        );
    }

    @Test
    public void shouldPassWhenBothFlagsAreDisabledInProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        assertDoesNotThrow(validator::validateFeatureFlags);
    }

    @Test
    public void shouldFailWhenViaInterceptorIsEnabledInProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("feature.wiremock-via-interceptor", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateFeatureFlags);
        assertEquals(
                "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=true, wiremock-as-proxy=false",
                exception.getMessage()
        );
    }

    @Test
    public void shouldFailWhenAsProxyIsEnabledInProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("feature.wiremock-as-proxy", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateFeatureFlags);
        assertEquals(
                "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=false, wiremock-as-proxy=true",
                exception.getMessage()
        );
    }

    @Test
    public void shouldFailWhenBothFlagsAreEnabledInProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("feature.wiremock-via-interceptor", "true");
        env.setProperty("feature.wiremock-as-proxy", "true");
        FeatureFlagValidator validator = new FeatureFlagValidator(env);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateFeatureFlags);
        assertEquals(
                "Startup Failed: WireMock mocking features are not allowed in production! " +
                "Active config: wiremock-via-interceptor=true, wiremock-as-proxy=true",
                exception.getMessage()
        );
    }
}
