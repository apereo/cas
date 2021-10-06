package org.apereo.cas.oidc.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcCasCallbackUrlResolver;
import org.apereo.cas.oidc.authn.OidcClientConfigurationAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcClientSecretJwtAuthenticator;
import org.apereo.cas.oidc.authn.OidcPrivateKeyJwtAuthenticator;
import org.apereo.cas.oidc.claims.OidcIdTokenClaimCollector;
import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.claims.mapping.OidcDefaultAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactory;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepository;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcEchoingWebFingerUserInfoRepository;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcGroovyWebFingerUserInfoRepository;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcRestfulWebFingerUserInfoRepository;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequestSerializer;
import org.apereo.cas.oidc.issuer.OidcDefaultIssuerService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcRegisteredServiceJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheExpirationPolicy;
import org.apereo.cas.oidc.profile.OidcProfileScopeToAttributesFilter;
import org.apereo.cas.oidc.profile.OidcUserProfileDataCreator;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionService;
import org.apereo.cas.oidc.profile.OidcUserProfileViewRenderer;
import org.apereo.cas.oidc.scopes.DefaultOidcAttributeReleasePolicyFactory;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.oidc.services.OidcServiceRegistryListener;
import org.apereo.cas.oidc.services.OidcServicesManagerRegisteredServiceLocator;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionService;
import org.apereo.cas.oidc.token.OidcJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.oidc.web.OidcAuthenticationAuthorizeSecurityLogic;
import org.apereo.cas.oidc.web.OidcCasClientRedirectActionBuilder;
import org.apereo.cas.oidc.web.OidcConsentApprovalViewResolver;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20InvalidAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OidcConfiguration {

    @Autowired
    @Qualifier("oidcAuthorizationResponseBuilders")
    private ObjectProvider<Set<OAuth20AuthorizationResponseBuilder>> oidcAuthorizationResponseBuilders;

    @Autowired
    @Qualifier("oauthRegisteredServiceCipherExecutor")
    private ObjectProvider<CipherExecutor> oauthRegisteredServiceCipherExecutor;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    private ObjectProvider<SessionStore> oauthDistributedSessionStore;

    @Autowired
    @Qualifier("oidcDefaultJsonWebKeystoreCache")
    private ObjectProvider<LoadingCache<String, Optional<PublicJsonWebKey>>> oidcDefaultJsonWebKeystoreCache;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("oauthAuthorizationRequestValidators")
    private ObjectProvider<Set<OAuth20AuthorizationRequestValidator>> oauthRequestValidators;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    @Qualifier("deviceTokenExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> deviceTokenExpirationPolicy;

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private ObjectProvider<OAuth20CasAuthenticationBuilder> authenticationBuilder;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    private ObjectProvider<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    private ObjectProvider<OAuth20DeviceTokenFactory> defaultDeviceTokenFactory;

    @Autowired
    @Qualifier("defaultDeviceUserCodeFactory")
    private ObjectProvider<OAuth20DeviceUserCodeFactory> defaultDeviceUserCodeFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("callbackAuthorizeViewResolver")
    private ObjectProvider<OAuth20CallbackAuthorizeViewResolver> callbackAuthorizeViewResolver;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private ObjectProvider<OAuth20CodeFactory> defaultOAuthCodeFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("oidcAccessTokenResponseGenerator")
    private ObjectProvider<OAuth20AccessTokenResponseGenerator> oidcAccessTokenResponseGenerator;

    @Autowired
    @Qualifier("oauthTokenRequestValidators")
    private ObjectProvider<Collection<OAuth20TokenRequestValidator>> oauthTokenRequestValidators;

    @Autowired
    @Qualifier("oauthDistributedSessionCookieGenerator")
    private ObjectProvider<CasCookieBuilder> oauthDistributedSessionCookieGenerator;

    @Autowired
    @Qualifier("oauthInvalidAuthorizationBuilder")
    private ObjectProvider<OAuth20InvalidAuthorizationResponseBuilder> oauthInvalidAuthorizationBuilder;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private ObjectProvider<OAuth20AccessTokenFactory> defaultAccessTokenFactory;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    private ObjectProvider<OAuth20TokenGenerator> oauthTokenGenerator;

    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OidcConsentApprovalViewResolver(casProperties, oauthDistributedSessionStore.getObject());
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcRequestSupport());
    }

    @Bean
    public HandlerInterceptor requiresAuthenticationAuthorizeInterceptor() {
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(),
            Authenticators.CAS_OAUTH_CLIENT, JEEHttpActionAdapter.INSTANCE);
        interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        interceptor.setSecurityLogic(new OidcAuthenticationAuthorizeSecurityLogic());
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcCasClientRedirectActionBuilder")
    @RefreshScope
    public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcRequestSupport());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcRequestSupport")
    @RefreshScope
    public OidcRequestSupport oidcRequestSupport() {
        return new OidcRequestSupport(ticketGrantingTicketCookieGenerator.getObject(),
            ticketRegistrySupport.getObject(), oidcIssuerService());
    }

    @ConditionalOnMissingBean(name = "oidcPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory oidcPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = OidcAttributeToScopeClaimMapper.DEFAULT_BEAN_NAME)
    public OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper() {
        val mappings = casProperties.getAuthn().getOidc().getCore().getClaimsMap();
        return new OidcDefaultAttributeToScopeClaimMapper(mappings);
    }

    @Bean
    @RefreshScope
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory(), casProperties, oidcAttributeReleasePolicyFactory());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServiceRegistryListener")
    public ServiceRegistryListener oidcServiceRegistryListener() {
        return new OidcServiceRegistryListener(oidcAttributeReleasePolicyFactory());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAttributeReleasePolicyFactory")
    public OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory() {
        return new DefaultOidcAttributeReleasePolicyFactory(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServicesManagerRegisteredServiceLocator")
    public ServicesManagerRegisteredServiceLocator oidcServicesManagerRegisteredServiceLocator() {
        return new OidcServicesManagerRegisteredServiceLocator();
    }

    @ConditionalOnMissingBean(name = "clientRegistrationRequestSerializer")
    @Bean
    @RefreshScope
    public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer() {
        return new OidcClientRegistrationRequestSerializer();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcWebFingerDiscoveryService")
    public OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService() throws Exception {
        return new OidcWebFingerDiscoveryService(oidcWebFingerUserInfoRepository(),
            oidcServerDiscoverySettingsFactory().getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcWebFingerUserInfoRepository")
    public OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository() {
        val userInfo = casProperties.getAuthn().getOidc().getWebfinger().getUserInfo();

        if (userInfo.getGroovy().getLocation() != null) {
            return new OidcGroovyWebFingerUserInfoRepository(userInfo.getGroovy().getLocation());
        }

        if (StringUtils.isNotBlank(userInfo.getRest().getUrl())) {
            return new OidcRestfulWebFingerUserInfoRepository(userInfo.getRest());
        }

        LOGGER.info("Using [{}] to locate webfinger resources, which is NOT appropriate for production purposes, "
            + "as it will always echo back the given username/email address and is only useful for testing/demo purposes. "
            + "Consider choosing and configuring a different repository implementation for locating and fetching user information "
            + "for webfinger resources, etc.", OidcEchoingWebFingerUserInfoRepository.class.getSimpleName());
        return new OidcEchoingWebFingerUserInfoRepository();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcUserProfileDataCreator")
    @RefreshScope
    public OAuth20UserProfileDataCreator oidcUserProfileDataCreator() {
        return new OidcUserProfileDataCreator(servicesManager.getObject(), profileScopeToAttributesFilter());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcTokenSigningAndEncryptionService")
    public OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService() throws Exception {
        return new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache.getObject(),
            oidcServiceJsonWebKeystoreCache(),
            oidcIssuerService(),
            oidcServerDiscoverySettingsFactory().getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcUserProfileSigningAndEncryptionService")
    public OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService() throws Exception {
        return new OidcUserProfileSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache.getObject(),
            oidcServiceJsonWebKeystoreCache(),
            oidcIssuerService(),
            oidcServerDiscoverySettingsFactory().getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServiceJsonWebKeystoreCache")
    @RefreshScope
    public LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> oidcServiceJsonWebKeystoreCache() {
        return Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfter(new OidcServiceJsonWebKeystoreCacheExpirationPolicy(casProperties))
            .build(oidcServiceJsonWebKeystoreCacheLoader());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServiceJsonWebKeystoreCacheLoader")
    public CacheLoader<OAuthRegisteredService, Optional<PublicJsonWebKey>> oidcServiceJsonWebKeystoreCacheLoader() {
        return new OidcRegisteredServiceJsonWebKeystoreCacheLoader(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServerDiscoverySettingsFactory")
    public FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory() {
        return new OidcServerDiscoverySettingsFactory(casProperties, oidcIssuerService(), applicationContext);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcRegisteredServiceJwtAccessTokenCipherExecutor")
    public RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor() {
        return new OidcRegisteredServiceJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache.getObject(),
            oidcServiceJsonWebKeystoreCache(), oidcIssuerService());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAccessTokenJwtCipherExecutor")
    public CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor() {
        return new OidcJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache.getObject(), oidcIssuerService());
    }

    @Bean
    public OAuth20AuthenticationClientProvider oidcClientConfigurationAuthenticationClientProvider() {
        return () -> {
            val accessTokenClient = new HeaderClient();
            accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
            accessTokenClient.setAuthenticator(new OidcClientConfigurationAccessTokenAuthenticator(ticketRegistry.getObject(),
                accessTokenJwtBuilder()));
            accessTokenClient.setName(OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
            accessTokenClient.init();
            return accessTokenClient;
        };
    }

    @Bean
    public OAuth20AuthenticationClientProvider oidcPrivateKeyJwtClientProvider() {
        return () -> {
            val privateKeyJwtClient = new DirectFormClient(new OidcPrivateKeyJwtAuthenticator(
                servicesManager.getObject(),
                registeredServiceAccessStrategyEnforcer.getObject(),
                ticketRegistry.getObject(),
                webApplicationServiceFactory.getObject(),
                casProperties,
                applicationContext));
            privateKeyJwtClient.setName(OidcConstants.CAS_OAUTH_CLIENT_PRIVATE_KEY_JWT_AUTHN);
            privateKeyJwtClient.setUsernameParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE);
            privateKeyJwtClient.setPasswordParameter(OAuth20Constants.CLIENT_ASSERTION);
            privateKeyJwtClient.init();
            return privateKeyJwtClient;
        };
    }

    @Bean
    public OAuth20AuthenticationClientProvider oidcClientSecretJwtClientProvider() {
        return () -> {
            val client = new DirectFormClient(new OidcClientSecretJwtAuthenticator(
                servicesManager.getObject(),
                registeredServiceAccessStrategyEnforcer.getObject(),
                ticketRegistry.getObject(),
                webApplicationServiceFactory.getObject(),
                casProperties,
                applicationContext));
            client.setName(OidcConstants.CAS_OAUTH_CLIENT_CLIENT_SECRET_JWT_AUTHN);
            client.setUsernameParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE);
            client.setPasswordParameter(OAuth20Constants.CLIENT_ASSERTION);
            client.init();
            return client;
        };
    }

    @Bean
    public Authenticator oAuthAccessTokenAuthenticator() throws Exception {
        return new OidcAccessTokenAuthenticator(ticketRegistry.getObject(),
            oidcTokenSigningAndEncryptionService(), servicesManager.getObject(),
            accessTokenJwtBuilder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcCasCallbackUrlResolver")
    public UrlResolver casCallbackUrlResolver() {
        return new OidcCasCallbackUrlResolver(casProperties);
    }

    @ConditionalOnMissingBean(name = "oidcUserProfileViewRenderer")
    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer() throws Exception {
        return new OidcUserProfileViewRenderer(casProperties.getAuthn().getOauth(),
            servicesManager.getObject(),
            oidcUserProfileSigningAndEncryptionService());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAccessTokenJwtBuilder")
    public JwtBuilder accessTokenJwtBuilder() {
        return new OAuth20JwtBuilder(
            oidcAccessTokenJwtCipherExecutor(),
            servicesManager.getObject(),
            oidcRegisteredServiceJwtAccessTokenCipherExecutor());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcIdTokenClaimCollector")
    public OidcIdTokenClaimCollector oidcIdTokenClaimCollector() {
        return OidcIdTokenClaimCollector.defaultCollector();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcIssuerService")
    public OidcIssuerService oidcIssuerService() {
        return new OidcDefaultIssuerService(casProperties.getAuthn().getOidc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcConfigurationContext")
    @SneakyThrows
    public OidcConfigurationContext oidcConfigurationContext() {
        return (OidcConfigurationContext) OidcConfigurationContext.builder()
            .idTokenClaimCollector(oidcIdTokenClaimCollector())
            .oidcRequestSupport(oidcRequestSupport())
            .issuerService(oidcIssuerService())
            .attributeToScopeClaimMapper(oidcAttributeToScopeClaimMapper())
            .applicationContext(applicationContext)
            .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor.getObject())
            .sessionStore(oauthDistributedSessionStore.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .accessTokenFactory(defaultAccessTokenFactory.getObject())
            .deviceTokenFactory(defaultDeviceTokenFactory.getObject())
            .deviceUserCodeFactory(defaultDeviceUserCodeFactory.getObject())
            .clientRegistrationRequestSerializer(clientRegistrationRequestSerializer())
            .clientIdGenerator(new DefaultRandomStringGenerator())
            .clientSecretGenerator(new DefaultRandomStringGenerator())
            .principalFactory(oidcPrincipalFactory())
            .webApplicationServiceServiceFactory(webApplicationServiceFactory.getObject())
            .casProperties(casProperties)
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator.getObject())
            .oauthConfig(oauthSecConfig.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .callbackAuthorizeViewResolver(callbackAuthorizeViewResolver.getObject())
            .profileScopeToAttributesFilter(profileScopeToAttributesFilter())
            .accessTokenGenerator(oauthTokenGenerator.getObject())
            .accessTokenResponseGenerator(oidcAccessTokenResponseGenerator.getObject())
            .deviceTokenExpirationPolicy(deviceTokenExpirationPolicy.getObject())
            .accessTokenGrantRequestValidators(oauthTokenRequestValidators.getObject())
            .userProfileDataCreator(oidcUserProfileDataCreator())
            .userProfileViewRenderer(oidcUserProfileViewRenderer())
            .oAuthCodeFactory(defaultOAuthCodeFactory.getObject())
            .consentApprovalViewResolver(consentApprovalViewResolver())
            .authenticationBuilder(authenticationBuilder.getObject())
            .oauthAuthorizationResponseBuilders(oidcAuthorizationResponseBuilders.getObject())
            .oauthInvalidAuthorizationResponseBuilder(oauthInvalidAuthorizationBuilder.getObject())
            .oauthRequestValidators(oauthRequestValidators.getObject())
            .singleLogoutServiceLogoutUrlBuilder(singleLogoutServiceLogoutUrlBuilder.getObject())
            .idTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder())
            .build();
    }
}
