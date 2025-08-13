package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * This is {@link OidcThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.OpenIDConnect,
    CasFeatureModule.FeatureCatalog.Throttling
})
@Configuration(value = "OidcThrottleConfiguration", proxyBeanMethods = false)
class OidcThrottleConfiguration {

    @Configuration(value = "OidcThrottleWebMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcThrottleWebMvcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcThrottleWebMvcConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebMvcConfigurer oidcThrottleWebMvcConfigurer(
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
                    authenticationThrottlingExecutionPlan.ifAvailable(plan -> {
                        val handler = new RefreshableHandlerInterceptor(plan::getAuthenticationThrottleInterceptors);
                        registry.addInterceptor(handler).order(0).addPathPatterns('/' + OidcConstants.BASE_OIDC_URL + "/**");
                    });
                }
            };
        }

    }

    @Configuration(value = "OidcThrottleExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcThrottleExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "oidcAuthenticationThrottlingExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationThrottlingExecutionPlanConfigurer oidcAuthenticationThrottlingExecutionPlanConfigurer(
            @Qualifier("oidcThrottledRequestFilter")
            final ThrottledRequestFilter oidcThrottledRequestFilter) {
            return plan -> plan.registerAuthenticationThrottleFilter(oidcThrottledRequestFilter);
        }

    }

    @Configuration(value = "OidcThrottleFilterConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcThrottleFilterConfiguration {
        private static final List<String> THROTTLED_ENDPOINTS = List.of(
            OidcConstants.AUTHORIZE_URL,
            OidcConstants.ACCESS_TOKEN_URL,
            OidcConstants.TOKEN_URL,
            OidcConstants.PROFILE_URL,
            OidcConstants.JWKS_URL,
            OidcConstants.CLIENT_CONFIGURATION_URL,
            OidcConstants.REVOCATION_URL,
            OidcConstants.INTROSPECTION_URL);

        @Bean
        @ConditionalOnMissingBean(name = "oidcThrottledRequestFilter")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ThrottledRequestFilter oidcThrottledRequestFilter() {
            return (request, response) -> {
                val webContext = new JEEContext(request, response);
                val url = webContext.getRequestURL();
                return THROTTLED_ENDPOINTS.stream().anyMatch(endpoint -> url.endsWith(endpoint));
            };
        }
    }
}
