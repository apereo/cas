package org.apereo.cas.oidc.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.oidc.web.OidcAuthorizationModelAndViewBuilder;
import org.apereo.cas.oidc.web.OidcCallbackAuthorizeViewResolver;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAuthorizationResponseBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcResponseConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("oidcResponseConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcResponseConfiguration {
    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private ObjectProvider<OAuth20CodeFactory> defaultOAuthCodeFactory;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    private ObjectProvider<OAuth20TokenGenerator> oauthTokenGenerator;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    @Autowired
    @Qualifier("oidcConfigurationContext")
    private ObjectProvider<OidcConfigurationContext> oidcConfigurationContext;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("oidcIssuerService")
    private ObjectProvider<OidcIssuerService> oidcIssuerService;

    @ConditionalOnMissingBean(name = "oidcAccessTokenResponseGenerator")
    @Bean
    @RefreshScope
    public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator(), accessTokenJwtBuilder.getObject(),
            casProperties, oidcIssuerService.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcClientCredentialsResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcClientCredentialsResponseBuilder() {
        return new OAuth20ClientCredentialsResponseBuilder(
            servicesManager.getObject(),
            oidcAccessTokenResponseGenerator(),
            oauthTokenGenerator.getObject(),
            casProperties,
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OidcCallbackAuthorizeViewResolver(servicesManager.getObject(),
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcTokenResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcTokenResponseBuilder() {
        return new OAuth20TokenAuthorizationResponseBuilder(
            servicesManager.getObject(),
            casProperties,
            oauthTokenGenerator.getObject(),
            accessTokenJwtBuilder.getObject(),
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    @RefreshScope
    public OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder() {
        return new OidcAuthorizationModelAndViewBuilder(oidcIssuerService.getObject(), casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAuthorizationCodeResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcAuthorizationCodeResponseBuilder() {
        return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(
            servicesManager.getObject(),
            casProperties,
            ticketRegistry.getObject(),
            defaultOAuthCodeFactory.getObject(),
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAuthorizationResponseBuilders")
    public Set<OAuth20AuthorizationResponseBuilder> oidcAuthorizationResponseBuilders() {
        val builders = applicationContext.getBeansOfType(OAuth20AuthorizationResponseBuilder.class, false, true);
        return builders.entrySet().stream().
            filter(e -> !e.getKey().startsWith("oauth")).
            map(Map.Entry::getValue).
            collect(Collectors.toSet());
    }


    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcImplicitIdTokenCallbackUrlBuilder")
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder() {
        return new OidcImplicitIdTokenAuthorizationResponseBuilder(
            oidcIdTokenGenerator(),
            oauthTokenGenerator.getObject(),
            grantingTicketExpirationPolicy.getObject(),
            servicesManager.getObject(),
            accessTokenJwtBuilder.getObject(),
            casProperties,
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder() {
        return new OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(
            oidcIdTokenGenerator(),
            oauthTokenGenerator.getObject(),
            grantingTicketExpirationPolicy.getObject(),
            servicesManager.getObject(),
            accessTokenJwtBuilder.getObject(),
            casProperties,
            oauthAuthorizationModelAndViewBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcResourceOwnerCredentialsResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcResourceOwnerCredentialsResponseBuilder() {
        return new OAuth20ResourceOwnerCredentialsResponseBuilder(
            servicesManager.getObject(), casProperties,
            oidcAccessTokenResponseGenerator(),
            oauthTokenGenerator.getObject(),
            oauthAuthorizationModelAndViewBuilder());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcIdTokenGenerator")
    @Bean
    public IdTokenGeneratorService oidcIdTokenGenerator() {
        return new OidcIdTokenGeneratorService(oidcConfigurationContext.getObject());
    }

}
