package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.web.support.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.support.AuthenticationThrottlingExecutionPlanConfigurer;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

/**
 * This is {@link CasOAuthThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Configuration("oauthThrottleConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthThrottleConfiguration implements AuthenticationThrottlingExecutionPlanConfigurer {

    @Configuration("oauthThrottleWebMvcConfigurer")
    static class CasOAuthThrottleWebMvcConfigurer extends WebMvcConfigurerAdapter {

        @Autowired
        @Qualifier("authenticationThrottlingExecutionPlan")
        private AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan;

        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors().forEach(handler ->
                    registry.addInterceptor(handler).addPathPatterns(BASE_OAUTH20_URL.concat("/").concat("*")));
        }
    }

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
    @Bean
    public SecurityInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new SecurityInterceptor(oauthSecConfig.getIfAvailable(), Authenticators.CAS_OAUTH_CLIENT);
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
    @Bean
    public SecurityInterceptor requiresAuthenticationAccessTokenInterceptor() {
        final String clients = Stream.of(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
            Authenticators.CAS_OAUTH_CLIENT_USER_FORM).collect(Collectors.joining(","));
        return new SecurityInterceptor(oauthSecConfig.getIfAvailable(), clients);
    }

    @ConditionalOnMissingBean(name = "oauthHandlerInterceptorAdapter")
    @Bean
    public HandlerInterceptor oauthHandlerInterceptorAdapter() {
        return new OAuth20HandlerInterceptorAdapter(
            requiresAuthenticationAccessTokenInterceptor(),
            requiresAuthenticationAuthorizeInterceptor(),
            accessTokenGrantRequestExtractors);
    }

    @Override
    public void configureAuthenticationThrottlingExecutionPlan(final AuthenticationThrottlingExecutionPlan plan) {
        plan.registerAuthenticationThrottleInterceptor(oauthHandlerInterceptorAdapter());
    }

}
