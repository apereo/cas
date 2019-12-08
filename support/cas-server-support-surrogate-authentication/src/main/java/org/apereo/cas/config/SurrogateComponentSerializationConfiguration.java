package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.ticket.expiration.SurrogateSessionExpirationPolicy;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "surrogateComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateComponentSerializationConfiguration implements ComponentSerializationPlanConfigurer {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(SurrogateSessionExpirationPolicy.class);
        plan.registerSerializableClass(SurrogateRegisteredServiceAccessStrategy.class);
    }
}
