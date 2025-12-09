package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.TokenAuthenticationEndpoint;
import org.apereo.cas.web.TokenRequestExtractor;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.TokenAuthenticationAction;
import org.apereo.cas.web.flow.TokenWebflowConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasTokenAuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "token")
@AutoConfiguration
public class CasTokenAuthenticationWebflowAutoConfiguration {

    @ConditionalOnMissingBean(name = "tokenWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer tokenWebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new TokenWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "tokenRequestExtractor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TokenRequestExtractor tokenRequestExtractor() {
        return TokenRequestExtractor.defaultExtractor();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TOKEN_AUTHENTICATION_ACTION)
    public Action tokenAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("tokenRequestExtractor")
        final TokenRequestExtractor tokenRequestExtractor,
        @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new TokenAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, tokenRequestExtractor,
                servicesManager, webApplicationServiceFactory, authenticationServiceSelectionPlan, casProperties))
            .withId(CasWebflowConstants.ACTION_ID_TOKEN_AUTHENTICATION_ACTION)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "tokenCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer tokenCasWebflowExecutionPlanConfigurer(
        @Qualifier("tokenWebflowConfigurer")
        final CasWebflowConfigurer tokenWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(tokenWebflowConfigurer);
    }


    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TokenAuthenticationEndpoint tokenAuthenticationEndpoint(
        final ObjectProvider<@NonNull CasConfigurationProperties> casProperties,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final ObjectProvider<@NonNull PrincipalResolver> defaultPrincipalResolver,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ObjectProvider<@NonNull ServicesManager> servicesManager,
        @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ObjectProvider<@NonNull ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
        @Qualifier(PrincipalFactory.BEAN_NAME)
        final ObjectProvider<@NonNull PrincipalFactory> principalFactory) {
        return new TokenAuthenticationEndpoint(casProperties, defaultPrincipalResolver,
            servicesManager, registeredServiceAccessStrategyEnforcer,
            webApplicationServiceFactory, principalFactory);
    }
}
