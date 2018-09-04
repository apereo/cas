package org.apereo.cas.support.pac4j.config.support.authentication;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Pac4jDelegatedAuthenticationSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("pac4jDelegatedAuthenticationSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jDelegatedAuthenticationSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(ClientCredential.class);
    }
}
