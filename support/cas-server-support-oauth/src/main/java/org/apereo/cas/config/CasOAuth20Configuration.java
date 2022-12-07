package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.validation.AssertionImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20DefaultCasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuth20ProofKeyCodeExchangeAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20RefreshTokenAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20UsernamePasswordAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.DefaultOAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20IdTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20TokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20DeviceCodeResponseTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20RevocationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.DefaultOAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenGrantRequestAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenResponseAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AuthorizationResponseAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenClientCredentialsGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenDeviceCodeResponseRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantAuditableRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenPasswordGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRefreshTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20DefaultAccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.support.oauth.web.response.callback.DefaultOAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20InvalidAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.mode.DefaultOAuth20ResponseModeFactory;
import org.apereo.cas.support.oauth.web.response.callback.mode.OAuth20ResponseModeFormPostBuilder;
import org.apereo.cas.support.oauth.web.response.callback.mode.OAuth20ResponseModeFragmentBuilder;
import org.apereo.cas.support.oauth.web.response.callback.mode.OAuth20ResponseModeQueryBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.code.OAuth20CodeExpirationPolicyBuilder;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.core.matching.matcher.Matcher;
import org.pac4j.core.matching.matcher.csrf.CsrfTokenGeneratorMatcher;
import org.pac4j.core.matching.matcher.csrf.DefaultCsrfTokenGenerator;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This this {@link CasOAuth20Configuration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@AutoConfiguration
public class CasOAuth20Configuration {
    @Configuration(value = "CasOAuth20JwtConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20JwtConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthAccessTokenJwtCipherExecutor")
        public CipherExecutor oauthAccessTokenJwtCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getOauth().getAccessToken().getCrypto();

            val enabled = FunctionUtils.doIf(
                    !crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                    && StringUtils.isNotBlank(crypto.getSigning().getKey()),
                    () -> {
                        LOGGER.warn("Default encryption/signing is not enabled explicitly for OAuth access tokens as JWTs if necessary, "
                                    + "yet signing/encryption keys are defined for operations. CAS will proceed to enable "
                                    + "the token encryption/signing functionality.");
                        return Boolean.TRUE;
                    },
                    crypto::isEnabled)
                .get();

            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, OAuth20JwtAccessTokenCipherExecutor.class);
            }
            LOGGER.info("OAuth access token encryption/signing is turned off for JWTs, if/when needed. This "
                        + "MAY NOT be safe in a production environment.");
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = "oauthRegisteredServiceJwtAccessTokenCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor() {
            return new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor();
        }

        @ConditionalOnMissingBean(name = "accessTokenJwtBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public JwtBuilder accessTokenJwtBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthRegisteredServiceJwtAccessTokenCipherExecutor")
            final RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor,
            @Qualifier("oauthAccessTokenJwtCipherExecutor")
            final CipherExecutor oauthAccessTokenJwtCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20JwtBuilder(oauthAccessTokenJwtCipherExecutor, servicesManager,
                oauthRegisteredServiceJwtAccessTokenCipherExecutor, casProperties);
        }
    }

    @Configuration(value = "CasOAuth20ContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ContextConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ConfigurationContext oauth20ConfigurationContext(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("oAuth2UserProfileDataCreator")
            final OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator,
            @Qualifier("oauthDistributedSessionCookieGenerator")
            final CasCookieBuilder oauthDistributedSessionCookieGenerator,
            @Qualifier("oauthUserProfileViewRenderer")
            final OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor,
            @Qualifier("oauthPrincipalFactory")
            final PrincipalFactory oauthPrincipalFactory,
            @Qualifier("callbackAuthorizeViewResolver")
            final OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig,
            final ObjectProvider<List<OAuth20TokenRequestValidator>> oauthTokenRequestValidators,
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier("oauthInvalidAuthorizationBuilder")
            final OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder,
            @Qualifier("consentApprovalViewResolver")
            final ConsentApprovalViewResolver consentApprovalViewResolver,
            @Qualifier("accessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
            @Qualifier("oauthCasAuthenticationBuilder")
            final OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            final ObjectProvider<List<OAuth20AuthorizationResponseBuilder>> oauthAuthorizationResponseBuilders,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator) {
            return OAuth20ConfigurationContext.builder()
                .argumentExtractor(argumentExtractor)
                .requestParameterResolver(oauthRequestParameterResolver)
                .applicationContext(applicationContext)
                .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor)
                .sessionStore(oauthDistributedSessionStore)
                .servicesManager(servicesManager)
                .ticketRegistry(ticketRegistry)
                .ticketFactory(ticketFactory)
                .principalFactory(oauthPrincipalFactory)
                .webApplicationServiceServiceFactory(webApplicationServiceFactory)
                .casProperties(casProperties)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator)
                .oauthConfig(oauthSecConfig)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .centralAuthenticationService(centralAuthenticationService)
                .callbackAuthorizeViewResolver(callbackAuthorizeViewResolver)
                .profileScopeToAttributesFilter(profileScopeToAttributesFilter)
                .accessTokenGenerator(oauthTokenGenerator)
                .accessTokenJwtBuilder(accessTokenJwtBuilder)
                .accessTokenResponseGenerator(accessTokenResponseGenerator)
                .deviceTokenExpirationPolicy(deviceTokenExpirationPolicy)
                .accessTokenGrantRequestValidators(oauthTokenRequestValidators)
                .userProfileDataCreator(oAuth2UserProfileDataCreator)
                .userProfileViewRenderer(oauthUserProfileViewRenderer)
                .consentApprovalViewResolver(consentApprovalViewResolver)
                .authenticationBuilder(oauthCasAuthenticationBuilder)
                .oauthInvalidAuthorizationResponseBuilder(oauthInvalidAuthorizationBuilder)
                .oauthAuthorizationResponseBuilders(oauthAuthorizationResponseBuilders)
                .oauthRequestValidators(oauthAuthorizationRequestValidators)
                .clientSecretValidator(oauth20ClientSecretValidator)
                .authenticationAttributeReleasePolicy(authenticationAttributeReleasePolicy)
                .build();
        }
    }

    @Configuration(value = "CasOAuth20WebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20WebConfiguration {

        @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentApprovalViewResolver consentApprovalViewResolver(
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ConsentApprovalViewResolver(casProperties, oauthDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = "oAuth2UserProfileDataCreator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator(
            @Qualifier("oauth20ConfigurationContext")
            final ObjectProvider<OAuth20ConfigurationContext> context) {
            return new DefaultOAuth20UserProfileDataCreator(context);
        }
    }

    @Configuration(value = "CasOAuth20TokenGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20TokenGeneratorConfiguration {
        @ConditionalOnMissingBean(name = "oauthTokenGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenGenerator oauthTokenGenerator(
            @Qualifier("defaultDeviceUserCodeFactory")
            final OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory,
            @Qualifier("defaultDeviceTokenFactory")
            final OAuth20DeviceTokenFactory defaultDeviceTokenFactory,
            @Qualifier("defaultRefreshTokenFactory")
            final OAuth20RefreshTokenFactory defaultRefreshTokenFactory,
            @Qualifier("defaultAccessTokenFactory")
            final OAuth20AccessTokenFactory defaultAccessTokenFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultTokenGenerator(
                defaultAccessTokenFactory, defaultDeviceTokenFactory,
                defaultDeviceUserCodeFactory, defaultRefreshTokenFactory,
                ticketRegistry, casProperties);
        }

        @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultAccessTokenResponseGenerator(accessTokenJwtBuilder, casProperties);
        }

    }

    @Configuration(value = "CasOAuth20ClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ClientConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client oauthCasClient(
            @Qualifier("oauthCasClientRedirectActionBuilder")
            final OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder,
            @Qualifier("casCallbackUrlResolver")
            final UrlResolver casCallbackUrlResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            val server = casProperties.getServer();
            val cfg = new CasConfiguration(server.getLoginUrl());
            val validator = new InternalTicketValidator(centralAuthenticationService,
                webApplicationServiceFactory, authenticationAttributeReleasePolicy, servicesManager);
            cfg.setDefaultTicketValidator((ticket, service) -> {
                val result = validator.validate(ticket, service);
                val attrPrincipal = new AttributePrincipalImpl(result.getPrincipal().getId(), (Map) result.getPrincipal().getAttributes());
                val registeredService = (RegisteredService) result.getContext().get(RegisteredService.class.getName());
                val assertion = (Assertion) result.getContext().get(Assertion.class.getName());
                val authenticationAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
                    assertion.primaryAuthentication(), assertion, new HashMap<>(0), registeredService);
                return new AssertionImpl(attrPrincipal, (Map) authenticationAttributes);
            });
            val oauthCasClient = new CasClient(cfg);
            oauthCasClient.setRedirectionActionBuilder((webContext, sessionStore) ->
                oauthCasClientRedirectActionBuilder.build(oauthCasClient, webContext));
            oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
            oauthCasClient.setUrlResolver(casCallbackUrlResolver);
            oauthCasClient.setCallbackUrl(OAuth20Utils.casOAuthCallbackUrl(server.getPrefix()));
            oauthCasClient.setCheckAuthenticationAttempt(false);
            oauthCasClient.init();
            return oauthCasClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client basicAuthClient(
            @Qualifier("oauthClientAuthenticator")
            final Authenticator oauthClientAuthenticator) {
            val basicAuthClient = new DirectBasicAuthClient(oauthClientAuthenticator);
            basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
            basicAuthClient.init();
            return basicAuthClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client directFormClient(
            @Qualifier("oauthClientAuthenticator")
            final Authenticator oauthClientAuthenticator) {
            val directFormClient = new DirectFormClient(oauthClientAuthenticator);
            directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
            directFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            directFormClient.setPasswordParameter(OAuth20Constants.CLIENT_SECRET);
            directFormClient.init();
            return directFormClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client pkceAuthnFormClient(
            @Qualifier("oAuthProofKeyCodeExchangeAuthenticator")
            final Authenticator oAuthProofKeyCodeExchangeAuthenticator) {
            val pkceAuthnFormClient = new DirectFormClient(oAuthProofKeyCodeExchangeAuthenticator);
            pkceAuthnFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM_PROOF_KEY_CODE_EXCHANGE_AUTHN);
            pkceAuthnFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            pkceAuthnFormClient.setPasswordParameter(OAuth20Constants.CODE_VERIFIER);
            pkceAuthnFormClient.init();
            return pkceAuthnFormClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client pkceBasicAuthClient(
            @Qualifier("oAuthProofKeyCodeExchangeAuthenticator")
            final Authenticator oAuthProofKeyCodeExchangeAuthenticator) {
            val pkceBasicAuthClient = new DirectBasicAuthClient(oAuthProofKeyCodeExchangeAuthenticator);
            pkceBasicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_PROOF_KEY_CODE_EXCHANGE_AUTHN);
            pkceBasicAuthClient.init();
            return pkceBasicAuthClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client refreshTokenFormClient(
            @Qualifier("oAuthRefreshTokenAuthenticator")
            final Authenticator oAuthRefreshTokenAuthenticator) {
            val refreshTokenFormClient = new DirectFormClient(oAuthRefreshTokenAuthenticator);
            refreshTokenFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_FORM_REFRESH_TOKEN_AUTHN);
            refreshTokenFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            refreshTokenFormClient.setPasswordParameter(OAuth20Constants.REFRESH_TOKEN);
            refreshTokenFormClient.init();
            return refreshTokenFormClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client userFormClient(
            @Qualifier("oauthUserAuthenticator")
            final Authenticator oauthUserAuthenticator) {
            val userFormClient = new DirectFormClient(oauthUserAuthenticator);
            userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
            userFormClient.init();
            return userFormClient;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Client accessTokenClient(
            @Qualifier("oauthAccessTokenAuthenticator")
            final Authenticator oauthAccessTokenAuthenticator) {
            val accessTokenClient = new HeaderClient();
            accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
            accessTokenClient.setAuthenticator(oauthAccessTokenAuthenticator);
            accessTokenClient.setName(Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN);
            accessTokenClient.init();
            return accessTokenClient;
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecConfigClients")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<Client> oauthSecConfigClients(
            @Qualifier("basicAuthClient")
            final Client basicAuthClient,
            @Qualifier("directFormClient")
            final Client directFormClient,
            @Qualifier("pkceAuthnFormClient")
            final Client pkceAuthnFormClient,
            @Qualifier("pkceBasicAuthClient")
            final Client pkceBasicAuthClient,
            @Qualifier("refreshTokenFormClient")
            final Client refreshTokenFormClient,
            @Qualifier("oauthCasClient")
            final Client oauthCasClient,
            @Qualifier("userFormClient")
            final Client userFormClient,
            @Qualifier("accessTokenClient")
            final Client accessTokenClient,
            final ObjectProvider<List<OAuth20AuthenticationClientProvider>> providers) {
            val clientProviders = Optional.ofNullable(providers.getIfAvailable()).orElseGet(ArrayList::new);
            AnnotationAwareOrderComparator.sort(clientProviders);
            val clientList = new ArrayList<Client>();
            clientProviders.forEach(p -> clientList.add(p.createClient()));
            clientList.add(oauthCasClient);
            clientList.add(basicAuthClient);
            clientList.add(pkceAuthnFormClient);
            clientList.add(pkceBasicAuthClient);
            clientList.add(refreshTokenFormClient);
            clientList.add(directFormClient);
            clientList.add(userFormClient);
            clientList.add(accessTokenClient);

            return BeanContainer.of(clientList);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecConfig")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Config oauthSecConfig(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("oauthSecCsrfTokenMatcher")
            final Matcher oauthSecCsrfTokenMatcher,
            @Qualifier("oauthSecConfigClients")
            final BeanContainer<Client> oauthSecConfigClients,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            val config = new Config(callbackUrl, oauthSecConfigClients.toList());
            config.setSessionStoreFactory(objects -> oauthDistributedSessionStore);
            config.setMatcher(oauthSecCsrfTokenMatcher);
            config.setProfileManagerFactory((webContext, sessionStore) ->
                new OAuth20ClientIdAwareProfileManager(webContext, oauthDistributedSessionStore,
                    servicesManager, oauthRequestParameterResolver));
            return config;
        }
    }

    @Configuration(value = "CasOAuth20ExtractorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ExtractorConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(context);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenAuthorizationCodeGrantRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenAuthorizationCodeGrantRequestExtractor(context);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenRefreshTokenGrantRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenRefreshTokenGrantRequestExtractor(context);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenPasswordGrantRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenPasswordGrantRequestExtractor(context);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenClientCredentialsGrantRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenClientCredentialsGrantRequestExtractor(context);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccessTokenGrantRequestExtractor accessTokenDeviceCodeResponseRequestExtractor(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new AccessTokenDeviceCodeResponseRequestExtractor(context);
        }
    }

    @Configuration(value = "CasOAuth20SessionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20SessionConfiguration {

        @ConditionalOnMissingBean(name = "oauthDistributedSessionCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder oauthDistributedSessionCookieGenerator(final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getAuthn().getOauth().getSessionReplication().getCookie();
            return CookieUtils.buildCookieRetrievingGenerator(cookie);
        }

        @ConditionalOnMissingBean(name = "oauthDistributedSessionStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SessionStore oauthDistributedSessionStore(
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("oauthDistributedSessionCookieGenerator")
            final CasCookieBuilder oauthDistributedSessionCookieGenerator,
            final CasConfigurationProperties casProperties) {
            val replicate = casProperties.getAuthn().getOauth().getSessionReplication().isReplicateSessions();
            if (replicate) {
                return new DistributedJEESessionStore(ticketRegistry,
                    ticketFactory, oauthDistributedSessionCookieGenerator);
            }
            return JEESessionStore.INSTANCE;
        }
    }

    @Configuration(value = "CasOAuth20LogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20LogoutConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oauthLogoutExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LogoutExecutionPlanConfigurer oauthLogoutExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore) {
            return plan -> {
                val replicate = casProperties.getAuthn().getOauth().getSessionReplication().isReplicateSessions();
                if (replicate) {
                    plan.registerLogoutPostProcessor(ticketGrantingTicket -> {
                        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
                        if (request != null && response != null) {
                            oauthDistributedSessionStore.destroySession(new JEEContext(request, response));
                        }
                    });
                }
            };
        }

    }

    @Configuration(value = "CasOAuth20CoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20CoreConfiguration {

        @ConditionalOnMissingBean(name = OAuth20RequestParameterResolver.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20RequestParameterResolver oauthRequestParameterResolver(
            @Qualifier("oauthSecConfig")
            final ObjectProvider<Config> oauthSecConfig,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder) {
            return new DefaultOAuth20RequestParameterResolver(accessTokenJwtBuilder, oauthSecConfig);
        }

        @ConditionalOnMissingBean(name = "oauthPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory oauthPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthAuthorizationModelAndViewBuilder")
        public OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder(
            @Qualifier(OAuth20ResponseModeFactory.BEAN_NAME)
            final OAuth20ResponseModeFactory oauthResponseModeFactory) {
            return new DefaultOAuth20AuthorizationModelAndViewBuilder(oauthResponseModeFactory);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OAuth20ResponseModeFactory.BEAN_NAME)
        public OAuth20ResponseModeFactory oauthResponseModeFactory(final List<OAuth20ResponseModeBuilder> responseModeBuilders) {
            val factory = new DefaultOAuth20ResponseModeFactory();
            responseModeBuilders.forEach(factory::registerBuilder);
            return factory;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthQueryResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthQueryResponseModeBuilder() {
            return new OAuth20ResponseModeQueryBuilder();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthFormPostResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthFormPostResponseModeBuilder() {
            return new OAuth20ResponseModeFormPostBuilder();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthFragmentResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthFragmentResponseModeBuilder() {
            return new OAuth20ResponseModeFragmentBuilder();
        }

        @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultUserProfileViewRenderer(servicesManager,
                casProperties.getAuthn().getOauth());
        }

        @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
            return OAuth20CallbackAuthorizeViewResolver.asDefault();
        }

        @ConditionalOnMissingBean(name = "profileScopeToAttributesFilter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
            return new DefaultOAuth20ProfileScopeToAttributesFilter();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oauthRegisteredServiceCipherExecutor")
        public CipherExecutor oauthRegisteredServiceCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getOauth().getCrypto();

            val enabled = FunctionUtils.doIf(
                    !crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey()),
                    () -> {
                        LOGGER.warn("Secret encryption/signing is not enabled explicitly in the configuration for OAuth/OIDC services, yet signing/encryption keys "
                                    + "are defined for operations. CAS will proceed to enable the encryption/signing functionality.");
                        return Boolean.TRUE;
                    },
                    crypto::isEnabled)
                .get();

            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, OAuth20RegisteredServiceCipherExecutor.class);
            }
            LOGGER.info("Relying party secret encryption/signing is turned off for OAuth/OIDC services. This "
                        + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
                        + "signing and verification of relying party secrets.");
            return CipherExecutor.noOp();
        }


        @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
            return new OAuth20DefaultCasClientRedirectActionBuilder();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casCallbackUrlResolver")
        public UrlResolver casCallbackUrlResolver(final CasConfigurationProperties casProperties,
                                                  @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
                                                  final OAuth20RequestParameterResolver oauthRequestParameterResolver) {
            val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            return new OAuth20CasCallbackUrlResolver(callbackUrl, oauthRequestParameterResolver);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecCsrfTokenMatcher")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Matcher oauthSecCsrfTokenMatcher(final CasConfigurationProperties casProperties) {
            val csrfMatcher = new CsrfTokenGeneratorMatcher(new DefaultCsrfTokenGenerator());
            val oauth = casProperties.getAuthn().getOauth();
            val csrfCookie = oauth.getCsrfCookie();
            val maxAge = csrfCookie.getMaxAge();
            if (maxAge >= 0) {
                csrfMatcher.setMaxAge(maxAge);
            }
            csrfMatcher.setSameSitePolicy(csrfCookie.getSameSitePolicy());
            csrfMatcher.setDomain(csrfCookie.getDomain());
            csrfMatcher.setPath(csrfCookie.getPath());
            csrfMatcher.setHttpOnly(csrfCookie.isHttpOnly());
            csrfMatcher.setSecure(csrfCookie.isSecure());
            return csrfMatcher;
        }

    }

    @Configuration(value = "CasOAuth20ValidatorsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ValidatorsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = OAuth20ClientSecretValidator.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ClientSecretValidator oauth20ClientSecretValidator(
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new DefaultOAuth20ClientSecretValidator(oauthRegisteredServiceCipherExecutor);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            val grantTypesSupported = context.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(grantTypesSupported.contains(OAuth20GrantTypes.AUTHORIZATION_CODE.getType()))
                .supply(() -> new OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(context))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeGrantTypeTokenRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthAuthorizationCodeGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            val grantTypesSupported = oauth20ConfigurationContext.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(grantTypesSupported.contains(OAuth20GrantTypes.AUTHORIZATION_CODE.getType()))
                .supply(() -> new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(oauth20ConfigurationContext))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthDeviceCodeResponseTypeRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthDeviceCodeResponseTypeRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(responseTypesSupported.contains(OAuth20ResponseTypes.DEVICE_CODE.getType()))
                .supply(() -> new OAuth20DeviceCodeResponseTypeRequestValidator(servicesManager,
                    webApplicationServiceFactory, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthRevocationRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthRevocationRequestValidator(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20RevocationRequestValidator(servicesManager,
                oauthDistributedSessionStore, oauthRequestParameterResolver);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthRefreshTokenGrantTypeTokenRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthRefreshTokenGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            val grantTypesSupported = oauth20ConfigurationContext.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(grantTypesSupported.contains(OAuth20GrantTypes.REFRESH_TOKEN.getType()))
                .supply(() -> new OAuth20RefreshTokenGrantTypeTokenRequestValidator(oauth20ConfigurationContext))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthPasswordGrantTypeTokenRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthPasswordGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            val grantTypesSupported = oauth20ConfigurationContext.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(grantTypesSupported.contains(OAuth20GrantTypes.PASSWORD.getType()))
                .supply(() -> new OAuth20PasswordGrantTypeTokenRequestValidator(oauth20ConfigurationContext))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthClientCredentialsGrantTypeTokenRequestValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenRequestValidator oauthClientCredentialsGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            val grantTypesSupported = oauth20ConfigurationContext.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
            return BeanSupplier.of(OAuth20TokenRequestValidator.class)
                .when(grantTypesSupported.contains(OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()))
                .supply(() -> new OAuth20ClientCredentialsGrantTypeTokenRequestValidator(oauth20ConfigurationContext))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseTypeRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oauthAuthorizationCodeResponseTypeRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
                .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.CODE.getType()))
                .supply(() -> new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager,
                    webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
                .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.CODE.getType()))
                .supply(() -> new OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(servicesManager,
                    webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oauthTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oauthTokenResponseTypeRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {

            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
                .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.TOKEN.getType()))
                .supply(() -> new OAuth20TokenResponseTypeAuthorizationRequestValidator(servicesManager,
                    webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oauthIdTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oauthIdTokenResponseTypeRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
                .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.ID_TOKEN.getType()))
                .supply(() -> new OAuth20IdTokenResponseTypeAuthorizationRequestValidator(servicesManager,
                    webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oauthIdTokenAndTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationRequestValidator oauthIdTokenAndTokenResponseTypeRequestValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
            return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
                .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.IDTOKEN_TOKEN.getType()))
                .supply(() -> new OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator(servicesManager,
                    webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasOAuth20TicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20TicketFactoryPlanConfiguration {

        @ConditionalOnMissingBean(name = "defaultRefreshTokenFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultRefreshTokenFactoryConfigurer(
            @Qualifier("defaultRefreshTokenFactory")
            final OAuth20RefreshTokenFactory defaultRefreshTokenFactory) {
            return () -> defaultRefreshTokenFactory;
        }

        @ConditionalOnMissingBean(name = "defaultDeviceUserCodeFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultDeviceUserCodeFactoryConfigurer(
            @Qualifier("defaultDeviceUserCodeFactory")
            final OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory) {
            return () -> defaultDeviceUserCodeFactory;
        }

        @ConditionalOnMissingBean(name = "defaultAccessTokenFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultAccessTokenFactoryConfigurer(
            @Qualifier("defaultAccessTokenFactory")
            final OAuth20AccessTokenFactory defaultAccessTokenFactory) {
            return () -> defaultAccessTokenFactory;
        }

        @ConditionalOnMissingBean(name = "defaultDeviceTokenFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultDeviceTokenFactoryConfigurer(
            @Qualifier("defaultDeviceTokenFactory")
            final OAuth20DeviceTokenFactory defaultDeviceTokenFactory) {
            return () -> defaultDeviceTokenFactory;
        }


        @ConditionalOnMissingBean(name = "defaultOAuthCodeFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultOAuthCodeFactoryConfigurer(
            @Qualifier("defaultOAuthCodeFactory")
            final OAuth20CodeFactory defaultOAuthCodeFactory) {
            return () -> defaultOAuthCodeFactory;
        }

    }

    @Configuration(value = "CasOAuth20TicketsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20TicketsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "accessTokenExpirationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder accessTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20AccessTokenExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "deviceTokenExpirationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder deviceTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20DeviceTokenExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder oAuthCodeExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20CodeExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oAuthCodeIdGenerator")
        public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "refreshTokenIdGenerator")
        public UniqueTicketIdGenerator refreshTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @ConditionalOnMissingBean(name = "accessTokenIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator accessTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @ConditionalOnMissingBean(name = "deviceTokenIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator deviceTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder refreshTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20RefreshTokenExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultRefreshTokenFactory")
        public OAuth20RefreshTokenFactory defaultRefreshTokenFactory(
            @Qualifier("refreshTokenIdGenerator")
            final UniqueTicketIdGenerator refreshTokenIdGenerator,
            @Qualifier("refreshTokenExpirationPolicy")
            final ExpirationPolicyBuilder refreshTokenExpirationPolicy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20DefaultRefreshTokenFactory(refreshTokenIdGenerator,
                refreshTokenExpirationPolicy, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultAccessTokenFactory")
        public OAuth20AccessTokenFactory defaultAccessTokenFactory(
            @Qualifier("accessTokenIdGenerator")
            final UniqueTicketIdGenerator accessTokenIdGenerator,
            @Qualifier("accessTokenExpirationPolicy")
            final ExpirationPolicyBuilder accessTokenExpirationPolicy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder) {
            return new OAuth20DefaultAccessTokenFactory(accessTokenIdGenerator,
                accessTokenExpirationPolicy, accessTokenJwtBuilder, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultDeviceTokenFactory")
        public OAuth20DeviceTokenFactory defaultDeviceTokenFactory(
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier("deviceTokenIdGenerator")
            final UniqueTicketIdGenerator deviceTokenIdGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultDeviceTokenFactory(deviceTokenIdGenerator, deviceTokenExpirationPolicy,
                casProperties.getAuthn().getOauth().getDeviceUserCode().getUserCodeLength(), servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultDeviceUserCodeFactory")
        public OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory(
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier("deviceTokenIdGenerator")
            final UniqueTicketIdGenerator deviceTokenIdGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultDeviceUserCodeFactory(deviceTokenIdGenerator, deviceTokenExpirationPolicy,
                casProperties.getAuthn().getOauth().getDeviceUserCode().getUserCodeLength(), servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
        public OAuth20CodeFactory defaultOAuthCodeFactory(
            @Qualifier("protocolTicketCipherExecutor")
            final CipherExecutor protocolTicketCipherExecutor,
            @Qualifier("oAuthCodeIdGenerator")
            final UniqueTicketIdGenerator oAuthCodeIdGenerator,
            @Qualifier("oAuthCodeExpirationPolicy")
            final ExpirationPolicyBuilder oAuthCodeExpirationPolicy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20DefaultOAuthCodeFactory(oAuthCodeIdGenerator,
                oAuthCodeExpirationPolicy, servicesManager, protocolTicketCipherExecutor);
        }
    }

    @Configuration(value = "CasOAuth20ResponseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ResponseConfiguration {
        @ConditionalOnMissingBean(name = "oauthResourceOwnerCredentialsResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20ResourceOwnerCredentialsResponseBuilder(oauth20ConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthClientCredentialsResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationResponseBuilder oauthClientCredentialsResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20ClientCredentialsResponseBuilder(oauth20ConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthTokenResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20TokenAuthorizationResponseBuilder(oauth20ConfigurationContext,
                oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(context,
                oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthInvalidAuthorizationBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder(
            @Qualifier(OAuth20ResponseModeFactory.BEAN_NAME)
            final OAuth20ResponseModeFactory oauthResponseModeFactory,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20InvalidAuthorizationResponseBuilder(servicesManager,
                oauthRequestParameterResolver, oauthResponseModeFactory);
        }

    }

    @Configuration(value = "CasOAuth20AuthenticatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20AuthenticatorConfiguration {
        @ConditionalOnMissingBean(name = "oauthCasAuthenticationBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oauthPrincipalFactory")
            final PrincipalFactory oauthPrincipalFactory,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultCasAuthenticationBuilder(oauthPrincipalFactory,
                webApplicationServiceFactory, profileScopeToAttributesFilter,
                oauthRequestParameterResolver, casProperties);
        }

        @ConditionalOnMissingBean(name = "oauthClientAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oauthClientAuthenticator(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator) {
            return new OAuth20ClientIdClientSecretAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                defaultPrincipalResolver,
                oauthRequestParameterResolver,
                oauth20ClientSecretValidator);
        }

        @ConditionalOnMissingBean(name = "oAuthProofKeyCodeExchangeAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oAuthProofKeyCodeExchangeAuthenticator(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator) {
            return new OAuth20ProofKeyCodeExchangeAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                defaultPrincipalResolver,
                oauthRequestParameterResolver,
                oauth20ClientSecretValidator);
        }

        @ConditionalOnMissingBean(name = "oAuthRefreshTokenAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oAuthRefreshTokenAuthenticator(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator) {
            return new OAuth20RefreshTokenAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                defaultPrincipalResolver,
                oauthRequestParameterResolver,
                oauth20ClientSecretValidator);
        }

        @ConditionalOnMissingBean(name = "oauthUserAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oauthUserAuthenticator(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            return new OAuth20UsernamePasswordAuthenticator(
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                oauthDistributedSessionStore,
                oauthRequestParameterResolver,
                oauth20ClientSecretValidator,
                authenticationAttributeReleasePolicy);
        }

        @ConditionalOnMissingBean(name = "oauthAccessTokenAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oauthAccessTokenAuthenticator(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            return new OAuth20AccessTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
        }
    }

    @Configuration(value = "CasOAuth20AuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20AuditConfiguration {
        @ConditionalOnMissingBean(name = "accessTokenGrantAuditableRequestExtractor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditableExecution accessTokenGrantAuditableRequestExtractor(
            final List<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
            return new AccessTokenGrantAuditableRequestExtractor(accessTokenGrantRequestExtractors);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthAuditTrailRecordResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer oauthAuditTrailRecordResolutionPlanConfigurer() {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.OAUTH2_USER_PROFILE_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                        AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.OAUTH2_USER_PROFILE_RESOURCE_RESOLVER,
                    new OAuth20UserProfileDataAuditResourceResolver());

                plan.registerAuditActionResolver(AuditActionResolvers.OAUTH2_ACCESS_TOKEN_REQUEST_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                        AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.OAUTH2_ACCESS_TOKEN_REQUEST_RESOURCE_RESOLVER,
                    new OAuth20AccessTokenGrantRequestAuditResourceResolver());

                plan.registerAuditActionResolver(AuditActionResolvers.OAUTH2_ACCESS_TOKEN_RESPONSE_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                        AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.OAUTH2_ACCESS_TOKEN_RESPONSE_RESOURCE_RESOLVER,
                    new OAuth20AccessTokenResponseAuditResourceResolver());

                plan.registerAuditActionResolver(AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                        AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER,
                    new OAuth20AuthorizationResponseAuditResourceResolver());
            };
        }
    }
}
