package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20CasAuthenticationInterceptor;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.OAuth20ClientAuthenticationInterceptor;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
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
import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

/**
 * This is {@link CasOAuth20ThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@Configuration(value = "CasOAuth20ThrottleConfiguration", proxyBeanMethods = false)
class CasOAuth20ThrottleConfiguration {
    @Configuration(value = "CasOAuth20ThrottlePlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class CasOAuth20ThrottlePlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oauthThrottleWebMvcConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebMvcConfigurer oauthThrottleWebMvcConfigurer(
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME) final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(
                    @NonNull final InterceptorRegistry registry) {
                    authenticationThrottlingExecutionPlan.ifAvailable(plan -> {
                        val handler = new RefreshableHandlerInterceptor(plan::getAuthenticationThrottleInterceptors);
                        registry.addInterceptor(handler)
                            .order(0)
                            .addPathPatterns(BASE_OAUTH20_URL.concat("/*"));
                    });
                }
            };
        }
    }

    @Configuration(value = "CasOAuth20ThrottleMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20ThrottleMvcConfiguration {

        @ConditionalOnMissingBean(name = "oauthHandlerInterceptorAdapter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor oauthHandlerInterceptorAdapter(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME) final ObjectProvider<OAuth20RequestParameterResolver> oauthRequestParameterResolver,
            @Qualifier("requiresAuthenticationAuthorizeInterceptor") final ObjectProvider<HandlerInterceptor> requiresAuthenticationAuthorizeInterceptor,
            @Qualifier("requiresAuthenticationAccessTokenInterceptor") final ObjectProvider<HandlerInterceptor> requiresAuthenticationAccessTokenInterceptor,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators,
            final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
            @Qualifier(ServicesManager.BEAN_NAME) final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME) final ObjectProvider<OAuth20ConfigurationContext> oauthConfigurationContext) {
            return new OAuth20HandlerInterceptorAdapter(
                requiresAuthenticationAccessTokenInterceptor,
                requiresAuthenticationAuthorizeInterceptor,
                accessTokenGrantRequestExtractors,
                servicesManager,
                oauthAuthorizationRequestValidators,
                oauthRequestParameterResolver,
                oauthConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthWebMvcConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebMvcConfigurer oauthWebMvcConfigurer(
            @Qualifier("oauthHandlerInterceptorAdapter") final ObjectProvider<HandlerInterceptor> oauthHandlerInterceptorAdapter) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(
                    @NonNull final InterceptorRegistry registry) {
                    val handler = new RefreshableHandlerInterceptor(oauthHandlerInterceptorAdapter);
                    registry
                        .addInterceptor(handler)
                        .order(1)
                        .addPathPatterns(BASE_OAUTH20_URL.concat("/*"));
                }
            };
        }
    }

    @Configuration(value = "CasOAuth20ThrottleInterceptorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20ThrottleInterceptorConfiguration {
        @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor requiresAuthenticationAuthorizeInterceptor(
            @Qualifier("oauthDistributedSessionStore")
            final CasSessionStore oauthDistributedSessionStore,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("casCallbackUrlResolver")
            final OAuth20CasCallbackUrlResolver casCallbackUrlResolver,
            @Qualifier("oauthCasClientRedirectActionBuilder")
            final OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder) {
            return new OAuth20CasAuthenticationInterceptor(oauthDistributedSessionStore, ticketGrantingTicketCookieGenerator,
                ticketRegistry, servicesManager, oauthRequestParameterResolver, casCallbackUrlResolver, oauthCasClientRedirectActionBuilder);
        }

        /**
         * Client authentication for the token, revocation, pushed authorization, backchannel authentication and
         * verifiable credential transaction requests, by every {@link OAuth20ClientAuthenticationProvider}.
         * A bearer access token is not client authentication: the pushed authorization request endpoint (RFC 9126,
         * section 2) and the backchannel authentication endpoint (OpenID Connect CIBA, section 7.1) authenticate the
         * client the way the token endpoint does.
         *
         * @param clientAuthenticationProviders the client authentication methods
         * @param servicesManager               the services manager
         * @param oauthRequestParameterResolver the request parameter resolver
         * @return the interceptor
         */
        @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor requiresAuthenticationAccessTokenInterceptor(
            final ObjectProvider<List<OAuth20ClientAuthenticationProvider>> clientAuthenticationProviders,
            @Qualifier(ServicesManager.BEAN_NAME) final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final ObjectProvider<OAuth20RequestParameterResolver> oauthRequestParameterResolver) {
            return new OAuth20ClientAuthenticationInterceptor(clientAuthenticationProviders, servicesManager, oauthRequestParameterResolver);
        }
    }
}
