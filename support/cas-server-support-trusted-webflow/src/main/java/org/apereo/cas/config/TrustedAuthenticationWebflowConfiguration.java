package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurer;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link TrustedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "trusted")
@AutoConfiguration
public class TrustedAuthenticationWebflowConfiguration {

    @Configuration(value = "TrustedAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TrustedAuthenticationWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "trustedWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer trustedWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new TrustedAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "casRequestHeaderAuthenticationFilter")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<RequestHeaderAuthenticationFilter> casRequestHeaderAuthenticationFilter(
            final CasConfigurationProperties casProperties) {
            val filter = new RequestHeaderAuthenticationFilter();
            filter.setPrincipalRequestHeader("REMOTE_USER");
            filter.setExceptionIfHeaderMissing(false);
            filter.setRequiresAuthenticationRequestMatcher(RegexRequestMatcher.regexMatcher(".+"));
            filter.setAuthenticationManager(authentication -> authentication);
            val bean = new FilterRegistrationBean<>(filter);
            bean.setName("RequestHeaderAuthenticationFilter");
            bean.setAsyncSupported(true);
            bean.setOrder(0);
            bean.setEnabled(true);
            return bean;
        }
    }

    @Configuration(value = "TrustedAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TrustedAuthenticationWebflowPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "trustedCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer trustedCasWebflowExecutionPlanConfigurer(
            @Qualifier("trustedWebflowConfigurer")
            final CasWebflowConfigurer trustedWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(trustedWebflowConfigurer);
        }
    }
}
