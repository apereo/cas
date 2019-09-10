package org.apereo.cas.support.saml.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurator;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlGoogleAppsComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("samlGoogleAppsComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlGoogleAppsComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(GoogleAccountsService.class);
    }
}
