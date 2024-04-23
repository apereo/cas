package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestValidator;
import org.apereo.cas.oidc.token.OidcIdTokenExpirationPolicyBuilder;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcPushedAuthorizationModelAndViewBuilder;
import org.apereo.cas.oidc.web.OidcPushedAuthorizationRequestUriResponseBuilder;
import org.apereo.cas.oidc.web.response.OidcIntrospectionResponseGenerator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionResponseGenerator;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.ObjectProvider;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@Configuration(value = "OidcResponseConfiguration", proxyBeanMethods = false)
class OidcResponseConfiguration {

    @Configuration(value = "OidcResponseAccessTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseAccessTokenConfiguration {

        @ConditionalOnMissingBean(name = "oidcAccessTokenResponseGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("oidcIdTokenGenerator")
            final IdTokenGeneratorService oidcIdTokenGenerator,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService) {
            return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator, accessTokenJwtBuilder,
                ticketRegistry, casProperties, oidcIssuerService);
        }
    }

    @Configuration(value = "OidcResponseClientCredentialConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseClientCredentialConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcClientCredentialsResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcClientCredentialsResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OAuth20ClientCredentialsResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

    }

    @Configuration(value = "OidcResponseTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseTokenConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcTokenResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcTokenResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OAuth20TokenAuthorizationResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }
    }

    @Configuration(value = "OidcResponseAuthorizationCodeConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseAuthorizationCodeConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAuthorizationCodeResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcAuthorizationCodeResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcPushedAuthorizationModelAndViewBuilder")
        public OAuth20AuthorizationModelAndViewBuilder oidcPushedAuthorizationModelAndViewBuilder() {
            return new OidcPushedAuthorizationModelAndViewBuilder();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcPushedAuthorizationRequestResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcPushedAuthorizationRequestResponseBuilder(
            @Qualifier("oidcPushedAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oidcPushedAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcPushedAuthorizationRequestUriResponseBuilder(oidcConfigurationContext,
                oidcPushedAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oidcPushedAuthorizationRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oidcPushedAuthorizationRequestValidator(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new OidcPushedAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer,
                ticketRegistry, ticketFactory, oauthRequestParameterResolver);
        }
    }

    @Configuration(value = "OidcResponseImplicitConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseImplicitConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcImplicitIdTokenCallbackUrlBuilder")
        public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcImplicitIdTokenAuthorizationResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
        public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }
    }

    @Configuration(value = "OidcResponseResourceOwnerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseResourceOwnerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcResourceOwnerCredentialsResponseBuilder")
        public OAuth20AuthorizationResponseBuilder oidcResourceOwnerCredentialsResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OAuth20ResourceOwnerCredentialsResponseBuilder(
                oidcConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }
    }

    @Configuration(value = "OidcResponseTokenGenerationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseTokenGenerationConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcIntrospectionResponseGenerator")
        @Bean
        public OAuth20IntrospectionResponseGenerator oidcIntrospectionResponseGenerator(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcIntrospectionResponseGenerator(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcIdTokenGenerator")
        @Bean
        public IdTokenGeneratorService oidcIdTokenGenerator(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcIdTokenGeneratorService(oidcConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcIdTokenExpirationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder oidcIdTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OidcIdTokenExpirationPolicyBuilder(casProperties);
        }
    }

}
