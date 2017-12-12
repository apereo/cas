package org.apereo.cas.web.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AzureActiveDirectoryAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryDelegationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasAzureActiveDirectoryAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casAzureActiveDirectoryAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAzureActiveDirectoryAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "azureActiveDirectoryPrincipalFactory")
    @Bean
    public PrincipalFactory azureActiveDirectoryPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "azureActiveDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer azureActiveDirectoryAuthenticationEventExecutionPlanConfigurer() {
        final AzureActiveDirectoryDelegationProperties azure = casProperties.getAuthn().getAzureAd();
        final AuthenticationHandler handler =
            new AzureActiveDirectoryAuthenticationHandler(azure.getName(), servicesManager, azureActiveDirectoryPrincipalFactory());
        return plan -> plan.registerAuthenticationHandler(handler);
    }
}
