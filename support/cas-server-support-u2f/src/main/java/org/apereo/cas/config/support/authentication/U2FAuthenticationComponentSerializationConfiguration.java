package org.apereo.cas.config.support.authentication;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.adaptors.u2f.U2FTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link U2FAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("u2fAuthenticationComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(U2FTokenCredential.class);
    }
}
