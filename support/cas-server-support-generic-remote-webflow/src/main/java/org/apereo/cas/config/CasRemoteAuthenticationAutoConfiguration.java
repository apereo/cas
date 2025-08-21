package org.apereo.cas.config;

import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAuthenticationCookieCipherExecutor;
import org.apereo.cas.adaptors.generic.remote.RemoteAuthenticationCredential;
import org.apereo.cas.adaptors.generic.remote.RemoteCookieAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.RemoteAuthenticationNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.RemoteAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link CasRemoteAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "remote")
@AutoConfiguration
public class CasRemoteAuthenticationAutoConfiguration {

    @Configuration(value = "RemoteAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    static class RemoteAuthenticationWebflowConfiguration {
        @ConditionalOnMissingBean(name = "remoteAuthenticationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer remoteAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new RemoteAuthenticationWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action remoteAuthenticationCheck(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RemoteAuthenticationNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                    serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, casProperties.getAuthn().getRemote()))
                .withId(CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
                .build()
                .get();
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

    @Configuration(value = "RemoteAddressAuthenticationConfiguration", proxyBeanMethods = false)
    static class RemoteAddressAuthenticationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "remoteAuthnComponentSerializationPlanConfigurer")
        public ComponentSerializationPlanConfigurer remoteAuthnComponentSerializationPlanConfigurer() {
            return plan -> plan.registerSerializableClass(RemoteAuthenticationCredential.class);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "remoteAddressAuthenticationHandler")
        public AuthenticationHandler remoteAddressAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("remoteAddressPrincipalFactory")
            final PrincipalFactory remoteAddressPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(BeanCondition.on("cas.authn.remote.ip-address-range")
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val remoteAddress = casProperties.getAuthn().getRemote();
                    val bean = new RemoteAddressAuthenticationHandler(remoteAddress,
                        remoteAddressPrincipalFactory);
                    bean.configureIpNetworkRange(remoteAddress.getIpAddressRange());
                    return bean;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "remoteCookieAuthenticationHandler")
        public AuthenticationHandler remoteCookieAuthenticationHandler(
            @Qualifier("remoteCookieCipherExecutor")
            final CipherExecutor remoteCookieCipherExecutor,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("remoteAddressPrincipalFactory")
            final PrincipalFactory remoteAddressPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(BeanCondition.on("cas.authn.remote.cookie.cookie-name")
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val remote = casProperties.getAuthn().getRemote();
                    return new RemoteCookieAuthenticationHandler(remote,
                        remoteAddressPrincipalFactory, remoteCookieCipherExecutor);
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "remoteCookieCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor remoteCookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getRemote().getCookie().getCrypto();
            if (crypto.isEnabled() || (StringUtils.isNotBlank(crypto.getEncryption().getKey())
                && StringUtils.isNotBlank(crypto.getSigning().getKey()))) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, RemoteAuthenticationCookieCipherExecutor.class);
            }
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = "remoteAddressPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory remoteAddressPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "RemoteAuthenticationCoreConfiguration", proxyBeanMethods = false)
    static class RemoteAuthenticationCoreConfiguration {
        @ConditionalOnMissingBean(name = "remoteAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer remoteAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("remoteAddressAuthenticationHandler")
            final AuthenticationHandler remoteAddressAuthenticationHandler,
            @Qualifier("remoteCookieAuthenticationHandler")
            final AuthenticationHandler remoteCookieAuthenticationHandler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> {
                plan.registerAuthenticationHandlerWithPrincipalResolver(
                    remoteAddressAuthenticationHandler, defaultPrincipalResolver);
                plan.registerAuthenticationHandlerWithPrincipalResolver(
                    remoteCookieAuthenticationHandler, defaultPrincipalResolver);
            };
        }
    }

}
