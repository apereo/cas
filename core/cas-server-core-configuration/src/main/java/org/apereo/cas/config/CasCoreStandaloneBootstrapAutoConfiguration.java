package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasCoreStandaloneBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SuppressWarnings("ConditionalOnProperty")
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false", matchIfMissing = true)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
@Import(CasCoreBaseStandaloneConfiguration.class)
public class CasCoreStandaloneBootstrapAutoConfiguration {
}
