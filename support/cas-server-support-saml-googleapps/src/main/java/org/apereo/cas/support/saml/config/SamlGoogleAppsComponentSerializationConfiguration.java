package org.apereo.cas.support.saml.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlGoogleAppsComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 * @deprecated Since 6.2, to be replaced with CAS SAML2 identity provider functionality.
 */
@Configuration(value = "samlGoogleAppsComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class SamlGoogleAppsComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "googleAppsComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer googleAppsComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(GoogleAccountsService.class);
    }
}
