package org.apereo.cas.oidc.config;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This is {@link OidcThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "oidcThrottleConfiguration", proxyBeanMethods = false)
public class OidcThrottleConfiguration implements WebMvcConfigurer {

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors().forEach(handler -> {
            val baseUrl = OidcConstants.BASE_OIDC_URL.concat("/");
            registry.addInterceptor(handler)
                .addPathPatterns(baseUrl.concat(OidcConstants.INTROSPECTION_URL).concat("*"))
                .addPathPatterns(baseUrl.concat(OidcConstants.JWKS_URL).concat("*"))
                .addPathPatterns(baseUrl.concat(OidcConstants.REGISTRATION_URL).concat("*"))
                .addPathPatterns(baseUrl.concat(OidcConstants.REVOCATION_URL).concat("*"))
                .addPathPatterns(baseUrl.concat(OidcConstants.CLIENT_CONFIGURATION_URL).concat("*"));
        });
    }

}
