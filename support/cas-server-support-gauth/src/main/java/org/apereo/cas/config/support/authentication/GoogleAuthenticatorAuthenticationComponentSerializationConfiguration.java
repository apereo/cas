package org.apereo.cas.config.support.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurator;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link GoogleAuthenticatorAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "googleAuthenticatorAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleAuthenticatorAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(GoogleAuthenticatorTokenCredential.class);
    }
}
