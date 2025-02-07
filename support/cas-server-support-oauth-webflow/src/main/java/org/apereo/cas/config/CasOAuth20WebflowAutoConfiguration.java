package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.flow.OAuth20RegisteredServiceUIAction;
import org.apereo.cas.support.oauth.web.flow.OAuth20WebflowConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.View;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasOAuth20WebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@ImportAutoConfiguration(CasOAuth20AutoConfiguration.class)
@AutoConfiguration
public class CasOAuth20WebflowAutoConfiguration {

    @Configuration(value = "CasOAuth20WebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20WebflowActionConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_OAUTH20_REGISTERED_SERVICE_UI)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action oauth20RegisteredServiceUIAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy oauth20AuthenticationServiceSelectionStrategy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new OAuth20RegisteredServiceUIAction(servicesManager, oauth20AuthenticationServiceSelectionStrategy))
                .withId(CasWebflowConstants.ACTION_ID_OAUTH20_REGISTERED_SERVICE_UI)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasOAuth20ViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20ViewsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View oauthConfirmView(
            @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oauth/confirm");
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View oauthDeviceCodeApprovalView(
            @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oauth/deviceCodeApproval");
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View oauthDeviceCodeApprovedView(
            @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oauth/deviceCodeApproved");
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View oauthSessionStaleMismatchErrorView(
            @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oauth/sessionStaleMismatchError");
        }
    }

    @Configuration(value = "CasOAuth20WebflowLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20WebflowLogoutConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauth20CasLogoutWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer oauth20CasLogoutWebflowExecutionPlanConfigurer(
            @Qualifier("oauth20LogoutWebflowConfigurer")
            final CasWebflowConfigurer oauth20LogoutWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(oauth20LogoutWebflowConfigurer);
        }

        @ConditionalOnMissingBean(name = "oauth20LogoutWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer oauth20LogoutWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new OAuth20WebflowConfigurer(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        }
    }
}
