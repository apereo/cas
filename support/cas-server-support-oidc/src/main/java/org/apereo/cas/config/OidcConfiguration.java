package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.assurance.AssuranceVerificationJsonSource;
import org.apereo.cas.oidc.assurance.AssuranceVerificationSource;
import org.apereo.cas.oidc.assurance.AssuranceVerifiedClaimsProducer;
import org.apereo.cas.oidc.assurance.DefaultAssuranceVerifiedClaimsProducer;
import org.apereo.cas.oidc.authn.OidcAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcCasCallbackUrlResolver;
import org.apereo.cas.oidc.authn.OidcClientConfigurationAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcClientIdClientSecretAuthenticator;
import org.apereo.cas.oidc.authn.OidcDPoPAuthenticator;
import org.apereo.cas.oidc.authn.OidcJwtAuthenticator;
import org.apereo.cas.oidc.authn.OidcX509Authenticator;
import org.apereo.cas.oidc.claims.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.claims.OidcDefaultAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.claims.OidcIdTokenClaimCollector;
import org.apereo.cas.oidc.claims.OidcSimpleIdTokenClaimCollector;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactory;
import org.apereo.cas.oidc.discovery.webfinger.OidcDefaultWebFingerDiscoveryService;
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
import org.apereo.cas.oidc.profile.OidcTokenIntrospectionSigningAndEncryptionService;
import org.apereo.cas.oidc.profile.OidcUserProfileDataCreator;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionService;
import org.apereo.cas.oidc.profile.OidcUserProfileViewRenderer;
import org.apereo.cas.oidc.scopes.DefaultOidcAttributeReleasePolicyFactory;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.oidc.services.OidcServiceRegistryListener;
import org.apereo.cas.oidc.services.OidcServicesManagerRegisteredServiceLocator;
import org.apereo.cas.oidc.ticket.OidcCibaRequestExpirationPolicyBuilder;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.oidc.ticket.OidcDefaultCibaRequestFactory;
import org.apereo.cas.oidc.ticket.OidcDefaultPushedAuthorizationRequestFactory;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestExpirationPolicyBuilder;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.oidc.ticket.OidcTicketCatalogConfigurer;
import org.apereo.cas.oidc.token.OidcDefaultTokenGenerator;
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
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationRequestTranslator;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDefaultClientRegistrationRequestTranslator;
import org.apereo.cas.oidc.web.response.OidcJwtResponseModeCipherExecutor;
import org.apereo.cas.oidc.web.response.OidcRegisteredServiceJwtResponseModeCipherExecutor;
import org.apereo.cas.oidc.web.response.OidcResponseModeFormPostJwtBuilder;
import org.apereo.cas.oidc.web.response.OidcResponseModeFragmentJwtBuilder;
import org.apereo.cas.oidc.web.response.OidcResponseModeQueryJwtBuilder;
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
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionResponseGenerator;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.JacksonObjectMapperCustomizer;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.SecurityLogicInterceptor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.Serializable;
import java.util.ArrayList;
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
@Configuration(value = "OidcConfiguration", proxyBeanMethods = false)
class OidcConfiguration {

