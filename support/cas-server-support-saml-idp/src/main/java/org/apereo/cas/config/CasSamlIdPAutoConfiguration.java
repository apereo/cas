package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlIdPAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@AutoConfiguration
@Import({
    SamlIdPConfiguration.class,
    SamlIdPThrottleConfiguration.class,
    SamlIdPAuthenticationServiceSelectionStrategyConfiguration.class,
    SamlIdPComponentSerializationConfiguration.class,
    SamlIdPWebflowConfiguration.class,
    SamlIdPEndpointsConfiguration.class,
    SamlIdPMetadataConfiguration.class,
    SamlIdPTicketCatalogConfiguration.class,
    SamlIdPTicketSerializationConfiguration.class,
    SamlIdPDelegatedAuthenticationConfiguration.class,
    SamlIdPMonitoringConfiguration.class
})
public class CasSamlIdPAutoConfiguration {
}
