package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasWsFederationAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederation)
@AutoConfiguration
@Import({
    WsFedAuthenticationEventExecutionPlanConfiguration.class,
    WsFederationAuthenticationConfiguration.class
})
@Deprecated(since = "8.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class CasWsFederationAuthenticationAutoConfiguration {
}
