package org.apereo.cas.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.uma.ticket.permission.DefaultUmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthUmaComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casOAuthUmaComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthUmaComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DefaultUmaPermissionTicket.class);
        plan.registerSerializableClass(ResourceSet.class);
        plan.registerSerializableClass(ResourceSetPolicy.class);
        plan.registerSerializableClass(ResourceSetPolicyPermission.class);
    }
}
