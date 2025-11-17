package org.apereo.cas.config;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
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
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasTrustedAuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "trusted")
@AutoConfiguration
public class CasTrustedAuthenticationWebflowAutoConfiguration {

    @Configuration(value = "TrustedAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationWebflowBaseConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "trustedAuthnComponentSerializationPlanConfigurer")
        public ComponentSerializationPlanConfigurer trustedAuthnComponentSerializationPlanConfigurer() {
            return plan -> plan.registerSerializableClass(PrincipalBearingCredential.class);
        }

        @ConditionalOnMissingBean(name = "trustedWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer trustedWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new TrustedAuthenticationWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "casRequestHeaderAuthenticationFilter")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<@NonNull RequestHeaderAuthenticationFilter> casRequestHeaderAuthenticationFilter(
            final CasConfigurationProperties casProperties) {
            val valve = casProperties.getServer().getTomcat().getRemoteUserValve();
            val filter = new RequestHeaderAuthenticationFilter();
            filter.setPrincipalRequestHeader(StringUtils.defaultIfBlank(valve.getRemoteUserHeader(), "NA"));
            filter.setExceptionIfHeaderMissing(false);
            filter.setRequiresAuthenticationRequestMatcher(request -> RegexUtils.matchesIpAddress(valve.getAllowedIpAddressRegex(), request.getRemoteAddr()));
            filter.setAuthenticationManager(authentication -> {
                authentication.setAuthenticated(authentication.getPrincipal() != null);
                return authentication;
            });
            val bean = new FilterRegistrationBean<>(filter);
            bean.setName("casRequestHeaderAuthenticationFilter");
            bean.setAsyncSupported(true);
            bean.setOrder(0);
            bean.setEnabled(StringUtils.isNotBlank(valve.getRemoteUserHeader()));
            return bean;
        }
    }

    @Configuration(value = "TrustedAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationWebflowPlanConfiguration {
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
