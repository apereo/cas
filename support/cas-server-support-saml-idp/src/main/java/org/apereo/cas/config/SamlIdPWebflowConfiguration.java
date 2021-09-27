package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
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
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.io.Serializable;
import java.util.Objects;

/**
 * This is {@link SamlIdPWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPWebflowConfiguration", proxyBeanMethods = false)
public class SamlIdPWebflowConfiguration {

    @ConditionalOnMissingBean(name = "samlIdPWebConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer samlIdPWebConfigurer(final CasConfigurationProperties casProperties,
                                                     final ConfigurableApplicationContext applicationContext,
                                                     @Qualifier("loginFlowDefinitionRegistry")
                                                     final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                     @Qualifier("flowBuilderServices")
                                                     final FlowBuilderServices flowBuilderServices) {
        return new SamlIdPWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPSessionStoreTicketGrantingTicketAction")
    public Action samlIdPSessionStoreTicketGrantingTicketAction(
        @Qualifier("samlIdPDistributedSessionStore")
        final SessionStore samlIdPDistributedSessionStore) {
        return new SessionStoreTicketGrantingTicketAction(samlIdPDistributedSessionStore);
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataUIParserAction")
    @Bean
    @RefreshScope
    public Action samlIdPMetadataUIParserAction(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("selectionStrategies")
        final AuthenticationServiceSelectionPlan selectionStrategies,
        @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
        final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
        return new SamlIdPMetadataUIAction(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver, selectionStrategies);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "samlIdPCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer samlIdPCasWebflowExecutionPlanConfigurer(
        @Qualifier("samlIdPWebConfigurer")
        final CasWebflowConfigurer samlIdPWebConfigurer) {
        return plan -> plan.registerWebflowConfigurer(samlIdPWebConfigurer);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("defaultTicketRegistrySupport")
        final TicketRegistrySupport ticketRegistrySupport,
        @Qualifier("selectionStrategies")
        final AuthenticationServiceSelectionPlan selectionStrategies) {
        return new SamlIdPSingleSignOnParticipationStrategy(servicesManager, ticketRegistrySupport, selectionStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope
    public SingleSignOnParticipationStrategyConfigurer samlIdPSingleSignOnParticipationStrategyConfigurer(
        @Qualifier("samlIdPSingleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy) {
        return chain -> chain.addStrategy(samlIdPSingleSignOnParticipationStrategy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMultifactorAuthenticationTrigger")
    @Autowired
    public MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger(final CasConfigurationProperties casProperties,
                                                                                    final ConfigurableApplicationContext applicationContext,
                                                                                    @Qualifier("openSamlConfigBean")
                                                                                    final OpenSamlConfigBean openSamlConfigBean,
                                                                                    @Qualifier("samlIdPDistributedSessionStore")
                                                                                    final SessionStore samlIdPDistributedSessionStore) {
        return new SamlIdPMultifactorAuthenticationTrigger(openSamlConfigBean, samlIdPDistributedSessionStore, applicationContext, casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "samlIdPAuthenticationContextWebflowEventResolver")
    public CasWebflowEventResolver samlIdPAuthenticationContextWebflowEventResolver(
        @Qualifier("samlIdPMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext, samlIdPMultifactorAuthenticationTrigger);
        Objects.requireNonNull(initialAuthenticationAttemptWebflowEventResolver).addDelegate(r);
        return r;
    }

    @Configuration(value = "SamlIdPConsentWebflowConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(value = ConsentableAttributeBuilder.class)
    public static class SamlIdPConsentWebflowConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPConsentableAttributeBuilder")
        @RefreshScope
        @Autowired
        public ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder(
            @Qualifier("attributeDefinitionStore")
            final AttributeDefinitionStore attributeDefinitionStore) {
            return attribute -> {
                val result = attributeDefinitionStore.locateAttributeDefinition(defn -> {
                    if (defn instanceof SamlIdPAttributeDefinition) {
                        val samlAttr = (SamlIdPAttributeDefinition) defn;
                        return samlAttr.getName().equalsIgnoreCase(attribute.getName()) && StringUtils.isNotBlank(samlAttr.getFriendlyName());
                    }
                    return false;
                });
                if (result.isPresent()) {
                    val samlAttr = (SamlIdPAttributeDefinition) result.get();
                    attribute.setFriendlyName(samlAttr.getFriendlyName());
                }
                attribute.getValues().replaceAll(o -> {
                    if (o instanceof XSString) {
                        return ((XSString) o).getValue();
                    }
                    if (o instanceof XSURI) {
                        return ((XSURI) o).getURI();
                    }
                    if (o instanceof Serializable) {
                        return o;
                    }
                    return o.toString();
                });
                return attribute;
            };
        }
    }
}
