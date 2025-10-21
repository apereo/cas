package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.WsFederationAction;
import org.apereo.cas.web.flow.WsFederationClientRedirectAction;
import org.apereo.cas.web.flow.WsFederationRequestBuilder;
import org.apereo.cas.web.flow.WsFederationResponseValidator;
import org.apereo.cas.web.flow.WsFederationWebflowConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import java.util.List;

/**
 * This is {@link CasWsFederationAuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederation)
@AutoConfiguration
public class CasWsFederationAuthenticationWebflowAutoConfiguration {

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer wsFederationWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new WsFederationWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WS_FEDERATION_REDIRECT)
    public Action wsFederationClientRedirectAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final ServerProperties serverProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new WsFederationClientRedirectAction(serverProperties))
            .withId(CasWebflowConstants.ACTION_ID_WS_FEDERATION_REDIRECT)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WS_FEDERATION)
    public Action wsFederationAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("wsFederationRequestBuilder")
        final WsFederationRequestBuilder wsFederationRequestBuilder,
        @Qualifier("wsFederationResponseValidator")
        final WsFederationResponseValidator wsFederationResponseValidator,
        @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new WsFederationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, wsFederationRequestBuilder,
                wsFederationResponseValidator))
            .withId(CasWebflowConstants.ACTION_ID_WS_FEDERATION)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationRequestBuilder")
    public WsFederationRequestBuilder wsFederationRequestBuilder(
        @Qualifier("wsFederationConfigurations")
        final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationRequestBuilder(wsFederationConfigurations.toList(), wsFederationHelper);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationResponseValidator")
    public WsFederationResponseValidator wsFederationResponseValidator(
        @Qualifier("wsFederationConfigurations")
        final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
        @Qualifier("wsFederationCookieManager")
        final WsFederationCookieManager wsFederationCookieManager,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationResponseValidator(wsFederationHelper,
            wsFederationConfigurations.toList(), authenticationSystemSupport, wsFederationCookieManager);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer(
        @Qualifier("wsFederationWebflowConfigurer")
        final CasWebflowConfigurer wsFederationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationEndpointConfigurer")
    public CasWebSecurityConfigurer<Void> wsFederationEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(WsFederationNavigationController.ENDPOINT_REDIRECT);
            }
        };
    }
}
