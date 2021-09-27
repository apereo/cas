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
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
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
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenGrantRequestAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenResponseAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20CodeResponseAuditResourceResolver;
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
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
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
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.JEESessionStore;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This this {@link CasOAuth20Configuration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration(value = "casOAuth20Configuration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasOAuth20Configuration {

    @Configuration(value = "CasOAuth20ContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ContextConfiguration {

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @Autowired
        public OAuth20ConfigurationContext oauth20ConfigurationContext(
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("defaultDeviceUserCodeFactory")
            final OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("defaultDeviceTokenFactory")
            final OAuth20DeviceTokenFactory defaultDeviceTokenFactory,
            @Qualifier("centralAuthenticationService")
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("oAuth2UserProfileDataCreator")
            final OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator,
            @Qualifier("oauthDistributedSessionCookieGenerator")
            final CasCookieBuilder oauthDistributedSessionCookieGenerator,
            @Qualifier("oauthUserProfileViewRenderer")
            final OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
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
            @Qualifier("defaultAccessTokenFactory")
            final OAuth20AccessTokenFactory defaultAccessTokenFactory,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig,
            @Qualifier("defaultOAuthCodeFactory")
            final OAuth20CodeFactory defaultOAuthCodeFactory,
            @Qualifier("oauthTokenRequestValidators")
            final Collection<OAuth20TokenRequestValidator> oauthTokenRequestValidators,
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
            @Qualifier("oauthAuthorizationResponseBuilders")
            final Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders,
            @Qualifier("oauthAuthorizationRequestValidators")
            final Set<OAuth20AuthorizationRequestValidator> oauthAuthorizationRequestValidators,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator) {
            return OAuth20ConfigurationContext.builder()
                .applicationContext(applicationContext)
                .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor)
                .sessionStore(oauthDistributedSessionStore)
                .servicesManager(servicesManager)
                .ticketRegistry(ticketRegistry)
                .accessTokenFactory(defaultAccessTokenFactory)
                .deviceTokenFactory(defaultDeviceTokenFactory)
                .deviceUserCodeFactory(defaultDeviceUserCodeFactory)
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
                .oAuthCodeFactory(defaultOAuthCodeFactory)
                .consentApprovalViewResolver(consentApprovalViewResolver)
                .authenticationBuilder(oauthCasAuthenticationBuilder)
                .oauthAuthorizationResponseBuilders(oauthAuthorizationResponseBuilders)
                .oauthInvalidAuthorizationResponseBuilder(oauthInvalidAuthorizationBuilder)
                .oauthRequestValidators(oauthAuthorizationRequestValidators)
                .build();
        }

    }

    @Configuration(value = "CasOAuth20WebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20WebConfiguration {

        @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
        @Bean
        @RefreshScope
        @Autowired
        public ConsentApprovalViewResolver consentApprovalViewResolver(
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ConsentApprovalViewResolver(casProperties, oauthDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = "oAuth2UserProfileDataCreator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator(
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter) {
            return new DefaultOAuth20UserProfileDataCreator(servicesManager, profileScopeToAttributesFilter);
        }

    }

    @Configuration(value = "CasOAuth20TokenGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20TokenGeneratorConfiguration {
        @ConditionalOnMissingBean(name = "oauthTokenGenerator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20TokenGenerator oauthTokenGenerator(
            @Qualifier("defaultDeviceUserCodeFactory")
            final OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory,
            @Qualifier("defaultDeviceTokenFactory")
            final OAuth20DeviceTokenFactory defaultDeviceTokenFactory,
            @Qualifier("defaultRefreshTokenFactory")
            final OAuth20RefreshTokenFactory defaultRefreshTokenFactory,
            @Qualifier("defaultAccessTokenFactory")
            final OAuth20AccessTokenFactory defaultAccessTokenFactory,
            @Qualifier("centralAuthenticationService")
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultTokenGenerator(
                defaultAccessTokenFactory, defaultDeviceTokenFactory,
                defaultDeviceUserCodeFactory, defaultRefreshTokenFactory,
                centralAuthenticationService, casProperties);
        }


        @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultAccessTokenResponseGenerator(accessTokenJwtBuilder, casProperties);
        }

        @ConditionalOnMissingBean(name = "accessTokenJwtBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public JwtBuilder accessTokenJwtBuilder(
            @Qualifier("oauthRegisteredServiceJwtAccessTokenCipherExecutor")
            final RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor,
            @Qualifier("oauthAccessTokenJwtCipherExecutor")
            final CipherExecutor oauthAccessTokenJwtCipherExecutor,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20JwtBuilder(
                oauthAccessTokenJwtCipherExecutor,
                servicesManager,
                oauthRegisteredServiceJwtAccessTokenCipherExecutor);
        }

    }

    @Configuration(value = "CasOAuth20ClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecConfigClients")
        @RefreshScope
        @Autowired
        public List<Client> oauthSecConfigClients(
            @Qualifier("oauthCasClientRedirectActionBuilder")
            final OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder,
            @Qualifier("casCallbackUrlResolver")
            final UrlResolver casCallbackUrlResolver,
            @Qualifier("oAuthUserAuthenticator")
            final Authenticator oAuthUserAuthenticator,
            @Qualifier("oAuthRefreshTokenAuthenticator")
            final Authenticator oAuthRefreshTokenAuthenticator,
            @Qualifier("oAuthClientAuthenticator")
            final Authenticator oAuthClientAuthenticator,
            @Qualifier("oAuthProofKeyCodeExchangeAuthenticator")
            final Authenticator oAuthProofKeyCodeExchangeAuthenticator,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("oAuthAccessTokenAuthenticator")
            final Authenticator oAuthAccessTokenAuthenticator,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("centralAuthenticationService")
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {

            val server = casProperties.getServer();
            val cfg = new CasConfiguration(server.getLoginUrl());
            val validator = new InternalTicketValidator(centralAuthenticationService,
                webApplicationServiceFactory, authenticationAttributeReleasePolicy, servicesManager);
            cfg.setDefaultTicketValidator(validator);

            val oauthCasClient = new CasClient(cfg);
            oauthCasClient.setRedirectionActionBuilder((webContext, sessionStore) ->
                oauthCasClientRedirectActionBuilder.build(oauthCasClient, webContext));
            oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
            oauthCasClient.setUrlResolver(casCallbackUrlResolver);
            oauthCasClient.setCallbackUrl(OAuth20Utils.casOAuthCallbackUrl(server.getPrefix()));
            oauthCasClient.setCheckAuthenticationAttempt(false);
            oauthCasClient.init();

            val basicAuthClient = new DirectBasicAuthClient(oAuthClientAuthenticator);
            basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
            basicAuthClient.init();

            val directFormClient = new DirectFormClient(oAuthClientAuthenticator);
            directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
            directFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            directFormClient.setPasswordParameter(OAuth20Constants.CLIENT_SECRET);
            directFormClient.init();

            val pkceAuthnFormClient = new DirectFormClient(oAuthProofKeyCodeExchangeAuthenticator);
            pkceAuthnFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM_PROOF_KEY_CODE_EXCHANGE_AUTHN);
            pkceAuthnFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            pkceAuthnFormClient.setPasswordParameter(OAuth20Constants.CODE_VERIFIER);
            pkceAuthnFormClient.init();

            val pkceBasicAuthClient = new DirectBasicAuthClient(oAuthProofKeyCodeExchangeAuthenticator);
            pkceBasicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_PROOF_KEY_CODE_EXCHANGE_AUTHN);
            pkceBasicAuthClient.init();

            val refreshTokenFormClient = new DirectFormClient(oAuthRefreshTokenAuthenticator);
            refreshTokenFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_FORM_REFRESH_TOKEN_AUTHN);
            refreshTokenFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
            refreshTokenFormClient.setPasswordParameter(OAuth20Constants.REFRESH_TOKEN);
            refreshTokenFormClient.init();

            val userFormClient = new DirectFormClient(oAuthUserAuthenticator);
            userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
            userFormClient.init();

            val accessTokenClient = new HeaderClient();
            accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
            accessTokenClient.setAuthenticator(oAuthAccessTokenAuthenticator);
            accessTokenClient.setName(Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN);
            accessTokenClient.init();

            val clientList = new ArrayList<Client>();

            val beans = applicationContext.getBeansOfType(OAuth20AuthenticationClientProvider.class, false, true);
            val providers = new ArrayList<>(beans.values());
            AnnotationAwareOrderComparator.sort(providers);

            providers.forEach(p -> clientList.add(p.createClient()));

            clientList.add(oauthCasClient);
            clientList.add(basicAuthClient);
            clientList.add(pkceAuthnFormClient);
            clientList.add(pkceBasicAuthClient);
            clientList.add(refreshTokenFormClient);
            clientList.add(directFormClient);
            clientList.add(userFormClient);
            clientList.add(accessTokenClient);
            return clientList;
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecConfig")
        @RefreshScope
        @Autowired
        public Config oauthSecConfig(
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("oauthSecCsrfTokenMatcher")
            final Matcher oauthSecCsrfTokenMatcher,
            @Qualifier("oauthSecConfigClients")
            final List<Client> oauthSecConfigClients,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), oauthSecConfigClients);
            config.setSessionStore(oauthDistributedSessionStore);
            config.setMatcher(oauthSecCsrfTokenMatcher);
            Config.setProfileManagerFactory("CASOAuthSecurityProfileManager", (webContext, sessionStore) ->
                new OAuth20ClientIdAwareProfileManager(webContext, config.getSessionStore(), servicesManager));
            return config;
        }
    }

    @Configuration(value = "CasOAuth20ExtractorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ExtractorConfiguration {

        @Bean
        @RefreshScope
        @Autowired
        public Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            val pkceExt = new AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(context);
            val accessTokenAuthorizationCodeGrantRequestExtractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(context);
            val refreshTokenExt = new AccessTokenRefreshTokenGrantRequestExtractor(context);
            val accessTokenPasswordGrantRequestExtractor = new AccessTokenPasswordGrantRequestExtractor(context);
            val accessTokenClientCredentialsGrantRequestExtractor = new AccessTokenClientCredentialsGrantRequestExtractor(context);
            val deviceCodeExt = new AccessTokenDeviceCodeResponseRequestExtractor(context);
            return CollectionUtils.wrapList(pkceExt, accessTokenAuthorizationCodeGrantRequestExtractor, refreshTokenExt,
                deviceCodeExt, accessTokenPasswordGrantRequestExtractor, accessTokenClientCredentialsGrantRequestExtractor);
        }

        @ConditionalOnMissingBean(name = "accessTokenGrantAuditableRequestExtractor")
        @Bean
        @RefreshScope
        @Autowired
        public AuditableExecution accessTokenGrantAuditableRequestExtractor(
            @Qualifier("accessTokenGrantRequestExtractors")
            final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
            return new AccessTokenGrantAuditableRequestExtractor(accessTokenGrantRequestExtractors);
        }
    }

    @Configuration(value = "CasOAuth20CoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20CoreConfiguration {

        @ConditionalOnMissingBean(name = "oauthDistributedSessionStore")
        @Bean
        @Autowired
        public SessionStore oauthDistributedSessionStore(
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory,
            @Qualifier("centralAuthenticationService")
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("oauthDistributedSessionCookieGenerator")
            final CasCookieBuilder oauthDistributedSessionCookieGenerator,
            final CasConfigurationProperties casProperties) {
            val replicate = casProperties.getAuthn().getOauth().isReplicateSessions();
            if (replicate) {
                return new DistributedJEESessionStore(centralAuthenticationService,
                    ticketFactory, oauthDistributedSessionCookieGenerator);
            }
            return JEESessionStore.INSTANCE;
        }

        @ConditionalOnMissingBean(name = "oauthPrincipalFactory")
        @Bean
        @RefreshScope
        public PrincipalFactory oauthPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }


        @Bean
        @RefreshScope
        @Autowired
        public ExpirationPolicyBuilder refreshTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20RefreshTokenExpirationPolicyBuilder(casProperties);
        }


        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "oauthAuthorizationModelAndViewBuilder")
        public OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder() {
            return new DefaultOAuth20AuthorizationModelAndViewBuilder();
        }


        @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer(final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultUserProfileViewRenderer(casProperties.getAuthn().getOauth());
        }

        @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
        @Bean
        public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
            return OAuth20CallbackAuthorizeViewResolver.asDefault();
        }


        @ConditionalOnMissingBean(name = "accessTokenIdGenerator")
        @Bean
        @RefreshScope
        public UniqueTicketIdGenerator accessTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @ConditionalOnMissingBean(name = "deviceTokenIdGenerator")
        @Bean
        @RefreshScope
        public UniqueTicketIdGenerator deviceTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthAuditTrailRecordResolutionPlanConfigurer")
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

                plan.registerAuditActionResolver(AuditActionResolvers.OAUTH2_CODE_RESPONSE_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                        AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.OAUTH2_CODE_RESPONSE_RESOURCE_RESOLVER,
                    new OAuth20CodeResponseAuditResourceResolver());
            };
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "oauthAccessTokenJwtCipherExecutor")
        @Autowired
        public CipherExecutor oauthAccessTokenJwtCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getOauth().getAccessToken().getCrypto();

            val enabled = FunctionUtils.doIf(
                    !crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                    && StringUtils.isNotBlank(crypto.getSigning().getKey()),
                    () -> {
                        LOGGER.warn("Default encryption/signing is not enabled explicitly for OAuth access tokens as JWTs if necessary, "
                                    + "yet signing/encryption keys are defined for operations. CAS will proceed to enable the token encryption/signing functionality.");
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

        @ConditionalOnMissingBean(name = "oauthDistributedSessionCookieGenerator")
        @Bean
        @Autowired
        public CasCookieBuilder oauthDistributedSessionCookieGenerator(final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getSessionReplication().getCookie();
            return CookieUtils.buildCookieRetrievingGenerator(cookie);
        }

        @ConditionalOnMissingBean(name = "profileScopeToAttributesFilter")
        @Bean
        public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
            return new DefaultOAuth20ProfileScopeToAttributesFilter();
        }

        @RefreshScope
        @Bean
        @ConditionalOnMissingBean(name = "oauthRegisteredServiceCipherExecutor")
        @Autowired
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

        @Bean
        @ConditionalOnMissingBean(name = "accessTokenExpirationPolicy")
        @RefreshScope
        @Autowired
        public ExpirationPolicyBuilder accessTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20AccessTokenExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "deviceTokenExpirationPolicy")
        @RefreshScope
        @Autowired
        public ExpirationPolicyBuilder deviceTokenExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20DeviceTokenExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope
        @Autowired
        public ExpirationPolicyBuilder oAuthCodeExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OAuth20CodeExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "oAuthCodeIdGenerator")
        public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "refreshTokenIdGenerator")
        public UniqueTicketIdGenerator refreshTokenIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }


        @ConditionalOnMissingBean(name = "oauthRegisteredServiceJwtAccessTokenCipherExecutor")
        @Bean
        @RefreshScope
        public RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor() {
            return new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor();
        }

        @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
        @Bean
        @RefreshScope
        public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
            return new OAuth20DefaultCasClientRedirectActionBuilder();
        }

        @Bean
        @RefreshScope
        @Autowired
        public UrlResolver casCallbackUrlResolver(final CasConfigurationProperties casProperties) {
            val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            return new OAuth20CasCallbackUrlResolver(callbackUrl);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthSecCsrfTokenMatcher")
        @RefreshScope
        @Autowired
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
        @ConditionalOnMissingBean(name = "oauthAuthorizationRequestValidators")
        @Bean
        @RefreshScope
        @Autowired
        public Set<OAuth20AuthorizationRequestValidator> oauthAuthorizationRequestValidators(
            @Qualifier("oauthIdTokenResponseTypeRequestValidator")
            final OAuth20AuthorizationRequestValidator oauthIdTokenResponseTypeRequestValidator,
            @Qualifier("oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
            final OAuth20AuthorizationRequestValidator oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator,
            @Qualifier("oauthAuthorizationCodeResponseTypeRequestValidator")
            final OAuth20AuthorizationRequestValidator oauthAuthorizationCodeResponseTypeRequestValidator,
            @Qualifier("oauthTokenResponseTypeRequestValidator")
            final OAuth20AuthorizationRequestValidator oauthTokenResponseTypeRequestValidator,
            @Qualifier("oauthIdTokenAndTokenResponseTypeRequestValidator")
            final OAuth20AuthorizationRequestValidator oauthIdTokenAndTokenResponseTypeRequestValidator) {
            val validators = new LinkedHashSet<OAuth20AuthorizationRequestValidator>(6);
            validators.add(oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator);
            validators.add(oauthAuthorizationCodeResponseTypeRequestValidator);
            validators.add(oauthIdTokenResponseTypeRequestValidator);
            validators.add(oauthTokenResponseTypeRequestValidator);
            validators.add(oauthIdTokenAndTokenResponseTypeRequestValidator);
            return validators;
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(oauth20ConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeGrantTypeTokenRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthAuthorizationCodeGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(oauth20ConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthDeviceCodeResponseTypeRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthDeviceCodeResponseTypeRequestValidator(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20DeviceCodeResponseTypeRequestValidator(servicesManager, webApplicationServiceFactory);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthRevocationRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthRevocationRequestValidator(
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20RevocationRequestValidator(servicesManager, oauthDistributedSessionStore);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthRefreshTokenGrantTypeTokenRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthRefreshTokenGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20RefreshTokenGrantTypeTokenRequestValidator(oauth20ConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthPasswordGrantTypeTokenRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthPasswordGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20PasswordGrantTypeTokenRequestValidator(oauth20ConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oauthClientCredentialsGrantTypeTokenRequestValidator")
        @RefreshScope
        @Autowired
        public OAuth20TokenRequestValidator oauthClientCredentialsGrantTypeTokenRequestValidator(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext oauth20ConfigurationContext) {
            return new OAuth20ClientCredentialsGrantTypeTokenRequestValidator(oauth20ConfigurationContext);
        }

        @ConditionalOnMissingBean(name = "oauthTokenRequestValidators")
        @Bean
        @RefreshScope
        @Autowired
        public Collection<OAuth20TokenRequestValidator> oauthTokenRequestValidators(
            @Qualifier("oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator")
            final OAuth20TokenRequestValidator oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator,
            @Qualifier("oauthAuthorizationCodeGrantTypeTokenRequestValidator")
            final OAuth20TokenRequestValidator oauthAuthorizationCodeGrantTypeTokenRequestValidator,
            @Qualifier("oauthDeviceCodeResponseTypeRequestValidator")
            final OAuth20TokenRequestValidator oauthDeviceCodeResponseTypeRequestValidator,
            @Qualifier("oauthRefreshTokenGrantTypeTokenRequestValidator")
            final OAuth20TokenRequestValidator oauthRefreshTokenGrantTypeTokenRequestValidator,
            @Qualifier("oauthPasswordGrantTypeTokenRequestValidator")
            final OAuth20TokenRequestValidator oauthPasswordGrantTypeTokenRequestValidator,
            @Qualifier("oauthClientCredentialsGrantTypeTokenRequestValidator")
            final OAuth20TokenRequestValidator oauthClientCredentialsGrantTypeTokenRequestValidator,
            @Qualifier("oauthRevocationRequestValidator")
            final OAuth20TokenRequestValidator oauthRevocationRequestValidator) {

            val validators = new ArrayList<OAuth20TokenRequestValidator>(6);
            validators.add(oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator);
            validators.add(oauthAuthorizationCodeGrantTypeTokenRequestValidator);
            validators.add(oauthDeviceCodeResponseTypeRequestValidator);
            validators.add(oauthRefreshTokenGrantTypeTokenRequestValidator);
            validators.add(oauthPasswordGrantTypeTokenRequestValidator);
            validators.add(oauthClientCredentialsGrantTypeTokenRequestValidator);
            validators.add(oauthRevocationRequestValidator);
            return validators;
        }

        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseTypeRequestValidator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationRequestValidator oauthAuthorizationCodeResponseTypeRequestValidator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = "oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationRequestValidator oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = "oauthTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationRequestValidator oauthTokenResponseTypeRequestValidator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20TokenResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = "oauthIdTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationRequestValidator oauthIdTokenResponseTypeRequestValidator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20IdTokenResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = "oauthIdTokenAndTokenResponseTypeRequestValidator")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationRequestValidator oauthIdTokenAndTokenResponseTypeRequestValidator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        }
    }

    @Configuration(value = "CasOAuth20TicketsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20TicketsConfiguration {
        @Bean
        @RefreshScope
        @Autowired
        @ConditionalOnMissingBean(name = "defaultRefreshTokenFactory")
        public OAuth20RefreshTokenFactory defaultRefreshTokenFactory(
            @Qualifier("refreshTokenIdGenerator")
            final UniqueTicketIdGenerator refreshTokenIdGenerator,
            @Qualifier("refreshTokenExpirationPolicy")
            final ExpirationPolicyBuilder refreshTokenExpirationPolicy,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20DefaultRefreshTokenFactory(refreshTokenIdGenerator,
                refreshTokenExpirationPolicy, servicesManager);
        }

        @ConditionalOnMissingBean(name = "defaultRefreshTokenFactoryConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public TicketFactoryExecutionPlanConfigurer defaultRefreshTokenFactoryConfigurer(
            @Qualifier("defaultRefreshTokenFactory")
            final OAuth20RefreshTokenFactory defaultRefreshTokenFactory) {
            return () -> defaultRefreshTokenFactory;
        }


        @Bean
        @RefreshScope
        @Autowired
        @ConditionalOnMissingBean(name = "defaultAccessTokenFactory")
        public OAuth20AccessTokenFactory defaultAccessTokenFactory(
            @Qualifier("accessTokenIdGenerator")
            final UniqueTicketIdGenerator accessTokenIdGenerator,
            @Qualifier("accessTokenExpirationPolicy")
            final ExpirationPolicyBuilder accessTokenExpirationPolicy,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder) {
            return new OAuth20DefaultAccessTokenFactory(accessTokenIdGenerator, accessTokenExpirationPolicy, accessTokenJwtBuilder, servicesManager);
        }

        @ConditionalOnMissingBean(name = "defaultAccessTokenFactoryConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public TicketFactoryExecutionPlanConfigurer defaultAccessTokenFactoryConfigurer(
            @Qualifier("defaultAccessTokenFactory")
            final OAuth20AccessTokenFactory defaultAccessTokenFactory) {
            return () -> defaultAccessTokenFactory;
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "defaultDeviceTokenFactory")
        @Autowired
        public OAuth20DeviceTokenFactory defaultDeviceTokenFactory(
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier("deviceTokenIdGenerator")
            final UniqueTicketIdGenerator deviceTokenIdGenerator,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultDeviceTokenFactory(deviceTokenIdGenerator, deviceTokenExpirationPolicy,
                casProperties.getAuthn().getOauth().getDeviceUserCode().getUserCodeLength(), servicesManager);
        }

        @ConditionalOnMissingBean(name = "defaultDeviceTokenFactoryConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public TicketFactoryExecutionPlanConfigurer defaultDeviceTokenFactoryConfigurer(
            @Qualifier("defaultDeviceTokenFactory")
            final OAuth20DeviceTokenFactory defaultDeviceTokenFactory) {
            return () -> defaultDeviceTokenFactory;
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "defaultDeviceUserCodeFactory")
        @Autowired
        public OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory(
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier("deviceTokenIdGenerator")
            final UniqueTicketIdGenerator deviceTokenIdGenerator,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultDeviceUserCodeFactory(deviceTokenIdGenerator, deviceTokenExpirationPolicy,
                casProperties.getAuthn().getOauth().getDeviceUserCode().getUserCodeLength(), servicesManager);
        }

        @ConditionalOnMissingBean(name = "defaultDeviceUserCodeFactoryConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public TicketFactoryExecutionPlanConfigurer defaultDeviceUserCodeFactoryConfigurer(
            @Qualifier("defaultDeviceUserCodeFactory")
            final OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory) {
            return () -> defaultDeviceUserCodeFactory;
        }

        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
        @Autowired
        public OAuth20CodeFactory defaultOAuthCodeFactory(
            @Qualifier("oAuthCodeIdGenerator")
            final UniqueTicketIdGenerator oAuthCodeIdGenerator,
            @Qualifier("oAuthCodeExpirationPolicy")
            final ExpirationPolicyBuilder oAuthCodeExpirationPolicy,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20DefaultOAuthCodeFactory(oAuthCodeIdGenerator, oAuthCodeExpirationPolicy, servicesManager);
        }

        @ConditionalOnMissingBean(name = "defaultOAuthCodeFactoryConfigurer")
        @Bean
        @RefreshScope
        @Autowired
        public TicketFactoryExecutionPlanConfigurer defaultOAuthCodeFactoryConfigurer(
            @Qualifier("defaultOAuthCodeFactory")
            final OAuth20CodeFactory defaultOAuthCodeFactory) {
            return () -> defaultOAuthCodeFactory;
        }
    }

    @Configuration(value = "CasOAuth20ResponseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20ResponseConfiguration {

        @ConditionalOnMissingBean(name = "oauthAuthorizationResponseBuilders")
        @Bean
        @RefreshScope
        @Autowired
        public Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders(
            @Qualifier("oauthAuthorizationCodeResponseBuilder")
            final OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder,
            @Qualifier("oauthTokenResponseBuilder")
            final OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder) {
            val builders = new LinkedHashSet<OAuth20AuthorizationResponseBuilder>(2);
            builders.add(oauthAuthorizationCodeResponseBuilder);
            builders.add(oauthTokenResponseBuilder);
            return builders;
        }


        @ConditionalOnMissingBean(name = "oauthResourceOwnerCredentialsResponseBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder(
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier("accessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ResourceOwnerCredentialsResponseBuilder(servicesManager, casProperties,
                accessTokenResponseGenerator, oauthTokenGenerator, oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthClientCredentialsResponseBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationResponseBuilder oauthClientCredentialsResponseBuilder(
            @Qualifier("accessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20ClientCredentialsResponseBuilder(servicesManager,
                accessTokenResponseGenerator, oauthTokenGenerator, casProperties, oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthTokenResponseBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OAuth20TokenAuthorizationResponseBuilder(servicesManager, casProperties,
                oauthTokenGenerator, accessTokenJwtBuilder, oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder(
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier("defaultOAuthCodeFactory")
            final OAuth20CodeFactory defaultOAuthCodeFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties) {
            return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(servicesManager, casProperties,
                ticketRegistry, defaultOAuthCodeFactory, oauthAuthorizationModelAndViewBuilder);
        }

        @ConditionalOnMissingBean(name = "oauthInvalidAuthorizationBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder(
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            return new OAuth20InvalidAuthorizationResponseBuilder(servicesManager);
        }

    }

    @Configuration(value = "CasOAuth20AuthenticatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20AuthenticatorConfiguration {
        @ConditionalOnMissingBean(name = "oauthCasAuthenticationBuilder")
        @Bean
        @RefreshScope
        @Autowired
        public OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder(
            @Qualifier("oauthPrincipalFactory")
            final PrincipalFactory oauthPrincipalFactory,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties) {
            return new OAuth20DefaultCasAuthenticationBuilder(oauthPrincipalFactory,
                webApplicationServiceFactory,
                profileScopeToAttributesFilter, casProperties);
        }

        @ConditionalOnMissingBean(name = "oAuthClientAuthenticator")
        @Bean
        @RefreshScope
        @Autowired
        public Authenticator oAuthClientAuthenticator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OAuth20ClientIdClientSecretAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                oauthRegisteredServiceCipherExecutor,
                ticketRegistry,
                defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "oAuthProofKeyCodeExchangeAuthenticator")
        @Bean
        @RefreshScope
        @Autowired
        public Authenticator oAuthProofKeyCodeExchangeAuthenticator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OAuth20ProofKeyCodeExchangeAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                oauthRegisteredServiceCipherExecutor,
                defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "oAuthRefreshTokenAuthenticator")
        @Bean
        @RefreshScope
        @Autowired
        public Authenticator oAuthRefreshTokenAuthenticator(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OAuth20RefreshTokenAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                oauthRegisteredServiceCipherExecutor,
                defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "oAuthUserAuthenticator")
        @Bean
        @RefreshScope
        @Autowired
        public Authenticator oAuthUserAuthenticator(
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("defaultAuthenticationSystemSupport")
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OAuth20UsernamePasswordAuthenticator(
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                oauthRegisteredServiceCipherExecutor,
                oauthDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = "oAuthAccessTokenAuthenticator")
        @Bean
        @RefreshScope
        @Autowired
        public Authenticator oAuthAccessTokenAuthenticator(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("ticketRegistry")
            final TicketRegistry ticketRegistry) {
            return new OAuth20AccessTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
        }

    }
}
