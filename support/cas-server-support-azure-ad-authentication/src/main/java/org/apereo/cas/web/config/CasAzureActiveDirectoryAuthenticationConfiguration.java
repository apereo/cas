package org.apereo.cas.web.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryDelegationProperties;
import org.apereo.cas.web.flow.AzureActiveDirectoryAuthenticationAction;
import org.apereo.cas.web.flow.AzureActiveDirectoryAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasAzureActiveDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casAzureActiveDirectoryAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAzureActiveDirectoryAuthenticationConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Bean
    public Action azureActiveDirectoryAuthenticationAction() {
        final AzureActiveDirectoryDelegationProperties azure = casProperties.getAuthn().getAzureAd();
        if (StringUtils.isBlank(azure.getTenant()) || StringUtils.isBlank(azure.getClientId()) || StringUtils.isBlank(azure.getClientSecret())) {
            throw new BeanCreationException("No tenant, client id or client secret is defined for Azure Active Directory authentication.");
        }
        return new AzureActiveDirectoryAuthenticationAction(casProperties);
    }

    @ConditionalOnMissingBean(name = "azureActiveDirectoryAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer azureActiveDirectoryAuthenticationWebflowConfigurer() {
        final CasWebflowConfigurer w = new AzureActiveDirectoryAuthenticationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @ConditionalOnMissingBean(name = "azureActiveDirectoryPrincipalFactory")
    @Bean
    public PrincipalFactory azureActiveDirectoryPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
}
