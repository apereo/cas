package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.ws.idp.services.CustomNamespaceWSFederationClaimsReleasePolicy;
import org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicy;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CoreWsSecurityIdentityProviderComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CoreWsSecurityIdentityProviderComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreWsSecurityIdentityProviderComponentSerializationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer wsFederationComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(WSFederationRegisteredService.class);
            plan.registerSerializableClass(CustomNamespaceWSFederationClaimsReleasePolicy.class);
            plan.registerSerializableClass(WSFederationClaimsReleasePolicy.class);
        };
    }
}
