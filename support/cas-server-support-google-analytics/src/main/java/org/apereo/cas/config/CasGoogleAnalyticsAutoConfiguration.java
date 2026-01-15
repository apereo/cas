package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasGoogleAnalyticsWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CreateGoogleAnalyticsCookieAction;
import org.apereo.cas.web.flow.RemoveGoogleAnalyticsCookieAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * This is {@link CasGoogleAnalyticsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Metrics)
@AutoConfiguration
public class CasGoogleAnalyticsAutoConfiguration {

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsCookieGenerator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasCookieBuilder casGoogleAnalyticsCookieGenerator(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getGoogleAnalytics().getCookie();
        return new CookieRetrievingCookieGenerator(
            CookieUtils.buildCookieGenerationContext(props),
            new NoOpCookieValueManager(tenantExtractor));
    }

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer casGoogleAnalyticsWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new CasGoogleAnalyticsWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_CREATE_COOKIE)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action createGoogleAnalyticsCookieAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("casGoogleAnalyticsCookieGenerator")
        final CasCookieBuilder casGoogleAnalyticsCookieGenerator) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new CreateGoogleAnalyticsCookieAction(casProperties, casGoogleAnalyticsCookieGenerator))
            .withId(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_CREATE_COOKIE)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_REMOVE_COOKIE)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action removeGoogleAnalyticsCookieAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("casGoogleAnalyticsCookieGenerator")
        final CasCookieBuilder casGoogleAnalyticsCookieGenerator) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new RemoveGoogleAnalyticsCookieAction(casGoogleAnalyticsCookieGenerator))
            .withId(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_REMOVE_COOKIE)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsWebflowExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer casGoogleAnalyticsWebflowExecutionPlanConfigurer(
        @Qualifier("casGoogleAnalyticsWebflowConfigurer")
        final CasWebflowConfigurer casGoogleAnalyticsWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(casGoogleAnalyticsWebflowConfigurer);
    }
}
