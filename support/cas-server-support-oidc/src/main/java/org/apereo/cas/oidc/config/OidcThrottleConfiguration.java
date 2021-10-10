package org.apereo.cas.oidc.config;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.ThrottledRequestFilter;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This is {@link OidcThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "oidcThrottleConfiguration", proxyBeanMethods = true)
@AutoConfigureBefore(OidcConfiguration.class)
public class OidcThrottleConfiguration {
    private static final List<String> THROTTLED_ENDPOINTS = List.of(
        OidcConstants.ACCESS_TOKEN_URL,
        OidcConstants.AUTHORIZE_URL,
        OidcConstants.TOKEN_URL,
        OidcConstants.PROFILE_URL,
        OidcConstants.JWKS_URL,
        OidcConstants.CLIENT_CONFIGURATION_URL,
        OidcConstants.REVOCATION_URL,
        OidcConstants.INTROSPECTION_URL);

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Autowired
    @Qualifier("oidcRequestSupport")
    private ObjectProvider<OidcRequestSupport> oidcRequestSupport;

    @Bean
    @ConditionalOnMissingBean(name = "oidcThrottleWebMvcConfigurer")
    public WebMvcConfigurer oidcThrottleWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                val interceptors = authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors();
                interceptors.forEach(handler -> registry.addInterceptor(handler)
                    .order(0)
                    .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL + "/**"));
            }
        };
    }

    @ConditionalOnMissingBean(name = "oidcAuthenticationThrottlingExecutionPlanConfigurer")
    @Bean
    public AuthenticationThrottlingExecutionPlanConfigurer oidcAuthenticationThrottlingExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationThrottleFilter(oidcThrottledRequestFilter());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcThrottledRequestFilter")
    public ThrottledRequestFilter oidcThrottledRequestFilter() {
        return (request, response) -> {
            val webContext = new JEEContext(request, response);
            return THROTTLED_ENDPOINTS
                .stream()
                .anyMatch(endpoint -> oidcRequestSupport.getObject().isValidIssuerForEndpoint(webContext, endpoint));
        };
    }
}
