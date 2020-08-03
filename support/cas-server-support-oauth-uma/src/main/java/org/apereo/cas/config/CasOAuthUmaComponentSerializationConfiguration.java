package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.uma.ticket.permission.DefaultUmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthUmaComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casOAuthUmaComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthUmaComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "umaComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer umaComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(DefaultUmaPermissionTicket.class);
            plan.registerSerializableClass(ResourceSet.class);
            plan.registerSerializableClass(ResourceSetPolicy.class);
            plan.registerSerializableClass(ResourceSetPolicyPermission.class);
        };
    }
}
