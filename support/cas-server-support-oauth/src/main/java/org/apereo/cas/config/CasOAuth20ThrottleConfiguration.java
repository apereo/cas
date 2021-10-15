package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenSecurityLogic;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;

import lombok.val;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

import static org.apereo.cas.support.oauth.OAuth20Constants.*;

/**
 * This is {@link CasOAuth20ThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "casOAuth20ThrottleConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20ThrottleConfiguration {

    @Configuration(value = "CasOAuth20ThrottlePlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasOAuth20ThrottlePlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oauthThrottleWebMvcConfigurer")
        @Autowired
        public WebMvcConfigurer oauthThrottleWebMvcConfigurer(
            @Qualifier("authenticationThrottlingExecutionPlan")
            final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(final InterceptorRegistry registry) {
                    authenticationThrottlingExecutionPlan.ifAvailable(plan ->
                        plan.getAuthenticationThrottleInterceptors().forEach(handler -> registry.addInterceptor(handler)
                            .order(0).addPathPatterns(BASE_OAUTH20_URL.concat("/*"))));
                }
            };
        }
    }

    @Configuration(value = "CasOAuth20ThrottleMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ThrottleMvcConfiguration {

        @ConditionalOnMissingBean(name = "oauthHandlerInterceptorAdapter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public HandlerInterceptor oauthHandlerInterceptorAdapter(
            @Qualifier("requiresAuthenticationAuthorizeInterceptor")
            final HandlerInterceptor requiresAuthenticationAuthorizeInterceptor,
            @Qualifier("requiresAuthenticationAccessTokenInterceptor")
            final HandlerInterceptor requiresAuthenticationAccessTokenInterceptor,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators,
            final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20HandlerInterceptorAdapter(
                requiresAuthenticationAccessTokenInterceptor,
                requiresAuthenticationAuthorizeInterceptor,
                accessTokenGrantRequestExtractors,
                servicesManager,
                oauthSecConfig.getSessionStore(),
                oauthAuthorizationRequestValidators);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthWebMvcConfigurer")
        @Autowired
        public WebMvcConfigurer oauthWebMvcConfigurer(
            @Qualifier("oauthHandlerInterceptorAdapter")
            final ObjectProvider<HandlerInterceptor> oauthHandlerInterceptorAdapter) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(final InterceptorRegistry registry) {
                    registry.addInterceptor(oauthHandlerInterceptorAdapter.getObject())
                        .order(1).addPathPatterns(BASE_OAUTH20_URL.concat("/*"));
                }
            };
        }
    }

    @Configuration(value = "CasOAuth20ThrottleInterceptorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ThrottleInterceptorConfiguration {
        @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
        @Bean
        @Autowired
        public HandlerInterceptor requiresAuthenticationAuthorizeInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val interceptor = new SecurityInterceptor(oauthSecConfig,
                Authenticators.CAS_OAUTH_CLIENT, JEEHttpActionAdapter.INSTANCE);
            interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
            interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
            return interceptor;
        }

        @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
        @Bean
        @Autowired
        public HandlerInterceptor requiresAuthenticationAccessTokenInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val clients = oauthSecConfig.getClients()
                .findAllClients()
                .stream()
                .filter(client -> client instanceof DirectClient)
                .map(Client::getName)
                .collect(Collectors.joining(","));
            val interceptor = new SecurityInterceptor(oauthSecConfig, clients, JEEHttpActionAdapter.INSTANCE);
            interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
            interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
            interceptor.setSecurityLogic(new OAuth20AccessTokenSecurityLogic());
            return interceptor;
        }
    }
}
