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
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.RemoteAddressWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
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
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasRemoteAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casRemoteAuthenticationConfiguration", proxyBeanMethods = false)
public class CasRemoteAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "remoteAddressWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer remoteAddressWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new RemoteAddressWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "remoteAddressAuthenticationHandler")
    @Autowired
    public AuthenticationHandler remoteAddressAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                                    @Qualifier("remoteAddressPrincipalFactory")
                                                                    final PrincipalFactory remoteAddressPrincipalFactory,
                                                                    @Qualifier(ServicesManager.BEAN_NAME)
                                                                    final ServicesManager servicesManager) {
        val remoteAddress = casProperties.getAuthn().getRemoteAddress();
        val bean = new RemoteAddressAuthenticationHandler(remoteAddress.getName(), servicesManager, remoteAddressPrincipalFactory, remoteAddress.getOrder());
        bean.configureIpNetworkRange(remoteAddress.getIpAddressRange());
        return bean;
    }

    @Bean
    @ConditionalOnMissingBean(name = "remoteAddressCheck")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action remoteAddressCheck(
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return new RemoteAddressNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @ConditionalOnMissingBean(name = "remoteAddressPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory remoteAddressPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "remoteAddressAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer remoteAddressAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("remoteAddressAuthenticationHandler")
        final AuthenticationHandler remoteAddressAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(remoteAddressAuthenticationHandler, defaultPrincipalResolver);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "remoteCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer remoteCasWebflowExecutionPlanConfigurer(
        @Qualifier("remoteAddressWebflowConfigurer")
        final CasWebflowConfigurer remoteAddressWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(remoteAddressWebflowConfigurer);
    }
}
