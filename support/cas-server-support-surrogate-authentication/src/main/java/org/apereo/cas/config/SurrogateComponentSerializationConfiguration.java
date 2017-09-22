package org.apereo.cas.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.support.SurrogateSessionExpirationPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("surrogateComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(SurrogateSessionExpirationPolicy.class);
    }
}
