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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcResponseConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "oidcResponseConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcResponseConfiguration {

    @Configuration(value = "OidcResponseAccessTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseAccessTokenConfiguration {

        @ConditionalOnMissingBean(name = "oidcAccessTokenResponseGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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

    }

    @Configuration(value = "OidcResponseClientCredentialConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseClientCredentialConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "oidcClientCredentialsResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcClientCredentialsResponseBuilder(
            @Qualifier("oidcAccessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ClientCredentialsResponseBuilder(
                servicesManager,
                oidcAccessTokenResponseGenerator,
                oauthTokenGenerator,
                casProperties,
                oauthAuthorizationModelAndViewBuilder);
        }

    }

    @Configuration(value = "OidcResponseTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseTokenConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "oidcTokenResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcTokenResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
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


    }

    @Configuration(value = "OidcResponseAuthorizationCodeConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseAuthorizationCodeConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "oidcAuthorizationCodeResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcAuthorizationCodeResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("defaultOAuthCodeFactory")
            final OAuth20CodeFactory defaultOAuthCodeFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(
                servicesManager,
                casProperties,
                ticketRegistry,
                defaultOAuthCodeFactory,
                oauthAuthorizationModelAndViewBuilder);
        }


    }

    @Configuration(value = "OidcResponseImplicitConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseImplicitConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "oidcImplicitIdTokenCallbackUrlBuilder")
        public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder(
            @Qualifier("oidcIdTokenGenerator")
            final IdTokenGeneratorService oidcIdTokenGenerator,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
            final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
        public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
            final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcIdTokenGenerator")
            final IdTokenGeneratorService oidcIdTokenGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
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

    }

    @Configuration(value = "OidcResponseResourceOwnerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseResourceOwnerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcResourceOwnerCredentialsResponseBuilder")
        @Autowired
        public OAuth20AuthorizationResponseBuilder oidcResourceOwnerCredentialsResponseBuilder(
            @Qualifier("oidcAccessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ResourceOwnerCredentialsResponseBuilder(
                servicesManager,
                casProperties,
                oidcAccessTokenResponseGenerator,
                oauthTokenGenerator,
                oauthAuthorizationModelAndViewBuilder);
        }
    }

    @Configuration(value = "OidcResponseTokenGenerationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcResponseTokenGenerationConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcIdTokenGenerator")
        @Bean
        @Autowired
        public IdTokenGeneratorService oidcIdTokenGenerator(
            @Qualifier("oidcConfigurationContext")
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcIdTokenGeneratorService(oidcConfigurationContext);
        }
    }

}
