package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.pac4j.web.DelegatedClientOidcBuilder;
import org.apereo.cas.pac4j.web.DelegatedClientOidcSessionManager;
import org.apereo.cas.pac4j.web.DelegatedClientsOidcEndpointContributor;
import org.apereo.cas.pac4j.web.flow.DelegatedAuthenticationOidcWebflowConfigurer;
import org.apereo.cas.pac4j.web.flow.DelegatedClientOidcLogoutAction;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DelegatedAuthenticationOidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "oidc")
@Configuration(value = "DelegatedAuthenticationOidcConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedAuthenticationOidcConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedClientsOidcEndpointContributor")
    public DelegatedClientsEndpointContributor delegatedClientsOidcEndpointContributor() {
        return new DelegatedClientsOidcEndpointContributor();
    }


    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedOidcClientBuilder")
    public ConfigurableDelegatedClientBuilder delegatedOidcClientBuilder(
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        return new DelegatedClientOidcBuilder(casSslContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedClientOidcSessionManager")
    public DelegatedClientSessionManager delegatedClientOidcSessionManager(
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
        final ObjectProvider<@NonNull DelegatedClientAuthenticationConfigurationContext> contextProvider) {
        return new DelegatedClientOidcSessionManager(contextProvider);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_OIDC_CLIENT_LOGOUT)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action delegatedClientOidcLogoutAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
        final SingleLogoutRequestExecutor singleLogoutRequestExecutor) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new DelegatedClientOidcLogoutAction(ticketRegistry, singleLogoutRequestExecutor))
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_OIDC_CLIENT_LOGOUT)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationOidcWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer delegatedAuthenticationOidcWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new DelegatedAuthenticationOidcWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
            applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedAuthenticationOidcWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer delegatedAuthenticationOidcWebflowExecutionPlanConfigurer(
        @Qualifier("delegatedAuthenticationOidcWebflowConfigurer")
        final CasWebflowConfigurer delegatedAuthenticationSaml2WebflowConfigurer,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
            .alwaysMatch()
            .supply(() -> plan -> plan.registerWebflowConfigurer(delegatedAuthenticationSaml2WebflowConfigurer))
            .otherwiseProxy()
            .get();
    }

}
