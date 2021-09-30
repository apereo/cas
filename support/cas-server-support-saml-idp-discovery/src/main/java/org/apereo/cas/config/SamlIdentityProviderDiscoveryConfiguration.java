package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.SamlIdentityProviderDiscoveryFeedController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SamlIdentityProviderDiscoveryWebflowConfigurer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is {@link SamlIdentityProviderDiscoveryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "samlIdentityProviderDiscoveryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdentityProviderDiscoveryConfiguration {

    @ConditionalOnMissingBean(name = "identityProviderDiscoveryWebflowConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CasWebflowConfigurer identityProviderDiscoveryWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new SamlIdentityProviderDiscoveryWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer identityProviderDiscoveryCasWebflowExecutionPlanConfigurer(
        @Qualifier("identityProviderDiscoveryWebflowConfigurer")
        final CasWebflowConfigurer identityProviderDiscoveryWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(identityProviderDiscoveryWebflowConfigurer);
    }

    @Bean
    @Autowired
    public SamlIdentityProviderDiscoveryFeedController identityProviderDiscoveryFeedController(
        @Qualifier("samlIdentityProviderEntityParser")
        final Supplier<List<SamlIdentityProviderEntityParser>> samlIdentityProviderEntityParser, final CasConfigurationProperties casProperties,
        @Qualifier("builtClients")
        final Clients builtClients,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
        final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
        @Qualifier("argumentExtractor")
        final ArgumentExtractor argumentExtractor) {
        return new SamlIdentityProviderDiscoveryFeedController(casProperties, samlIdentityProviderEntityParser.get(), builtClients,
            new DelegatedAuthenticationAccessStrategyHelper(servicesManager, registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer), argumentExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdentityProviderEntityParser")
    @Autowired
    public Supplier<List<SamlIdentityProviderEntityParser>> samlIdentityProviderEntityParser(final CasConfigurationProperties casProperties,
                                                                                             @Qualifier("builtClients")
                                                                                             final Clients builtClients) {
        val parsers = new ArrayList<SamlIdentityProviderEntityParser>();
        val resource = casProperties.getAuthn().getPac4j().getSamlDiscovery().getResource();
        resource.stream().filter(res -> res.getLocation() != null).forEach(Unchecked.consumer(res -> parsers.add(new SamlIdentityProviderEntityParser(res.getLocation()))));
        builtClients.findAllClients().stream().filter(c -> c instanceof SAML2Client).map(SAML2Client.class::cast).forEach(c -> {
            c.init();
            val entity = new SamlIdentityProviderEntity();
            entity.setEntityID(c.getIdentityProviderResolvedEntityId());
            parsers.add(new SamlIdentityProviderEntityParser(entity));
        });
        return () -> parsers;
    }
}
