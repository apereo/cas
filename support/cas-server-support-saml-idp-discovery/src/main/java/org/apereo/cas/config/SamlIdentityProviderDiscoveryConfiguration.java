package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.SamlIdentityProviderDiscoveryFeedController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SamlIdentityProviderDiscoveryWebflowConfigurer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdentityProviderDiscoveryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "samlIdentityProviderDiscoveryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdentityProviderDiscoveryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("builtClients")
    private ObjectProvider<Clients> builtClients;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private ObjectProvider<SessionStore> delegatedClientDistributedSessionStore;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;
    
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryWebflowConfigurer")
    @RefreshScope
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer identityProviderDiscoveryWebflowConfigurer() {
        return new SamlIdentityProviderDiscoveryWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer identityProviderDiscoveryCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(identityProviderDiscoveryWebflowConfigurer());
    }

    @Bean
    public SamlIdentityProviderDiscoveryFeedController identityProviderDiscoveryFeedController() {
        return new SamlIdentityProviderDiscoveryFeedController(casProperties, samlIdentityProviderEntityParser(),
            builtClients.getObject(),
            new DelegatedAuthenticationAccessStrategyHelper(servicesManager.getObject(),
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer.getObject()),
            argumentExtractor.getObject(),
            delegatedClientDistributedSessionStore.getObject()
        );
    }

    @Bean
    public List<SamlIdentityProviderEntityParser> samlIdentityProviderEntityParser() {
        val parsers = new ArrayList<SamlIdentityProviderEntityParser>();

        val resource = casProperties.getAuthn().getPac4j().getSamlDiscovery().getResource();
        resource
            .stream()
            .filter(res -> res.getLocation() != null)
            .forEach(Unchecked.consumer(res -> parsers.add(new SamlIdentityProviderEntityParser(res.getLocation()))));

        builtClients.getObject().findAllClients()
            .stream()
            .filter(c -> c instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .forEach(c -> {
                c.init();
                val entity = new SamlIdentityProviderEntity();
                entity.setEntityID(c.getIdentityProviderResolvedEntityId());
                parsers.add(new SamlIdentityProviderEntityParser(entity));
            });
        return parsers;
    }
}
