package org.apereo.cas.oidc.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGenerator;
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
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

import lombok.val;
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
@Configuration(value = "oidcResponseConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcResponseConfiguration {

    @ConditionalOnMissingBean(name = "oidcAccessTokenResponseGenerator")
    @Bean
    @RefreshScope
    @Autowired
    public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator(
        @Qualifier("oidcIdTokenGenerator")
        final IdTokenGeneratorService oidcIdTokenGenerator,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        final CasConfigurationProperties casProperties,
        @Qualifier("oidcIssuerService")
        final OidcIssuerService oidcIssuerService) {
        return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator, accessTokenJwtBuilder,
            casProperties, oidcIssuerService);
    }

    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "oidcClientCredentialsResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcClientCredentialsResponseBuilder(
        @Qualifier("oidcAccessTokenResponseGenerator")
        final OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new OAuth20ClientCredentialsResponseBuilder(
            servicesManager,
            oidcAccessTokenResponseGenerator,
            oauthTokenGenerator,
            casProperties,
            oauthAuthorizationModelAndViewBuilder);
    }

    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "oidcTokenResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcTokenResponseBuilder(
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        final CasConfigurationProperties casProperties) {
        return new OAuth20TokenAuthorizationResponseBuilder(
            servicesManager,
            casProperties,
            oauthTokenGenerator,
            accessTokenJwtBuilder,
            oauthAuthorizationModelAndViewBuilder);
    }


    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "oidcAuthorizationCodeResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oidcAuthorizationCodeResponseBuilder(
        final CasConfigurationProperties casProperties,
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("defaultOAuthCodeFactory")
        final OAuth20CodeFactory defaultOAuthCodeFactory,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("ticketRegistry")
        final TicketRegistry ticketRegistry) {
        return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(
            servicesManager,
            casProperties,
            ticketRegistry,
            defaultOAuthCodeFactory,
            oauthAuthorizationModelAndViewBuilder);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAuthorizationResponseBuilders")
    @Autowired
    public Set<OAuth20AuthorizationResponseBuilder> oidcAuthorizationResponseBuilders(
        final ConfigurableApplicationContext applicationContext) {
        val builders = applicationContext.getBeansOfType(OAuth20AuthorizationResponseBuilder.class, false, true);
        return builders.entrySet().stream().
            filter(e -> !e.getKey().startsWith("oauth")).
            map(Map.Entry::getValue).
            collect(Collectors.toSet());
    }

    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "oidcImplicitIdTokenCallbackUrlBuilder")
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder(
        @Qualifier("oidcIdTokenGenerator")
        final IdTokenGeneratorService oidcIdTokenGenerator,
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("grantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        final CasConfigurationProperties casProperties,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new OidcImplicitIdTokenAuthorizationResponseBuilder(
            oidcIdTokenGenerator,
            oauthTokenGenerator,
            grantingTicketExpirationPolicy,
            servicesManager,
            accessTokenJwtBuilder,
            casProperties,
            oauthAuthorizationModelAndViewBuilder);
    }

    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder(
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("grantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        final CasConfigurationProperties casProperties,
        @Qualifier("oidcIdTokenGenerator")
        final IdTokenGeneratorService oidcIdTokenGenerator,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(
            oidcIdTokenGenerator,
            oauthTokenGenerator,
            grantingTicketExpirationPolicy,
            servicesManager,
            accessTokenJwtBuilder,
            casProperties,
            oauthAuthorizationModelAndViewBuilder);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcResourceOwnerCredentialsResponseBuilder")
    @Autowired
    public OAuth20AuthorizationResponseBuilder oidcResourceOwnerCredentialsResponseBuilder(
        @Qualifier("oidcAccessTokenResponseGenerator")
        final OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator,
        @Qualifier("oauthAuthorizationModelAndViewBuilder")
        final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new OAuth20ResourceOwnerCredentialsResponseBuilder(
            servicesManager,
            casProperties,
            oidcAccessTokenResponseGenerator,
            oauthTokenGenerator,
            oauthAuthorizationModelAndViewBuilder);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcIdTokenGenerator")
    @Bean
    @Autowired
    public IdTokenGeneratorService oidcIdTokenGenerator(
        @Qualifier("oidcConfigurationContext")
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcIdTokenGeneratorService(oidcConfigurationContext);
    }

}
