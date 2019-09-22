package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RadiusTokenAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "radiusTokenAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusTokenAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurer {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(RadiusTokenCredential.class);
    }
}
