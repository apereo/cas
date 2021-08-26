package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIAction;
import org.apereo.cas.support.saml.web.flow.SamlIdPWebflowConfigurer;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPMultifactorAuthenticationTrigger;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPSingleSignOnParticipationStrategy;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.login.SessionStoreTicketGrantingTicketAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Objects;

/**
 * This is {@link SamlIdPWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("samlIdPWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPWebflowConfiguration {
    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> selectionStrategies;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier(DistributedJEESessionStore.DEFAULT_BEAN_NAME)
    private ObjectProvider<SessionStore> samlIdPDistributedSessionStore;
    
    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

    @ConditionalOnMissingBean(name = "samlIdPWebConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer samlIdPWebConfigurer() {
        return new SamlIdPWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext,
            casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPSessionStoreTicketGrantingTicketAction")
    public Action samlIdPSessionStoreTicketGrantingTicketAction() {
        return new SessionStoreTicketGrantingTicketAction(samlIdPDistributedSessionStore.getObject());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataUIParserAction")
    @Bean
    @RefreshScope
    public Action samlIdPMetadataUIParserAction() {
        return new SamlIdPMetadataUIAction(servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            selectionStrategies.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "samlIdPCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer samlIdPCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(samlIdPWebConfigurer());
    }


    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy() {
        return new SamlIdPSingleSignOnParticipationStrategy(servicesManager.getObject(),
            ticketRegistrySupport.getObject(), selectionStrategies.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope
    public SingleSignOnParticipationStrategyConfigurer samlIdPSingleSignOnParticipationStrategyConfigurer() {
        return chain -> chain.addStrategy(samlIdPSingleSignOnParticipationStrategy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMultifactorAuthenticationTrigger")
    public MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger() {
        return new SamlIdPMultifactorAuthenticationTrigger(openSamlConfigBean.getObject(),
            samlIdPDistributedSessionStore.getObject(), applicationContext, casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "samlIdPAuthenticationContextWebflowEventResolver")
    public CasWebflowEventResolver samlIdPAuthenticationContextWebflowEventResolver() {
        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(), samlIdPMultifactorAuthenticationTrigger());
        Objects.requireNonNull(initialAuthenticationAttemptWebflowEventResolver.getObject()).addDelegate(r);
        return r;
    }

    @Configuration(value = "SamlIdPConsentWebflowConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(value = ConsentableAttributeBuilder.class)
    public class SamlIdPConsentWebflowConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPConsentableAttributeBuilder")
        @RefreshScope
        public ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder() {
            return attribute -> {
                val result = attributeDefinitionStore.getObject()
                    .locateAttributeDefinition(defn -> {
                        if (defn instanceof SamlIdPAttributeDefinition) {
                            val samlAttr = SamlIdPAttributeDefinition.class.cast(defn);
                            return samlAttr.getName().equalsIgnoreCase(attribute.getName())
                                && StringUtils.isNotBlank(samlAttr.getFriendlyName());
                        }
                        return false;
                    });

                if (result.isPresent()) {
                    val samlAttr = SamlIdPAttributeDefinition.class.cast(result.get());
                    attribute.setFriendlyName(samlAttr.getFriendlyName());
                }
                return attribute;
            };
        }
    }
}
