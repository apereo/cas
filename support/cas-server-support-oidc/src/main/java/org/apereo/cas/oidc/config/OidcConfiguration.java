package org.apereo.cas.oidc.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcCasCallbackUrlResolver;
import org.apereo.cas.oidc.authn.OidcClientConfigurationAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcDPoPAuthenticator;
import org.apereo.cas.oidc.authn.OidcJwtAuthenticator;
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
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
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
import org.apereo.cas.oidc.ticket.OidcDefaultPushedAuthorizationRequestFactory;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestExpirationPolicyBuilder;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionService;
import org.apereo.cas.oidc.token.OidcJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.oidc.web.OidcAuthenticationAuthorizeSecurityLogic;
import org.apereo.cas.oidc.web.OidcAuthorizationModelAndViewBuilder;
import org.apereo.cas.oidc.web.OidcCallbackAuthorizeViewResolver;
import org.apereo.cas.oidc.web.OidcCasClientRedirectActionBuilder;
import org.apereo.cas.oidc.web.OidcClientSecretValidator;
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
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20InvalidAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@AutoConfiguration
public class OidcConfiguration {

    @Configuration(value = "OidcServicesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcServicesConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oidcServiceRegistryListener")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryListener oidcServiceRegistryListener(
            @Qualifier("oidcAttributeReleasePolicyFactory")
            final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory) {
            return new OidcServiceRegistryListener(oidcAttributeReleasePolicyFactory);
        }
    }

    @Configuration(value = "OidcWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcWebConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ClientSecretValidator oauth20ClientSecretValidator(
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OidcClientSecretValidator(oauthRegisteredServiceCipherExecutor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SecurityLogic oidcAuthorizationSecurityLogic(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            return new OidcAuthenticationAuthorizeSecurityLogic(ticketGrantingTicketCookieGenerator,
                ticketRegistry, oauthRequestParameterResolver);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor requiresAuthenticationAuthorizeInterceptor(
            @Qualifier("oidcAuthorizationSecurityLogic")
            final SecurityLogic oidcAuthorizationSecurityLogic,
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val interceptor = new SecurityInterceptor(oauthSecConfig,
                Authenticators.CAS_OAUTH_CLIENT, JEEHttpActionAdapter.INSTANCE);
            interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
            interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
            interceptor.setSecurityLogic(oidcAuthorizationSecurityLogic);
            return interceptor;
        }
    }

    @Configuration(value = "OidcWebFingerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcWebFingerConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcWebFingerDiscoveryService")
        public OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService(
            @Qualifier("oidcWebFingerUserInfoRepository")
            final OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory) throws Exception {
            return new OidcWebFingerDiscoveryService(oidcWebFingerUserInfoRepository,
                oidcServerDiscoverySettingsFactory.getObject());
        }

    }

    @Configuration(value = "OidcUserProfileConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcUserProfileConfiguration {

        @ConditionalOnMissingBean(name = "oidcUserProfileViewRenderer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer(
            @Qualifier("oidcUserProfileSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) throws Exception {
            return new OidcUserProfileViewRenderer(casProperties.getAuthn().getOauth(),
                servicesManager, oidcUserProfileSigningAndEncryptionService);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcUserProfileDataCreator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileDataCreator oidcUserProfileDataCreator(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcUserProfileDataCreator(oidcConfigurationContext);
        }
    }

    @Configuration(value = "OidcClaimsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcClaimsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter(
            @Qualifier("oidcPrincipalFactory")
            final PrincipalFactory oidcPrincipalFactory,
            @Qualifier("oidcAttributeReleasePolicyFactory")
            final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory,
            final CasConfigurationProperties casProperties) {
            return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory,
                casProperties, oidcAttributeReleasePolicyFactory);
        }

    }

    @Configuration(value = "OidcCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oidcServiceJsonWebKeystoreCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache(
            @Qualifier("oidcServiceJsonWebKeystoreCacheLoader")
            final CacheLoader<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCacheLoader,
            final CasConfigurationProperties casProperties) {
            return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfter(new OidcServiceJsonWebKeystoreCacheExpirationPolicy(casProperties))
                .build(oidcServiceJsonWebKeystoreCacheLoader);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcServiceJsonWebKeystoreCacheLoader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCacheLoader(
            final ConfigurableApplicationContext applicationContext) {
            return new OidcRegisteredServiceJsonWebKeystoreCacheLoader(applicationContext);
        }

    }

    @Configuration(value = "OidcRedirectConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcRedirectConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "oidcRequestSupport")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcRequestSupport oidcRequestSupport(
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport) {
            return new OidcRequestSupport(ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
        }


        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oauthAuthorizationModelAndViewBuilder")
            final OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OidcCallbackAuthorizeViewResolver(servicesManager,
                oauthAuthorizationModelAndViewBuilder, oauthRequestParameterResolver);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcCasClientRedirectActionBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oidcRequestSupport")
            final OidcRequestSupport oidcRequestSupport) {
            return new OidcCasClientRedirectActionBuilder(oidcRequestSupport, oauthRequestParameterResolver);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier("oidcRequestSupport")
            final OidcRequestSupport oidcRequestSupport) {
            return new OidcCasClientRedirectActionBuilder(oidcRequestSupport, oauthRequestParameterResolver);
        }
    }

    @Configuration(value = "OidcConsentConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcConsentConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentApprovalViewResolver consentApprovalViewResolver(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            final CasConfigurationProperties casProperties) {
            return new OidcConsentApprovalViewResolver(casProperties,
                oauthDistributedSessionStore,
                ticketRegistry, ticketFactory,
                oauthRequestParameterResolver);
        }
    }

    @Configuration(value = "OidcTokenServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcTokenServiceConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcTokenSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService(
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache) throws Exception {
            return new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache,
                oidcIssuerService,
                oidcServerDiscoverySettingsFactory.getObject());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcUserProfileSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService(
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache) throws Exception {
            return new OidcUserProfileSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache,
                oidcIssuerService,
                oidcServerDiscoverySettingsFactory.getObject());
        }

    }

    @Configuration(value = "OidcCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcCryptoConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcRegisteredServiceJwtAccessTokenCipherExecutor")
        public RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor(
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache) {
            return new OidcRegisteredServiceJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache, oidcIssuerService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAccessTokenJwtCipherExecutor")
        public CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor(
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache) {
            return new OidcJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache, oidcIssuerService);
        }

    }

    @Configuration(value = "OidcClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcClientConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthenticationClientProvider oidcClientConfigurationAuthenticationClientProvider(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            return () -> {
                val accessTokenClient = new HeaderClient();
                accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
                accessTokenClient.setAuthenticator(new OidcClientConfigurationAccessTokenAuthenticator(
                    ticketRegistry, accessTokenJwtBuilder));
                accessTokenClient.setName(OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
                accessTokenClient.init();
                return accessTokenClient;
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthenticationClientProvider oidcDynamicRegistrationAuthenticationClientProvider(
            @Qualifier("oidcDynamicRegistrationAuthenticator")
            final Authenticator oidcDynamicRegistrationAuthenticator) {
            return () -> {
                val registrationClient = new HeaderClient();
                registrationClient.setCredentialsExtractor(new BearerAuthExtractor());
                registrationClient.setAuthenticator(oidcDynamicRegistrationAuthenticator);
                registrationClient.setName(Authenticators.CAS_OAUTH_CLIENT_DYNAMIC_REGISTRATION_AUTHN);
                registrationClient.init();
                return registrationClient;
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcDPoPClientProvider")
        public OAuth20AuthenticationClientProvider oidcDPoPClientProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
            return () -> {
                val client = new HeaderClient(OAuth20Constants.DPOP,
                    new OidcDPoPAuthenticator(oidcServerDiscoverySettings, servicesManager,
                        registeredServiceAccessStrategyEnforcer, casProperties));
                client.setName(Authenticators.CAS_OAUTH_CLIENT_DPOP_AUTHN);
                client.init();
                return client;
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJwtClientProvider")
        public OAuth20AuthenticationClientProvider oidcJwtClientProvider(
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return () -> {
                val authenticator = new OidcJwtAuthenticator(oidcIssuerService,
                    servicesManager, registeredServiceAccessStrategyEnforcer,
                    ticketRegistry, webApplicationServiceFactory,
                    casProperties, applicationContext);
                val privateKeyJwtClient = new DirectFormClient(authenticator);
                privateKeyJwtClient.setName(OidcConstants.CAS_OAUTH_CLIENT_PRIVATE_KEY_JWT_AUTHN);
                privateKeyJwtClient.setUsernameParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE);
                privateKeyJwtClient.setPasswordParameter(OAuth20Constants.CLIENT_ASSERTION);
                privateKeyJwtClient.init();
                return privateKeyJwtClient;
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthAccessTokenAuthenticator")
        public Authenticator oauthAccessTokenAuthenticator(
            @Qualifier("oidcTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) throws Exception {
            return new OidcAccessTokenAuthenticator(ticketRegistry,
                oidcTokenSigningAndEncryptionService, servicesManager, accessTokenJwtBuilder);
        }

        @ConditionalOnMissingBean(name = "oidcDynamicRegistrationAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oidcDynamicRegistrationAuthenticator(
            @Qualifier("oidcTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) throws Exception {
            val authenticator = new OidcAccessTokenAuthenticator(ticketRegistry,
                oidcTokenSigningAndEncryptionService, servicesManager, accessTokenJwtBuilder);
            authenticator.setRequiredScopes(Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            return authenticator;
        }
    }

    @Configuration(value = "OidcJwtConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcJwtConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAccessTokenJwtBuilder")
        public JwtBuilder accessTokenJwtBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcAccessTokenJwtCipherExecutor")
            final CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor,
            @Qualifier("oidcRegisteredServiceJwtAccessTokenCipherExecutor")
            final RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OAuth20JwtBuilder(oidcAccessTokenJwtCipherExecutor, servicesManager,
                oidcRegisteredServiceJwtAccessTokenCipherExecutor, casProperties);
        }
    }

    @Configuration(value = "OidcContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcContextConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = OidcConfigurationContext.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcConfigurationContext oidcConfigurationContext(
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator,
            @Qualifier("oidcIdTokenGenerator")
            final IdTokenGeneratorService oidcIdTokenGenerator,
            @Qualifier("oidcIdTokenExpirationPolicy")
            final ExpirationPolicyBuilder oidcIdTokenExpirationPolicy,
            @Qualifier("oidcUserProfileViewRenderer")
            final OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer,
            @Qualifier("oidcIdTokenClaimCollector")
            final OidcIdTokenClaimCollector oidcIdTokenClaimCollector,
            @Qualifier("callbackAuthorizeViewResolver")
            final OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver,
            @Qualifier("oauthInvalidAuthorizationBuilder")
            final OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationBuilder,
            @Qualifier("oidcUserProfileDataCreator")
            final OAuth20UserProfileDataCreator oidcUserProfileDataCreator,
            @Qualifier("oidcTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier("oauthCasAuthenticationBuilder")
            final OAuth20CasAuthenticationBuilder authenticationBuilder,
            @Qualifier("profileScopeToAttributesFilter")
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier("oidcRequestSupport")
            final OidcRequestSupport oidcRequestSupport,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthRequestValidators,
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor,
            @Qualifier("consentApprovalViewResolver")
            final ConsentApprovalViewResolver consentApprovalViewResolver,
            @Qualifier("oidcAttributeToScopeClaimMapper")
            final OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper,
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("deviceTokenExpirationPolicy")
            final ExpirationPolicyBuilder deviceTokenExpirationPolicy,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            final ObjectProvider<List<OAuth20AuthorizationResponseBuilder>> oidcAuthorizationResponseBuilders,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("oauthDistributedSessionCookieGenerator")
            final CasCookieBuilder oauthDistributedSessionCookieGenerator,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("clientRegistrationRequestSerializer")
            final StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            final ObjectProvider<List<OAuth20TokenRequestValidator>> oauthTokenRequestValidators,
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig,
            @Qualifier("oidcAccessTokenResponseGenerator")
            final OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier("oidcPrincipalFactory")
            final PrincipalFactory oidcPrincipalFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return (OidcConfigurationContext) OidcConfigurationContext.builder()
                .discoverySettings(oidcServerDiscoverySettings)
                .requestParameterResolver(oauthRequestParameterResolver)
                .issuerService(oidcIssuerService)
                .ticketFactory(ticketFactory)
                .idTokenClaimCollector(oidcIdTokenClaimCollector)
                .idTokenGeneratorService(oidcIdTokenGenerator)
                .idTokenExpirationPolicy(oidcIdTokenExpirationPolicy)
                .oidcRequestSupport(oidcRequestSupport)
                .attributeToScopeClaimMapper(oidcAttributeToScopeClaimMapper)
                .applicationContext(applicationContext)
                .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor)
                .sessionStore(oauthDistributedSessionStore)
                .servicesManager(servicesManager)
                .ticketRegistry(ticketRegistry)
                .clientRegistrationRequestSerializer(clientRegistrationRequestSerializer)
                .clientIdGenerator(new DefaultRandomStringGenerator())
                .clientSecretGenerator(new DefaultRandomStringGenerator())
                .principalFactory(oidcPrincipalFactory)
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
                .accessTokenResponseGenerator(oidcAccessTokenResponseGenerator)
                .deviceTokenExpirationPolicy(deviceTokenExpirationPolicy)
                .accessTokenGrantRequestValidators(oauthTokenRequestValidators)
                .userProfileDataCreator(oidcUserProfileDataCreator)
                .userProfileViewRenderer(oidcUserProfileViewRenderer)
                .consentApprovalViewResolver(consentApprovalViewResolver)
                .authenticationBuilder(authenticationBuilder)
                .oauthAuthorizationResponseBuilders(oidcAuthorizationResponseBuilders)
                .oauthInvalidAuthorizationResponseBuilder(oauthInvalidAuthorizationBuilder)
                .oauthRequestValidators(oauthRequestValidators)
                .singleLogoutServiceLogoutUrlBuilder(singleLogoutServiceLogoutUrlBuilder)
                .idTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService)
                .accessTokenJwtBuilder(accessTokenJwtBuilder)
                .clientSecretValidator(oauth20ClientSecretValidator)
                .build();
        }
    }

    @Configuration(value = "OidcCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public static class OidcCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcWebFingerUserInfoRepository")
        public OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository(
            final CasConfigurationProperties casProperties) {
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcCasCallbackUrlResolver")
        public UrlResolver casCallbackUrlResolver(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            final CasConfigurationProperties casProperties) {
            return new OidcCasCallbackUrlResolver(casProperties, oauthRequestParameterResolver);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcIdTokenClaimCollector")
        public OidcIdTokenClaimCollector oidcIdTokenClaimCollector() {
            return OidcIdTokenClaimCollector.defaultCollector();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OidcIssuerService.BEAN_NAME)
        public OidcIssuerService oidcIssuerService(final CasConfigurationProperties casProperties) {
            return new OidcDefaultIssuerService(casProperties.getAuthn().getOidc());
        }

        @ConditionalOnMissingBean(name = "oidcPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory oidcPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OidcAttributeToScopeClaimMapper.DEFAULT_BEAN_NAME)
        public OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper(final CasConfigurationProperties casProperties) {
            val mappings = casProperties.getAuthn().getOidc().getCore().getClaimsMap();
            return new OidcDefaultAttributeToScopeClaimMapper(mappings);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAttributeReleasePolicyFactory")
        public OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory(
            final CasConfigurationProperties casProperties) {
            return new DefaultOidcAttributeReleasePolicyFactory(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator oidcServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
            return new OidcServicesManagerRegisteredServiceLocator(casProperties);
        }

        @ConditionalOnMissingBean(name = "clientRegistrationRequestSerializer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer() {
            return new OidcClientRegistrationRequestSerializer();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService) {
            return new OidcAuthorizationModelAndViewBuilder(oidcIssuerService, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory(
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return new OidcServerDiscoverySettingsFactory(casProperties, oidcIssuerService, applicationContext);
        }
    }

    @Configuration(value = "OidcTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcTicketFactoryPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "pushedAuthorizationUriExpirationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder pushedAuthorizationUriExpirationPolicy(
            final CasConfigurationProperties casProperties) {
            return new OidcPushedAuthorizationRequestExpirationPolicyBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "pushedAuthorizationIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator pushedAuthorizationIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }

        @ConditionalOnMissingBean(name = "oidcPushedAuthorizationUriFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcPushedAuthorizationRequestFactory oidcPushedAuthorizationUriFactory(
            @Qualifier("pushedAuthorizationUriExpirationPolicy")
            final ExpirationPolicyBuilder pushedAuthorizationUriExpirationPolicy,
            @Qualifier("pushedAuthorizationIdGenerator")
            final UniqueTicketIdGenerator pushedAuthorizationIdGenerator) {
            return new OidcDefaultPushedAuthorizationRequestFactory(pushedAuthorizationIdGenerator, pushedAuthorizationUriExpirationPolicy);
        }

        @ConditionalOnMissingBean(name = "oidcPushedAuthorizationUriFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer oidcPushedAuthorizationUriFactoryConfigurer(
            @Qualifier("oidcPushedAuthorizationUriFactory")
            final OidcPushedAuthorizationRequestFactory oidcPushedAuthorizationRequestFactory) {
            return () -> oidcPushedAuthorizationRequestFactory;
        }
    }
}
