package org.apereo.cas.config.support.authentication;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RadiusTokenAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("radiusTokenAuthenticationComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusTokenAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(RadiusTokenCredential.class);
    }
}
