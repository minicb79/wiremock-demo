package com.minicdesign.wiremockdemo.order.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagValidator {
	private final Environment environment;

	@PostConstruct
	public void validateFeatureFlags() {
		Boolean rawViaInterceptor = environment.getProperty("feature.wiremock-via-interceptor", Boolean.class);
		Boolean rawAsProxy = environment.getProperty("feature.wiremock-as-proxy", Boolean.class);

		boolean viaInterceptor = rawViaInterceptor != null ? rawViaInterceptor : false;
		boolean asProxy = rawAsProxy != null ? rawAsProxy : false;

		String viaInterceptorLog = rawViaInterceptor == null ? "false (default)" : viaInterceptor + " (explicit)";
		String asProxyLog = rawAsProxy == null ? "false (default)" : asProxy + " (explicit)";

		boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

		log.info(
				"Feature flag validation at startup - Profile: prod={}, wiremock-via-interceptor={}, wiremock-as-proxy={}",
				isProd, viaInterceptorLog, asProxyLog);

		if (isProd) {
			if (viaInterceptor || asProxy) {
				String errorMsg = "Startup Failed: WireMock mocking features are not allowed in production! "
						+ "Active config: wiremock-via-interceptor=" + viaInterceptor + ", wiremock-as-proxy="
						+ asProxy;
				log.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			}
		} else {
			if (viaInterceptor && asProxy) {
				String errorMsg = "Startup Failed: Invalid feature flag configuration. "
						+ "feature.wiremock-via-interceptor and feature.wiremock-as-proxy cannot both be enabled simultaneously.";
				log.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			}
		}
	}
}
