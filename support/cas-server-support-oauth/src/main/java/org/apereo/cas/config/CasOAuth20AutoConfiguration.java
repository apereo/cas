package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasOAuth20AutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@AutoConfiguration
@Import({
    CasOAuth20Configuration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuth20ComponentSerializationConfiguration.class,
    CasOAuth20ThrottleConfiguration.class,
    CasOAuth20TicketSerializationConfiguration.class,
    CasOAuth20ServicesConfiguration.class,
    CasOAuth20EndpointsConfiguration.class
})
public class CasOAuth20AutoConfiguration {
}
