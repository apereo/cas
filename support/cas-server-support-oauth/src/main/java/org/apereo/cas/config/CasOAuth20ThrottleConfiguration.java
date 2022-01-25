package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.OAuth20TicketGrantingTicketAwareSecurityLogic;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apereo.cas.support.oauth.OAuth20Constants.*;

/**
 * This is {@link CasOAuth20ThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casOAuth20ThrottleConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20ThrottleConfiguration {

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @Autowired
    @Qualifier("oauthAuthorizationRequestValidators")
    private Set<OAuth20AuthorizationRequestValidator> oauthAuthorizationRequestValidators;

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
    @Bean
    public HandlerInterceptor requiresAuthenticationAuthorizeInterceptor() {
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(),
            Authenticators.CAS_OAUTH_CLIENT, JEEHttpActionAdapter.INSTANCE);
        interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator.getObject(),
                ticketRegistry.getObject(), centralAuthenticationService.getObject());
        interceptor.setSecurityLogic(logic);
        return interceptor;
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
    @Bean
    public HandlerInterceptor requiresAuthenticationAccessTokenInterceptor() {
        val secConfig = oauthSecConfig.getObject();
        val clients = Objects.requireNonNull(secConfig).getClients()
            .findAllClients()
            .stream()
            .filter(client -> client instanceof DirectClient)
            .map(Client::getName)
            .collect(Collectors.joining(","));
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        interceptor.setSecurityLogic(new OAuth20AccessTokenSecurityLogic());
        return interceptor;
    }

    @ConditionalOnMissingBean(name = "oauthHandlerInterceptorAdapter")
    @Bean
    @RefreshScope
    public HandlerInterceptor oauthHandlerInterceptorAdapter() {
        return new OAuth20HandlerInterceptorAdapter(
            requiresAuthenticationAccessTokenInterceptor(),
            requiresAuthenticationAuthorizeInterceptor(),
            accessTokenGrantRequestExtractors,
            servicesManager.getObject(),
            oauthSecConfig.getObject().getSessionStore(),
            oauthAuthorizationRequestValidators);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthThrottleWebMvcConfigurer")
    public WebMvcConfigurer oauthThrottleWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors()
                    .forEach(handler -> registry.addInterceptor(handler).order(0).addPathPatterns(BASE_OAUTH20_URL.concat("/*")));
                registry.addInterceptor(oauthHandlerInterceptorAdapter()).order(1).addPathPatterns(BASE_OAUTH20_URL.concat("/*"));
            }
        };
    }

    public static class OAuth20AccessTokenSecurityLogic extends DefaultSecurityLogic {
        @Override
        public Object perform(final WebContext context, final SessionStore sessionStore,
                              final Config config, final SecurityGrantedAccessAdapter securityGrantedAccessAdapter,
                              final HttpActionAdapter httpActionAdapter,
                              final String clients, final String authorizers,
                              final String matchers, final Object... parameters) {
            val manager = this.getProfileManager(context, sessionStore);
            manager.setConfig(config);
            manager.removeProfiles();

            return super.perform(context, sessionStore, config,
                securityGrantedAccessAdapter, httpActionAdapter, clients, authorizers, matchers, parameters);
        }
    }
}