    @Configuration(value = "OidcServicesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcServicesConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcRegisteredServiceSerializationCustomizer")
        public JacksonObjectMapperCustomizer oidcRegisteredServiceSerializationCustomizer(
            final CasConfigurationProperties casProperties) {
            return JacksonObjectMapperCustomizer.mappedInjectableValues(
                casProperties.getAuthn().getOidc().getServices().getDefaults());
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcServiceRegistryListener")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public ServiceRegistryListener oidcServiceRegistryListener(
            @Qualifier(OidcAttributeReleasePolicyFactory.BEAN_NAME)
            final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory) {
            return new OidcServiceRegistryListener(oidcAttributeReleasePolicyFactory);
        }
    }

    @Configuration(value = "OidcWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcWebConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ClientSecretValidator oauth20ClientSecretValidator(
            @Qualifier("oauthRegisteredServiceCipherExecutor")
            final CipherExecutor oauthRegisteredServiceCipherExecutor) {
            return new OidcClientSecretValidator(oauthRegisteredServiceCipherExecutor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAuthorizationSecurityLogic")
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
            val authzConfig = oauthSecConfig.withSecurityLogic(oidcAuthorizationSecurityLogic);
            return new SecurityLogicInterceptor(authzConfig, Authenticators.CAS_OAUTH_CLIENT);
        }
    }

    @Configuration(value = "OidcWebFingerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcWebFingerConfiguration {
        private static final BeanCondition CONDITION_WEBFINGER = BeanCondition.on("cas.authn.oidc.web-finger.enabled").isTrue().evenIfMissing();

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OidcWebFingerUserInfoRepository.BEAN_NAME)
        public OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(OidcWebFingerUserInfoRepository.class)
                .when(CONDITION_WEBFINGER.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val userInfo = casProperties.getAuthn().getOidc().getWebfinger().getUserInfo();
                    if (userInfo.getGroovy().getLocation() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
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
                })
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcWebFingerDiscoveryService")
        public OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(OidcWebFingerUserInfoRepository.BEAN_NAME)
            final OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory) {

            return BeanSupplier.of(OidcWebFingerDiscoveryService.class)
                .when(CONDITION_WEBFINGER.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> new OidcDefaultWebFingerDiscoveryService(
                    oidcWebFingerUserInfoRepository,
                    oidcServerDiscoverySettingsFactory.getObject(),
                    casProperties.getAuthn().getOidc().getWebfinger())))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "OidcUserProfileConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcUserProfileConfiguration {

        @ConditionalOnMissingBean(name = "oidcUserProfileViewRenderer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer(
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier("oidcUserProfileSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return new OidcUserProfileViewRenderer(casProperties.getAuthn().getOauth(),
                servicesManager, oidcUserProfileSigningAndEncryptionService, attributeDefinitionStore);
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
    static class OidcClaimsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcPrincipalFactory")
            final PrincipalFactory oidcPrincipalFactory,
            @Qualifier(OidcAttributeReleasePolicyFactory.BEAN_NAME)
            final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory,
            final CasConfigurationProperties casProperties) {
            return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory,
                casProperties, oidcAttributeReleasePolicyFactory, applicationContext);
        }

    }

    @Configuration(value = "OidcCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcCacheConfiguration {

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
    static class OidcRedirectConfiguration {

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
    static class OidcConsentConfiguration {
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
    static class OidcTokenServiceConfiguration {

        @ConditionalOnMissingBean(name = "oidcTokenGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenGenerator oauthTokenGenerator(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver principalResolver,
            @Qualifier(OAuth20ProfileScopeToAttributesFilter.BEAN_NAME)
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties) {
            return new OidcDefaultTokenGenerator(ticketFactory, ticketRegistry,
                principalResolver, profileScopeToAttributesFilter, casProperties);
        }
        
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcTokenSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) throws Exception {
            return new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache,
                oidcIssuerService,
                oidcServerDiscoverySettingsFactory.getObject(),
                casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcUserProfileSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) throws Exception {
            return new OidcUserProfileSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache,
                oidcIssuerService,
                oidcServerDiscoverySettingsFactory.getObject(),
                casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcTokenIntrospectionSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService oidcTokenIntrospectionSigningAndEncryptionService(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) throws Exception {
            return new OidcTokenIntrospectionSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache,
                oidcIssuerService,
                oidcServerDiscoverySettingsFactory.getObject(),
                casProperties);
        }
    }

    @Configuration(value = "OidcCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcCryptoConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcRegisteredServiceJwtAccessTokenCipherExecutor")
        public RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor(
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {
            return new OidcRegisteredServiceJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache,
                oidcServiceJsonWebKeystoreCache, oidcIssuerService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAccessTokenJwtCipherExecutor")
        public CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {

            val crypto = casProperties.getAuthn().getOauth().getAccessToken().getCrypto();
            return FunctionUtils.doIf(crypto.isEnabled(),
                () -> {
                    val cipher = new OidcJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache, oidcIssuerService);
                    cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
                    cipher.setSigningEnabled(crypto.isSigningEnabled());
                    cipher.setEncryptionEnabled(crypto.isEncryptionEnabled());
                    return cipher;
                },
                CipherExecutor::noOpOfSerializableToString).get();
        }

    }

    @Configuration(value = "OidcClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcClientConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthenticationClientProvider oidcClientConfigurationAuthenticationClientProvider(
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
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
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
            return () -> {
                val authenticator = new OidcJwtAuthenticator(oidcIssuerService,
                    servicesManager, registeredServiceAccessStrategyEnforcer,
                    ticketRegistry, webApplicationServiceFactory,
                    casProperties, applicationContext, oidcServerDiscoverySettings);
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
        @ConditionalOnMissingBean(name = "oidcAccessTokenAuthenticator")
        public Authenticator oauthAccessTokenAuthenticator(
            @Qualifier("oidcTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OidcAccessTokenAuthenticator(ticketRegistry,
                oidcTokenSigningAndEncryptionService, servicesManager, accessTokenJwtBuilder);
        }

        @ConditionalOnMissingBean(name = "oidcDynamicRegistrationAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oidcDynamicRegistrationAuthenticator(
            @Qualifier("oidcTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val authenticator = new OidcAccessTokenAuthenticator(ticketRegistry,
                oidcTokenSigningAndEncryptionService, servicesManager, accessTokenJwtBuilder);
            authenticator.setRequiredScopes(Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            return authenticator;
        }
    }

    @Configuration(value = "OidcJwtConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcJwtConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcAccessTokenJwtBuilder")
        public JwtBuilder accessTokenJwtBuilder(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcAccessTokenJwtCipherExecutor")
            final CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor,
            @Qualifier("oidcRegisteredServiceJwtAccessTokenCipherExecutor")
            final RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver principalResolver) {
            return new OAuth20JwtBuilder(oidcAccessTokenJwtCipherExecutor, applicationContext, servicesManager,
                oidcRegisteredServiceJwtAccessTokenCipherExecutor, casProperties, principalResolver, webApplicationServiceFactory);
        }
    }

    @Configuration(value = "OidcContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcContextConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = OidcConfigurationContext.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcConfigurationContext oidcConfigurationContext(
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier("oidcTokenIntrospectionSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService oidcTokenIntrospectionSigningAndEncryptionService,
            @Qualifier("oidcClientRegistrationRequestTranslator")
            final OidcClientRegistrationRequestTranslator oidcClientRegistrationRequestTranslator,
            @Qualifier("oidcResponseModeJwtBuilder")
            final JwtBuilder oidcResponseModeJwtBuilder,
            @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
            final OAuth20ClientSecretValidator oauth20ClientSecretValidator,
            @Qualifier("oidcIdTokenGenerator")
            final IdTokenGeneratorService oidcIdTokenGenerator,
            @Qualifier("oidcIdTokenExpirationPolicy")
            final ExpirationPolicyBuilder oidcIdTokenExpirationPolicy,
            @Qualifier("oidcUserProfileViewRenderer")
            final OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer,
            final List<OidcIdTokenClaimCollector> oidcIdTokenClaimCollectors,
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
            @Qualifier(OAuth20ProfileScopeToAttributesFilter.BEAN_NAME)
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
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
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
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            final List<OAuth20IntrospectionResponseGenerator> oauthIntrospectionResponseGenerator,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver principalResolver,
            @Qualifier("taskScheduler")
            final TaskScheduler taskScheduler,
            @Qualifier("messageSource")
            final MessageSource messageSource,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationManager,
            @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
            final CipherExecutor webflowCipherExecutor,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor) {

            val sortedIdClaimCollectors = new ArrayList<>(oidcIdTokenClaimCollectors);
            AnnotationAwareOrderComparator.sortIfNecessary(sortedIdClaimCollectors);

            return (OidcConfigurationContext) OidcConfigurationContext
                .builder()
                .messageSource(messageSource)
                .introspectionSigningAndEncryptionService(oidcTokenIntrospectionSigningAndEncryptionService)
                .introspectionResponseGenerator(oauthIntrospectionResponseGenerator)
                .argumentExtractor(argumentExtractor)
                .responseModeJwtBuilder(oidcResponseModeJwtBuilder)
                .authenticationAttributeReleasePolicy(authenticationAttributeReleasePolicy)
                .discoverySettings(oidcServerDiscoverySettings)
                .requestParameterResolver(oauthRequestParameterResolver)
                .issuerService(oidcIssuerService)
                .clientRegistrationRequestTranslator(oidcClientRegistrationRequestTranslator)
                .ticketFactory(ticketFactory)
                .idTokenClaimCollectors(sortedIdClaimCollectors)
                .idTokenGeneratorService(oidcIdTokenGenerator)
                .idTokenExpirationPolicy(oidcIdTokenExpirationPolicy)
                .oidcRequestSupport(oidcRequestSupport)
                .attributeToScopeClaimMapper(oidcAttributeToScopeClaimMapper)
                .applicationContext(applicationContext)
                .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor)
                .sessionStore(oauthDistributedSessionStore)
                .servicesManager(servicesManager)
                .ticketRegistry(ticketRegistry)
                .httpClient(httpClient)
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
                .attributeDefinitionStore(attributeDefinitionStore)
                .principalResolver(principalResolver)
                .taskScheduler(taskScheduler)
                .communicationsManager(communicationManager)
                .webflowCipherExecutor(webflowCipherExecutor)
                .tenantExtractor(tenantExtractor)
                .build();
        }
    }

    @Configuration(value = "OidcCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    static class OidcCoreConfiguration {
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
        @ConditionalOnMissingBean(name = AssuranceVerifiedClaimsProducer.BEAN_NAME)
        public AssuranceVerifiedClaimsProducer assuranceVerifiedClaimsProducer(
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings,
            @Qualifier(AssuranceVerificationSource.BEAN_NAME)
            final AssuranceVerificationSource assuranceVerificationSource) {
            return new DefaultAssuranceVerifiedClaimsProducer(assuranceVerificationSource, oidcServerDiscoverySettings);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OidcIdTokenClaimCollector.BEAN_NAME)
        public OidcIdTokenClaimCollector oidcIdTokenClaimCollector(
            @Qualifier(AssuranceVerifiedClaimsProducer.BEAN_NAME)
            final AssuranceVerifiedClaimsProducer assuranceVerifiedClaimsProducer,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore) {
            return new OidcSimpleIdTokenClaimCollector(attributeDefinitionStore, assuranceVerifiedClaimsProducer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = OidcIssuerService.BEAN_NAME)
        public OidcIssuerService oidcIssuerService(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties) {
            return new OidcDefaultIssuerService(casProperties.getAuthn().getOidc(), tenantExtractor);
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
        @ConditionalOnMissingBean(name = OidcAttributeReleasePolicyFactory.BEAN_NAME)
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
        public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer(
            final ConfigurableApplicationContext applicationContext) {
            return new OidcClientRegistrationRequestSerializer(applicationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcClientRegistrationRequestTranslator")
        public OidcClientRegistrationRequestTranslator oidcClientRegistrationRequestTranslator(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcDefaultClientRegistrationRequestTranslator(oidcConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder(
            @Qualifier(OAuth20ResponseModeFactory.BEAN_NAME)
            final OAuth20ResponseModeFactory oauthResponseModeFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService) {
            return new OidcAuthorizationModelAndViewBuilder(oauthResponseModeFactory, oidcIssuerService, casProperties);
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


        @ConditionalOnMissingBean(name = "oidcClientAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oauthClientAuthenticator(
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(OAuth20ProfileScopeToAttributesFilter.BEAN_NAME)
            final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
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
            return new OidcClientIdClientSecretAuthenticator(servicesManager,
                webApplicationServiceFactory,
                registeredServiceAccessStrategyEnforcer,
                ticketRegistry,
                defaultPrincipalResolver,
                oauthRequestParameterResolver,
                oauth20ClientSecretValidator,
                profileScopeToAttributesFilter,
                ticketFactory,
                applicationContext,
                oidcServerDiscoverySettings);
        }

        @ConditionalOnMissingBean(name = "oidcX509CertificateAuthenticator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Authenticator oauthX509CertificateAuthenticator(
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final OidcServerDiscoverySettings oidcServerDiscoverySettings,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new OidcX509Authenticator(servicesManager, oauthRequestParameterResolver, oidcServerDiscoverySettings);
        }
    }

    @Configuration(value = "OidcTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcTicketFactoryPlanConfiguration {

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
            return new HostNameBasedUniqueTicketIdGenerator();
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

        @Bean
        @ConditionalOnMissingBean(name = "cibaRequestExpirationPolicy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder cibaRequestExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new OidcCibaRequestExpirationPolicyBuilder(casProperties);
        }
        
        @ConditionalOnMissingBean(name = "oidcCibaRequestFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcCibaRequestFactory oidcCibaRequestFactory(
            @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
            final CipherExecutor webflowCipherExecutor,
            @Qualifier("cibaRequestExpirationPolicy")
            final ExpirationPolicyBuilder cibaRequestExpirationPolicy,
            @Qualifier("cibaRequestIdGenerator")
            final UniqueTicketIdGenerator cibaRequestIdGenerator) {
            return new OidcDefaultCibaRequestFactory(cibaRequestIdGenerator, cibaRequestExpirationPolicy, webflowCipherExecutor);
        }

        @ConditionalOnMissingBean(name = "cibaRequestIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator cibaRequestIdGenerator() {
            return new HostNameBasedUniqueTicketIdGenerator();
        }


        @ConditionalOnMissingBean(name = "oidcCibaRequestFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer oidcCibaRequestFactoryConfigurer(
            @Qualifier("oidcCibaRequestFactory")
            final OidcCibaRequestFactory oidcCibaRequestFactory) {
            return () -> oidcCibaRequestFactory;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcTicketCatalogConfigurer")
        public TicketCatalogConfigurer oidcTicketCatalogConfigurer() {
            return new OidcTicketCatalogConfigurer();
        }
    }

    @Configuration(value = "OidcResponseModesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcResponseModesConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthQueryJwtResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthQueryJwtResponseModeBuilder(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcResponseModeQueryJwtBuilder(oidcConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthFragmentJwtResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthFragmentJwtResponseModeBuilder(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcResponseModeFragmentJwtBuilder(oidcConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthFormPostJwtResponseModeBuilder")
        public OAuth20ResponseModeBuilder oauthFormPostJwtResponseModeBuilder(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
            return new OidcResponseModeFormPostJwtBuilder(oidcConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcResponseModeJwtCipherExecutor")
        public CipherExecutor<Serializable, String> oidcResponseModeJwtCipherExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {
            val crypto = casProperties.getAuthn().getOidc().getResponse().getCrypto();
            return FunctionUtils.doIf(crypto.isEnabled(),
                () -> {
                    val cipher = new OidcJwtResponseModeCipherExecutor(oidcDefaultJsonWebKeystoreCache, oidcIssuerService);
                    cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
                    cipher.setSigningEnabled(crypto.isSigningEnabled());
                    cipher.setEncryptionEnabled(crypto.isEncryptionEnabled());
                    return cipher;
                },
                CipherExecutor::noOpOfSerializableToString).get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcRegisteredServiceResponseModeJwtCipherExecutor")
        public RegisteredServiceCipherExecutor oidcRegisteredServiceResponseModeJwtCipherExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcServiceJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache,
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {
            val crypto = casProperties.getAuthn().getOidc().getResponse().getCrypto();
            return FunctionUtils.doIf(crypto.isEnabled(),
                () -> new OidcRegisteredServiceJwtResponseModeCipherExecutor(oidcDefaultJsonWebKeystoreCache,
                    oidcServiceJsonWebKeystoreCache, oidcIssuerService),
                RegisteredServiceCipherExecutor::noOp).get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcResponseModeJwtBuilder")
        public JwtBuilder oidcResponseModeJwtBuilder(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcRegisteredServiceResponseModeJwtCipherExecutor")
            final RegisteredServiceCipherExecutor oidcRegisteredServiceResponseModeJwtCipherExecutor,
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcResponseModeJwtCipherExecutor")
            final CipherExecutor<Serializable, String> oidcResponseModeJwtCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver principalResolver) {
            return new OAuth20JwtBuilder(oidcResponseModeJwtCipherExecutor, applicationContext, servicesManager,
                oidcRegisteredServiceResponseModeJwtCipherExecutor, casProperties, principalResolver, webApplicationServiceFactory);
        }

    }
    
    @Configuration(value = "OidcAssuranceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcAssuranceConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = AssuranceVerificationSource.BEAN_NAME)
        public AssuranceVerificationSource assuranceVerificationSource(
            final CasConfigurationProperties casProperties) {
            val source = casProperties.getAuthn().getOidc().getIdentityAssurance().getVerificationSource().getLocation();
            return FunctionUtils.doIfNotNull(source,
                () -> new AssuranceVerificationJsonSource(source), AssuranceVerificationSource::empty).get();
        }
    }

}
