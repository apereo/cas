package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationPreProcessor;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationWebflowConfigurer;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SurrogateAuthenticationDelegationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.DelegatedAuthentication,
    CasFeatureModule.FeatureCatalog.SurrogateAuthentication
})
@ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
@Configuration(value = "SurrogateAuthenticationDelegationConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationDelegationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogateDelegatedAuthenticationPreProcessor")
    public DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor(
        @Qualifier(SurrogateAuthenticationService.BEAN_NAME) final SurrogateAuthenticationService surrogateAuthenticationService,
        @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME) final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder) {
        return new SurrogateDelegatedAuthenticationPreProcessor(surrogateAuthenticationService, surrogatePrincipalBuilder);
    }

    @ConditionalOnMissingBean(name = "surrogateDelegationWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer surrogateDelegationWebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new SurrogateDelegatedAuthenticationWebflowConfigurer(
            flowBuilderServices, flowDefinitionRegistry,
            applicationContext, casProperties);
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "surrogateDelegationCasWebflowExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer surrogateDelegationCasWebflowExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("surrogateDelegationWebflowConfigurer")
        final CasWebflowConfigurer surrogateDelegationWebflowConfigurer) {
        return plan -> {
            val allow = casProperties.getAuthn().getPac4j().getCore().isAllowImpersonation();
            if (allow) {
                plan.registerWebflowConfigurer(surrogateDelegationWebflowConfigurer);
            }
        };
    }

}
