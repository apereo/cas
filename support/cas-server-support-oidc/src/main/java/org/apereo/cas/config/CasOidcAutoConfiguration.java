package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasOidcAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@AutoConfiguration
@Import({
    OidcConfiguration.class,
    OidcResponseConfiguration.class,
    OidcJwksConfiguration.class,
    OidcEndpointsConfiguration.class,
    OidcLogoutConfiguration.class,
    OidcComponentSerializationConfiguration.class,
    OidcThrottleConfiguration.class,
    OidcAuditConfiguration.class,
    OidcWebflowConfiguration.class,
    OidcFederationConfiguration.class
})
public class CasOidcAutoConfiguration {
}
