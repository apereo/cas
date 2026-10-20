package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasOidcJwksMongoDbAutoConfiguration}. It runs before the OpenID Connect auto-configuration, so that
 * the MongoDb client JWKS store is defined first and the in-memory store that the core defines when none exists backs off.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "mongodb")
@AutoConfiguration(beforeName = "org.apereo.cas.config.CasOidcAutoConfiguration")
@Import({
    CasOidcMongoDbJwksConfiguration.class,
    CasOidcMongoDbClientJwksConfiguration.class
})
public class CasOidcJwksMongoDbAutoConfiguration {
}
