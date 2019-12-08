package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreTicketComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCoreComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreTicketComponentSerializationConfiguration implements ComponentSerializationPlanConfigurer {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(RememberMeDelegatingExpirationPolicy.class);
    }
}
