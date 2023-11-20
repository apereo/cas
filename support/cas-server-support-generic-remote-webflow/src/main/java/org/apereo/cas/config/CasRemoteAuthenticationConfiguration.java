package org.apereo.cas.config;

import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.RemoteAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
 * This is {@link CasRemoteAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "remote")
@AutoConfiguration
public class CasRemoteAuthenticationConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class RemoteAuthenticationWebflowConfiguration {
        @ConditionalOnMissingBean(name = "remoteAuthenticationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer remoteAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new RemoteAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action remoteAddressCheck(
            @Qualifier("adaptiveAuthenticationPolicy")
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier("serviceTicketRequestWebflowEventResolver")
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            return new RemoteAddressNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "remoteCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer remoteCasWebflowExecutionPlanConfigurer(
            @Qualifier("remoteAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer remoteAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(remoteAuthenticationWebflowConfigurer);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RemoteAddressAuthenticationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "remoteAddressAuthenticationHandler")
        public AuthenticationHandler remoteAddressAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                                        @Qualifier("remoteAddressPrincipalFactory")
                                                                        final PrincipalFactory remoteAddressPrincipalFactory,
                                                                        @Qualifier(ServicesManager.BEAN_NAME)
                                                                        final ServicesManager servicesManager) {
            val remoteAddress = casProperties.getAuthn().getRemote();
            val bean = new RemoteAddressAuthenticationHandler(remoteAddress.getName(),
                servicesManager, remoteAddressPrincipalFactory, remoteAddress.getOrder());
            bean.configureIpNetworkRange(remoteAddress.getIpAddressRange());
            return bean;
        }


        @ConditionalOnMissingBean(name = "remoteAddressPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory remoteAddressPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RemoteAuthenticationCoreConfiguration {
        @ConditionalOnMissingBean(name = "remoteAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer remoteAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("remoteAddressAuthenticationHandler")
            final AuthenticationHandler remoteAddressAuthenticationHandler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
                remoteAddressAuthenticationHandler, defaultPrincipalResolver);
        }
    }

}
