package org.apereo.cas.oidc.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.ThrottledRequestFilter;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This is {@link OidcThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "oidcThrottleConfiguration", proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class OidcThrottleConfiguration {

    @Configuration(value = "OidcThrottleWebMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcThrottleWebMvcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcThrottleWebMvcConfigurer")
        @Autowired
        public WebMvcConfigurer oidcThrottleWebMvcConfigurer(
            @Qualifier("authenticationThrottlingExecutionPlan")
            final AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(final InterceptorRegistry registry) {
                    val interceptors = authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors();
                    interceptors.forEach(handler -> registry.addInterceptor(handler)
                        .order(0)
                        .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL + "/**"));
                }
            };
        }

    }

    @Configuration(value = "OidcThrottleExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcThrottleExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "oidcAuthenticationThrottlingExecutionPlanConfigurer")
        @Bean
        @Autowired
        public AuthenticationThrottlingExecutionPlanConfigurer oidcAuthenticationThrottlingExecutionPlanConfigurer(
            @Qualifier("oidcThrottledRequestFilter")
            final ThrottledRequestFilter oidcThrottledRequestFilter) {
            return plan -> plan.registerAuthenticationThrottleFilter(oidcThrottledRequestFilter);
        }

    }

    @Configuration(value = "OidcThrottleFilterConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcThrottleFilterConfiguration {
        private static final List<String> THROTTLED_ENDPOINTS = List.of(
            OidcConstants.ACCESS_TOKEN_URL,
            OidcConstants.AUTHORIZE_URL,
            OidcConstants.TOKEN_URL,
            OidcConstants.PROFILE_URL,
            OidcConstants.JWKS_URL,
            OidcConstants.CLIENT_CONFIGURATION_URL,
            OidcConstants.REVOCATION_URL,
            OidcConstants.INTROSPECTION_URL);

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "oidcThrottledRequestFilter")
        public ThrottledRequestFilter oidcThrottledRequestFilter(
            @Qualifier("oidcRequestSupport")
            final OidcRequestSupport oidcRequestSupport) {
            return (request, response) -> {
                val webContext = new JEEContext(request, response);
                return THROTTLED_ENDPOINTS
                    .stream()
                    .anyMatch(endpoint -> oidcRequestSupport.isValidIssuerForEndpoint(webContext, endpoint));
            };
        }
    }
}
