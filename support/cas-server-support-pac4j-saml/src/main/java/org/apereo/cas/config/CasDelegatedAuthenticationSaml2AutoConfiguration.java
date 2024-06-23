package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasDelegatedAuthenticationSaml2AutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml")
@AutoConfiguration
@ImportAutoConfiguration(CasCoreSamlAutoConfiguration.class)
@Import({
    DelegatedAuthenticationSaml2Configuration.class,
    DelegatedAuthenticationSaml2MongoDbConfiguration.class,
    DelegatedAuthenticationSaml2JdbcConfiguration.class,
    DelegatedAuthenticationSaml2HazelcastConfiguration.class,
    DelegatedAuthenticationSaml2IdPConfiguration.class,
    DelegatedAuthenticationSaml2AmazonS3Configuration.class
})
public class CasDelegatedAuthenticationSaml2AutoConfiguration {
}
