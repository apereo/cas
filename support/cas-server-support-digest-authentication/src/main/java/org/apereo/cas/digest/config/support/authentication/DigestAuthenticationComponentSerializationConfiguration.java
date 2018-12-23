package org.apereo.cas.digest.config.support.authentication;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.digest.DigestCredential;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DigestAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("digestAuthenticationComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DigestAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DigestCredential.class);
    }
}
