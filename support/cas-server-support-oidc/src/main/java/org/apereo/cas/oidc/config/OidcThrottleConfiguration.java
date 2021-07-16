package org.apereo.cas.oidc.config;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
@AutoConfigureBefore(OidcConfiguration.class)
public class OidcThrottleConfiguration implements WebMvcConfigurer {

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        val interceptors = authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors();
        interceptors.forEach(handler -> registry.addInterceptor(handler)
            .order(0)
            .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL + "/**"));
    }

}
