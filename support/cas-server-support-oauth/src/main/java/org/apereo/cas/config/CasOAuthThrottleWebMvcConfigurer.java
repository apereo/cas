package org.apereo.cas.config;

import org.apereo.cas.web.support.AuthenticationThrottlingExecutionPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

/**
 * This is {@link CasOAuthThrottleWebMvcConfigurer}.
 *
 * @author sbearcsiro
 * @since 5.3.0
 */
@Configuration
public class CasOAuthThrottleWebMvcConfigurer extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors().forEach(handler ->
                registry.addInterceptor(handler).addPathPatterns(BASE_OAUTH20_URL.concat("/").concat("*")));
    }
}
