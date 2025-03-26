package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedAuthenticationSaml2WebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientFinishLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientTerminateSessionAction;
import org.apereo.cas.web.saml2.DelegatedClientSaml2Builder;
import org.apereo.cas.web.saml2.DelegatedClientSaml2SessionManager;
import org.apereo.cas.web.saml2.DelegatedClientsSaml2EndpointContributor;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataController;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
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
import java.util.List;

/**
 * This is {@link DelegatedAuthenticationSaml2Configuration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml")
@Configuration(value = "DelegatedAuthenticationSaml2Configuration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedAuthenticationSaml2Configuration {

    @Configuration(value = "DelegatedAuthenticationSAMLWebConfiguration", proxyBeanMethods = false)
    static class DelegatedAuthenticationSAMLWebConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedSaml2ClientTerminateSessionAction(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(Action.class)
                .when(BeanCondition.on("cas.slo.disabled").isFalse().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> WebflowActionBeanSupplier.builder()
                    .withApplicationContext(applicationContext)
                    .withProperties(casProperties)
                    .withAction(() -> new DelegatedSaml2ClientTerminateSessionAction(identityProviders, delegatedClientDistributedSessionStore))
                    .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION)
                    .build()
                    .get())
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }


        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedSaml2ClientLogoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
            final SingleLogoutRequestExecutor singleLogoutRequestExecutor) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedSaml2ClientLogoutAction(ticketRegistry, singleLogoutRequestExecutor))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationSaml2ClientFinishLogoutAction(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean configBean,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory serviceFactory,
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedSaml2ClientFinishLogoutAction(identityProviders,
                    delegatedClientDistributedSessionStore, configBean,
                    ticketRegistry, ticketFactory, serviceFactory))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = "delegatedAuthenticationSaml2WebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer delegatedAuthenticationSaml2WebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new DelegatedAuthenticationSaml2WebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
                applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationSaml2WebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer delegatedAuthenticationSaml2WebflowExecutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationSaml2WebflowConfigurer")
            final CasWebflowConfigurer delegatedAuthenticationSaml2WebflowConfigurer,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .alwaysMatch()
                .supply(() -> plan -> plan.registerWebflowConfigurer(delegatedAuthenticationSaml2WebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientSaml2EndpointConfigurer")
        public CasWebSecurityConfigurer<Void> delegatedClientSaml2EndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER, "/"));
                }
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientMetadataController")
        public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean configBean) {
            return new DelegatedSaml2ClientMetadataController(identityProviders, configBean);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientsSaml2EndpointContributor")
        public DelegatedClientsEndpointContributor delegatedClientsSaml2EndpointContributor() {
            return new DelegatedClientsSaml2EndpointContributor();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedSaml2ClientBuilder")
        public ConfigurableDelegatedClientBuilder delegatedSaml2ClientBuilder(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean configBean,
            @Qualifier(DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
            final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            return new DelegatedClientSaml2Builder(casSslContext, samlMessageStoreFactory, configBean);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientSaml2SessionManager")
        public DelegatedClientSessionManager delegatedClientSaml2SessionManager(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final ObjectProvider<DelegatedClientAuthenticationConfigurationContext> contextProvider) {
            return new DelegatedClientSaml2SessionManager(contextProvider);
        }
    }
}
