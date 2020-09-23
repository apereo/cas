package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.integration.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20ProofKeyCodeExchangeAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20RefreshTokenAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20UsernamePasswordAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuthAuthenticationClientProvider;
import org.apereo.cas.support.oauth.profile.CasServerApiBasedTicketValidator;
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
import org.apereo.cas.support.oauth.web.audit.AccessTokenResponseAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenGrantRequestAuditResourceResolver;
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
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.code.OAuth20CodeExpirationPolicyBuilder;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenExpirationPolicyBuilder;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
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
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.core.io.ResourceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
@Configuration("casOAuth20Configuration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasOAuth20Configuration {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("oauthDistributedSessionCookieGenerator")
    private ObjectProvider<CasCookieBuilder> oauthDistributedSessionCookieGenerator;

    @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
    @Bean
    @RefreshScope
    public OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OAuth20DefaultAccessTokenResponseGenerator(accessTokenJwtBuilder(), casProperties);
    }

    @ConditionalOnMissingBean(name = "accessTokenJwtBuilder")
    @Bean
    @RefreshScope
    public JwtBuilder accessTokenJwtBuilder() {
        return new OAuth20JwtBuilder(casProperties.getServer().getPrefix(),
            oauthAccessTokenJwtCipherExecutor(),
            servicesManager.getObject(),
            oauthRegisteredServiceJwtAccessTokenCipherExecutor());
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
    public UrlResolver casCallbackUrlResolver() {
        return new OAuth20CasCallbackUrlResolver(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()));
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthSecConfig")
    @RefreshScope
    public Config oauthSecConfig() {
        val clientList = oauthSecConfigClients();
        val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), clientList);
        config.setSessionStore(oauthDistributedSessionStore());
        Config.setProfileManagerFactory("CASOAuthSecurityProfileManager", webContext ->
            new OAuth20ClientIdAwareProfileManager(webContext, config.getSessionStore(), servicesManager.getObject()));
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthSecConfigClients")
    @RefreshScope
    public List<Client> oauthSecConfigClients() {
        val server = casProperties.getServer();

        val cfg = new CasConfiguration(server.getLoginUrl());
        cfg.setDefaultTicketValidator(new CasServerApiBasedTicketValidator(centralAuthenticationService.getObject()));

        val oauthCasClient = new CasClient(cfg);
        oauthCasClient.setRedirectionActionBuilder(webContext ->
            oauthCasClientRedirectActionBuilder().build(oauthCasClient, webContext));
        oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
        oauthCasClient.setUrlResolver(casCallbackUrlResolver());
        oauthCasClient.setCallbackUrl(OAuth20Utils.casOAuthCallbackUrl(server.getPrefix()));
        oauthCasClient.init();

        val authenticator = oAuthClientAuthenticator();
        val basicAuthClient = new DirectBasicAuthClient(authenticator);
        basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        basicAuthClient.init();

        val directFormClient = new DirectFormClient(authenticator);
        directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
        directFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
        directFormClient.setPasswordParameter(OAuth20Constants.CLIENT_SECRET);
        directFormClient.init();

        val pkceAuthenticator = oAuthProofKeyCodeExchangeAuthenticator();
        val pkceAuthnFormClient = new DirectFormClient(pkceAuthenticator);
        pkceAuthnFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM_PROOF_KEY_CODE_EXCHANGE_AUTHN);
        pkceAuthnFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
        pkceAuthnFormClient.setPasswordParameter(OAuth20Constants.CODE_VERIFIER);
        pkceAuthnFormClient.init();

        val pkceBasicAuthClient = new DirectBasicAuthClient(pkceAuthenticator);
        pkceBasicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_PROOF_KEY_CODE_EXCHANGE_AUTHN);
        pkceBasicAuthClient.init();

        val refreshTokenFormClient = new DirectFormClient(oAuthRefreshTokenAuthenticator());
        refreshTokenFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_FORM_REFRESH_TOKEN_AUTHN);
        refreshTokenFormClient.setUsernameParameter(OAuth20Constants.CLIENT_ID);
        refreshTokenFormClient.setPasswordParameter(OAuth20Constants.REFRESH_TOKEN);
        refreshTokenFormClient.init();

        val userFormClient = new DirectFormClient(oAuthUserAuthenticator());
        userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
        userFormClient.init();

        val accessTokenClient = new HeaderClient();
        accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
        accessTokenClient.setAuthenticator(oAuthAccessTokenAuthenticator());
        accessTokenClient.setName(Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN);
        accessTokenClient.init();

        val clientList = new ArrayList<Client>();

        val beans = applicationContext.getBeansOfType(OAuthAuthenticationClientProvider.class, false, true);
        val providers = new ArrayList<OAuthAuthenticationClientProvider>(beans.values());
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

    @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
    @Bean
    @RefreshScope
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OAuth20ConsentApprovalViewResolver(casProperties);
    }

    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
        };
    }

    @ConditionalOnMissingBean(name = "oAuthClientAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthClientAuthenticator() {
        return new OAuth20ClientIdClientSecretAuthenticator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            oauthRegisteredServiceCipherExecutor(),
            ticketRegistry.getObject(),
            defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "oAuthProofKeyCodeExchangeAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthProofKeyCodeExchangeAuthenticator() {
        return new OAuth20ProofKeyCodeExchangeAuthenticator(this.servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            ticketRegistry.getObject(),
            oauthRegisteredServiceCipherExecutor(),
            defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "oAuthRefreshTokenAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthRefreshTokenAuthenticator() {
        return new OAuth20RefreshTokenAuthenticator(this.servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            registeredServiceAccessStrategyEnforcer.getObject(),
            ticketRegistry.getObject(),
            oauthRegisteredServiceCipherExecutor(),
            defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "oAuthUserAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthUserAuthenticator() {
        return new OAuth20UsernamePasswordAuthenticator(authenticationSystemSupport.getObject(),
            servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            oauthRegisteredServiceCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "oAuthAccessTokenAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<TokenCredentials> oAuthAccessTokenAuthenticator() {
        return new OAuth20AccessTokenAuthenticator(ticketRegistry.getObject(), accessTokenJwtBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultAccessTokenFactory")
    public OAuth20AccessTokenFactory defaultAccessTokenFactory() {
        return new OAuth20DefaultAccessTokenFactory(accessTokenIdGenerator(),
            accessTokenExpirationPolicy(),
            accessTokenJwtBuilder(),
            servicesManager.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultDeviceTokenFactory")
    public OAuth20DeviceTokenFactory defaultDeviceTokenFactory() {
        return new OAuth20DefaultDeviceTokenFactory(deviceTokenIdGenerator(), deviceTokenExpirationPolicy(),
            casProperties.getAuthn().getOauth().getDeviceUserCode().getUserCodeLength(),
            servicesManager.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "accessTokenExpirationPolicy")
    @RefreshScope
    public ExpirationPolicyBuilder accessTokenExpirationPolicy() {
        return new OAuth20AccessTokenExpirationPolicyBuilder(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "deviceTokenExpirationPolicy")
    @RefreshScope
    public ExpirationPolicyBuilder deviceTokenExpirationPolicy() {
        return new OAuth20DeviceTokenExpirationPolicyBuilder(casProperties);
    }

    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder oAuthCodeExpirationPolicy() {
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

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
    public OAuth20CodeFactory defaultOAuthCodeFactory() {
        return new OAuth20DefaultOAuthCodeFactory(oAuthCodeIdGenerator(),
            oAuthCodeExpirationPolicy(), servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "profileScopeToAttributesFilter")
    @Bean
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new DefaultOAuth20ProfileScopeToAttributesFilter();
    }


    @ConditionalOnMissingBean(name = "oauthTokenGenerator")
    @Bean
    @RefreshScope
    public OAuth20TokenGenerator oauthTokenGenerator() {
        return new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory(),
            defaultDeviceTokenFactory(),
            defaultRefreshTokenFactory(),
            centralAuthenticationService.getObject(),
            casProperties);
    }

    @Bean
    public Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors() {
        val context = oauth20ConfigurationContext();
        val pkceExt = new AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(context);
        val authzCodeExt = new AccessTokenAuthorizationCodeGrantRequestExtractor(context);
        val refreshTokenExt = new AccessTokenRefreshTokenGrantRequestExtractor(context);
        val pswExt = new AccessTokenPasswordGrantRequestExtractor(context);
        val credsExt = new AccessTokenClientCredentialsGrantRequestExtractor(context);
        val deviceCodeExt = new AccessTokenDeviceCodeResponseRequestExtractor(context);
        return CollectionUtils.wrapList(pkceExt, authzCodeExt, refreshTokenExt, deviceCodeExt, pswExt, credsExt);
    }

    @ConditionalOnMissingBean(name = "accessTokenGrantAuditableRequestExtractor")
    @Bean
    @RefreshScope
    public AuditableExecution accessTokenGrantAuditableRequestExtractor() {
        return new AccessTokenGrantAuditableRequestExtractor(accessTokenGrantRequestExtractors());
    }


    @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer() {
        return new OAuth20DefaultUserProfileViewRenderer(casProperties.getAuthn().getOauth());
    }

    @ConditionalOnMissingBean(name = "oAuth2UserProfileDataCreator")
    @Bean
    @RefreshScope
    public OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator() {
        return new DefaultOAuth20UserProfileDataCreator(servicesManager.getObject(),
            profileScopeToAttributesFilter());
    }


    @ConditionalOnMissingBean(name = "oauthAuthorizationResponseBuilders")
    @Bean
    @RefreshScope
    public Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders() {
        val builders = applicationContext.getBeansOfType(OAuth20AuthorizationResponseBuilder.class, false, true);
        return new HashSet<>(builders.values());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationRequestValidators")
    @Bean
    @RefreshScope
    public Set<OAuth20AuthorizationRequestValidator> oauthAuthorizationRequestValidators() {
        val validators = new LinkedHashSet<OAuth20AuthorizationRequestValidator>(5);
        validators.add(oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator());
        validators.add(oauthAuthorizationCodeResponseTypeRequestValidator());
        validators.add(oauthIdTokenResponseTypeRequestValidator());
        validators.add(oauthTokenResponseTypeRequestValidator());
        validators.add(oauthIdTokenAndTokenResponseTypeRequestValidator());
        return validators;
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator() {
        val context = oauth20ConfigurationContext();
        return new OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeGrantTypeTokenRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthAuthorizationCodeGrantTypeTokenRequestValidator() {
        val context = oauth20ConfigurationContext();
        return new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthDeviceCodeResponseTypeRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthDeviceCodeResponseTypeRequestValidator() {
        val svcManager = servicesManager.getObject();
        return new OAuth20DeviceCodeResponseTypeRequestValidator(svcManager, webApplicationServiceFactory.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthRevocationRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthRevocationRequestValidator() {
        val svcManager = servicesManager.getObject();
        return new OAuth20RevocationRequestValidator(svcManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthRefreshTokenGrantTypeTokenRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthRefreshTokenGrantTypeTokenRequestValidator() {
        val context = oauth20ConfigurationContext();
        return new OAuth20RefreshTokenGrantTypeTokenRequestValidator(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthPasswordGrantTypeTokenRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthPasswordGrantTypeTokenRequestValidator() {
        val context = oauth20ConfigurationContext();
        return new OAuth20PasswordGrantTypeTokenRequestValidator(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthClientCredentialsGrantTypeTokenRequestValidator")
    @RefreshScope
    public OAuth20TokenRequestValidator oauthClientCredentialsGrantTypeTokenRequestValidator() {
        val context = oauth20ConfigurationContext();
        return new OAuth20ClientCredentialsGrantTypeTokenRequestValidator(context);
    }

    @ConditionalOnMissingBean(name = "oauthTokenRequestValidators")
    @Bean
    @RefreshScope
    public Collection<OAuth20TokenRequestValidator> oauthTokenRequestValidators() {
        val validators = new ArrayList<OAuth20TokenRequestValidator>(6);

        validators.add(oauth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator());
        validators.add(oauthAuthorizationCodeGrantTypeTokenRequestValidator());
        validators.add(oauthDeviceCodeResponseTypeRequestValidator());
        validators.add(oauthRefreshTokenGrantTypeTokenRequestValidator());
        validators.add(oauthPasswordGrantTypeTokenRequestValidator());
        validators.add(oauthClientCredentialsGrantTypeTokenRequestValidator());
        validators.add(oauthRevocationRequestValidator());

        return validators;
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthAuthorizationCodeResponseTypeRequestValidator() {
        return new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator() {
        return new OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthTokenResponseTypeRequestValidator() {
        return new OAuth20TokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oauthIdTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthIdTokenResponseTypeRequestValidator() {
        return new OAuth20IdTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oauthIdTokenAndTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthIdTokenAndTokenResponseTypeRequestValidator() {
        return new OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oauthResourceOwnerCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder() {
        return new OAuth20ResourceOwnerCredentialsResponseBuilder(accessTokenResponseGenerator(), oauthTokenGenerator(),
            accessTokenExpirationPolicy(), casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthClientCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthClientCredentialsResponseBuilder() {
        return new OAuth20ClientCredentialsResponseBuilder(accessTokenResponseGenerator(),
            oauthTokenGenerator(), accessTokenExpirationPolicy(), casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder() {
        return new OAuth20TokenAuthorizationResponseBuilder(oauthTokenGenerator(), accessTokenExpirationPolicy(),
            servicesManager.getObject(), accessTokenJwtBuilder(), casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder() {
        return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(ticketRegistry.getObject(),
            defaultOAuthCodeFactory(), servicesManager.getObject());
    }


    @ConditionalOnMissingBean(name = "oauthPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory oauthPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultRefreshTokenFactory")
    public OAuth20RefreshTokenFactory defaultRefreshTokenFactory() {
        return new OAuth20DefaultRefreshTokenFactory(refreshTokenIdGenerator(),
            refreshTokenExpirationPolicy(), servicesManager.getObject());
    }

    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder refreshTokenExpirationPolicy() {
        return new OAuth20RefreshTokenExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthCasAuthenticationBuilder")
    @Bean
    @RefreshScope
    public OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder() {
        return new OAuth20CasAuthenticationBuilder(oauthPrincipalFactory(), webApplicationServiceFactory.getObject(),
            profileScopeToAttributesFilter(), casProperties);
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
            plan.registerAuditActionResolver("OAUTH2_USER_PROFILE_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                    AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
            plan.registerAuditResourceResolver("OAUTH2_USER_PROFILE_RESOURCE_RESOLVER",
                new OAuth20UserProfileDataAuditResourceResolver());

            plan.registerAuditActionResolver("OAUTH2_ACCESS_TOKEN_REQUEST_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                    AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
            plan.registerAuditResourceResolver("OAUTH2_ACCESS_TOKEN_REQUEST_RESOURCE_RESOLVER",
                new OAuth20AccessTokenGrantRequestAuditResourceResolver());

            plan.registerAuditActionResolver("OAUTH2_ACCESS_TOKEN_RESPONSE_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                    AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
            plan.registerAuditResourceResolver("OAUTH2_ACCESS_TOKEN_RESPONSE_RESOURCE_RESOLVER",
                new AccessTokenResponseAuditResourceResolver());
        };
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oauthAccessTokenJwtCipherExecutor")
    public CipherExecutor oauthAccessTokenJwtCipherExecutor() {
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
    public CasCookieBuilder oauthDistributedSessionCookieGenerator() {
        val cookie = casProperties.getSessionReplication().getCookie();
        return CookieUtils.buildCookieRetrievingGenerator(cookie);
    }

    @ConditionalOnMissingBean(name = "oauthDistributedSessionStore")
    @Bean
    public SessionStore<JEEContext> oauthDistributedSessionStore() {
        val replicate = casProperties.getAuthn().getOauth().isReplicateSessions();
        if (replicate) {
            return new DistributedJEESessionStore(centralAuthenticationService.getObject(),
                ticketFactory.getObject(), oauthDistributedSessionCookieGenerator());
        }
        return new JEESessionStore();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oauthRegisteredServiceCipherExecutor")
    public CipherExecutor oauthRegisteredServiceCipherExecutor() {
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
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public OAuth20ConfigurationContext oauth20ConfigurationContext() {
        return OAuth20ConfigurationContext.builder()
            .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor())
            .sessionStore(oauthDistributedSessionStore())
            .servicesManager(servicesManager.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .accessTokenFactory(defaultAccessTokenFactory())
            .deviceTokenFactory(defaultDeviceTokenFactory())
            .principalFactory(oauthPrincipalFactory())
            .webApplicationServiceServiceFactory(webApplicationServiceFactory.getObject())
            .casProperties(casProperties)
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .resourceLoader(resourceLoader)
            .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator.getObject())
            .oauthConfig(oauthSecConfig())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .callbackAuthorizeViewResolver(callbackAuthorizeViewResolver())
            .profileScopeToAttributesFilter(profileScopeToAttributesFilter())
            .accessTokenGenerator(oauthTokenGenerator())
            .accessTokenJwtBuilder(accessTokenJwtBuilder())
            .accessTokenResponseGenerator(accessTokenResponseGenerator())
            .accessTokenExpirationPolicy(accessTokenExpirationPolicy())
            .deviceTokenExpirationPolicy(deviceTokenExpirationPolicy())
            .accessTokenGrantRequestValidators(oauthTokenRequestValidators())
            .userProfileDataCreator(oAuth2UserProfileDataCreator())
            .userProfileViewRenderer(oauthUserProfileViewRenderer())
            .oAuthCodeFactory(defaultOAuthCodeFactory())
            .consentApprovalViewResolver(consentApprovalViewResolver())
            .authenticationBuilder(oauthCasAuthenticationBuilder())
            .oauthAuthorizationResponseBuilders(oauthAuthorizationResponseBuilders())
            .oauthRequestValidators(oauthAuthorizationRequestValidators())
            .build();
    }
}
