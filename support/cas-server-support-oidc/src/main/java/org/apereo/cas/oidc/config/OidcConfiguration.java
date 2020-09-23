package org.apereo.cas.oidc.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcClientConfigurationAccessTokenAuthenticator;
import org.apereo.cas.oidc.authn.OidcClientSecretJwtAuthenticator;
import org.apereo.cas.oidc.authn.OidcPrivateKeyJwtAuthenticator;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
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
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheExpirationPolicy;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.profile.OidcProfileScopeToAttributesFilter;
import org.apereo.cas.oidc.profile.OidcUserProfileDataCreator;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionService;
import org.apereo.cas.oidc.profile.OidcUserProfileViewRenderer;
import org.apereo.cas.oidc.services.OidcServiceRegistryListener;
import org.apereo.cas.oidc.slo.OidcSingleLogoutMessageCreator;
import org.apereo.cas.oidc.slo.OidcSingleLogoutServiceMessageHandler;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionService;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.oidc.web.OidcCallbackAuthorizeViewResolver;
import org.apereo.cas.oidc.web.OidcCasClientRedirectActionBuilder;
import org.apereo.cas.oidc.web.OidcConsentApprovalViewResolver;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcSecurityInterceptor;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksEndpointController;
import org.apereo.cas.oidc.web.controllers.logout.OidcLogoutEndpointController;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcAccessTokenEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
import org.apereo.cas.oidc.web.flow.OidcMultifactorAuthenticationTrigger;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIAction;
import org.apereo.cas.oidc.web.flow.OidcWebflowConfigurer;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuthAuthenticationClientProvider;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.BearerAuthExtractor;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OidcConfiguration implements WebMvcConfigurer {
    @Autowired
    @Qualifier("oauthRegisteredServiceCipherExecutor")
    private ObjectProvider<CipherExecutor> oauthRegisteredServiceCipherExecutor;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    private ObjectProvider<SessionStore> oauthDistributedSessionStore;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    @Autowired
    @Qualifier("accessTokenGrantAuditableRequestExtractor")
    private ObjectProvider<AuditableExecution> accessTokenGrantAuditableRequestExtractor;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("oauthAuthorizationRequestValidators")
    private ObjectProvider<Set<OAuth20AuthorizationRequestValidator>> oauthRequestValidators;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    private ObjectProvider<OAuth20TokenGenerator> oauthTokenGenerator;

    @Autowired
    @Qualifier("oauthAuthorizationResponseBuilders")
    private ObjectProvider<Set<OAuth20AuthorizationResponseBuilder>> oauthAuthorizationResponseBuilders;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    @Qualifier("accessTokenExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> accessTokenExpirationPolicy;

    @Autowired
    @Qualifier("deviceTokenExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> deviceTokenExpirationPolicy;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private ObjectProvider<SecurityInterceptor> requiresAuthenticationAccessTokenInterceptor;

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private ObjectProvider<OAuth20CasAuthenticationBuilder> authenticationBuilder;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> logoutFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private ObjectProvider<AuthenticationServiceSelectionStrategy> oauth20AuthenticationServiceSelectionStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casProtocolViewFactory")
    private ObjectProvider<CasProtocolViewFactory> casProtocolViewFactory;

    @Autowired
    private ResourceLoader resourceLoader;

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
    @Qualifier("defaultAccessTokenFactory")
    private ObjectProvider<OAuth20AccessTokenFactory> defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    private ObjectProvider<OAuth20DeviceTokenFactory> defaultDeviceTokenFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private ObjectProvider<OAuth20CodeFactory> defaultOAuthCodeFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private ObjectProvider<Collection<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors;

    @Autowired
    @Qualifier("oauthTokenRequestValidators")
    private ObjectProvider<Collection<OAuth20TokenRequestValidator>> oauthTokenRequestValidators;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderResolver")
    private ObjectProvider<MultifactorAuthenticationProviderResolver> multifactorAuthenticationProviderResolver;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("oauthDistributedSessionCookieGenerator")
    private ObjectProvider<CasCookieBuilder> oauthDistributedSessionCookieGenerator;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor()).addPathPatterns('/' + OidcConstants.BASE_OIDC_URL.concat("/").concat("*"));
    }

    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OidcConsentApprovalViewResolver(casProperties);
    }

    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OidcCallbackAuthorizeViewResolver();
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor() {
        val clients = String.join(",",
            Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
            Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationClientConfigurationInterceptor() {
        val clients = String.join(",", OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor() {
        val name = oauthSecConfig.getObject().getClients()
            .findClient(CasClient.class)
            .orElseThrow()
            .getName();
        val interceptor = new OidcSecurityInterceptor(oauthSecConfig.getObject(), name,
            oidcAuthorizationRequestSupport(),
            oauthDistributedSessionStore.getObject());

        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcCasClientRedirectActionBuilder")
    @RefreshScope
    public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcIdTokenGenerator")
    @Bean
    public IdTokenGeneratorService oidcIdTokenGenerator() {
        val context = buildConfigurationContext();
        context.setIdTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService());
        return new OidcIdTokenGeneratorService(context);
    }

    @ConditionalOnMissingBean(name = "oidcAccessTokenResponseGenerator")
    @Bean
    @RefreshScope
    public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator(),
            accessTokenJwtBuilder.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcAuthorizationRequestSupport")
    public OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport() {
        return new OidcAuthorizationRequestSupport(ticketGrantingTicketCookieGenerator.getObject(), ticketRegistrySupport.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory oidcPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcAttributeToScopeClaimMapper")
    public OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper() {
        val mappings = casProperties.getAuthn().getOidc().getClaimsMap();
        return new OidcDefaultAttributeToScopeClaimMapper(mappings);
    }

    @Bean
    @RefreshScope
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory(),
            casProperties, userDefinedScopeBasedAttributeReleasePolicies());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServiceRegistryListener")
    public ServiceRegistryListener oidcServiceRegistryListener() {
        return new OidcServiceRegistryListener(userDefinedScopeBasedAttributeReleasePolicies());
    }

    @RefreshScope
    @Bean
    public OidcIntrospectionEndpointController oidcIntrospectionEndpointController() {
        val context = buildConfigurationContext();
        return new OidcIntrospectionEndpointController(context);
    }

    @RefreshScope
    @Bean
    public OidcLogoutEndpointController oidcLogoutEndpointController() {
        val context = buildConfigurationContext();
        return new OidcLogoutEndpointController(context);
    }

    @RefreshScope
    @Bean
    public OidcRevocationEndpointController oidcRevocationEndpointController() {
        val context = buildConfigurationContext();
        return new OidcRevocationEndpointController(context);
    }

    @RefreshScope
    @Bean
    public OidcAccessTokenEndpointController oidcAccessTokenController() {
        val context = buildConfigurationContext();
        return new OidcAccessTokenEndpointController(context, accessTokenGrantAuditableRequestExtractor.getObject());
    }

    @ConditionalOnMissingBean(name = "clientRegistrationRequestSerializer")
    @Bean
    @RefreshScope
    public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer() {
        return new OidcClientRegistrationRequestSerializer();
    }

    @RefreshScope
    @Bean
    public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController() {
        val context = buildConfigurationContext();
        return new OidcDynamicClientRegistrationEndpointController(context);
    }

    @RefreshScope
    @Bean
    public OidcClientConfigurationEndpointController oidcClientConfigurationEndpointController() {
        val context = buildConfigurationContext();
        return new OidcClientConfigurationEndpointController(context);
    }


    @RefreshScope
    @Bean
    public OidcJwksEndpointController oidcJwksController() {
        val context = buildConfigurationContext();
        return new OidcJwksEndpointController(context, oidcJsonWebKeystoreGeneratorService());
    }

    @RefreshScope
    @Bean
    @Autowired
    public OidcWellKnownEndpointController oidcWellKnownController(@Qualifier("oidcWebFingerDiscoveryService")
                                                                   final OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService) {
        val context = buildConfigurationContext();
        return new OidcWellKnownEndpointController(context, oidcWebFingerDiscoveryService);
    }

    @RefreshScope
    @Bean
    public OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService(@Qualifier("oidcServerDiscoverySettingsFactory")
                                                                        final OidcServerDiscoverySettings discoverySettings) {
        return new OidcWebFingerDiscoveryService(oidcWebFingerUserInfoRepository(), discoverySettings);
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

        LOGGER.warn("Using [{}] to locate webfinger resources, which is NOT appropriate for production purposes, "
            + "as it will always echo back the given username/email address and is only useful for testing/demo purposes. "
            + "Consider choosing and configuring a different repository implementation for locating and fetching user information "
            + "for webfinger resources, etc.", OidcEchoingWebFingerUserInfoRepository.class.getSimpleName());
        return new OidcEchoingWebFingerUserInfoRepository();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcProfileController")
    @Bean
    public OidcUserProfileEndpointController oidcProfileController() {
        val context = buildConfigurationContext();
        return new OidcUserProfileEndpointController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcUserProfileDataCreator")
    @RefreshScope
    public OAuth20UserProfileDataCreator oidcUserProfileDataCreator() {
        return new OidcUserProfileDataCreator(servicesManager.getObject(), profileScopeToAttributesFilter());
    }

    @RefreshScope
    @Bean
    public OidcAuthorizeEndpointController oidcAuthorizeController() {
        val context = buildConfigurationContext();
        return new OidcAuthorizeEndpointController(context);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcMultifactorAuthenticationTrigger")
    public MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger() {
        return new OidcMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver.getObject(), this.applicationContext);
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .warnCookieGenerator(warnCookieGenerator.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .casProperties(casProperties)
            .ticketRegistry(ticketRegistry.getObject())
            .applicationContext(applicationContext)
            .authenticationEventExecutionPlan(authenticationEventExecutionPlan.getObject())
            .build();

        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(context, oidcMultifactorAuthenticationTrigger());
        Objects.requireNonNull(this.initialAuthenticationAttemptWebflowEventResolver.getObject()).addDelegate(r);
        return r;
    }

    @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer oidcWebflowConfigurer() {
        val cfg = new OidcWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), oidcRegisteredServiceUIAction(), applicationContext, casProperties);
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry.getObject());
        return cfg;
    }

    @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
    @Bean
    public Action oidcRegisteredServiceUIAction() {
        return new OidcRegisteredServiceUIAction(this.servicesManager.getObject(), oauth20AuthenticationServiceSelectionStrategy.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcTokenSigningAndEncryptionService")
    public OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService() {
        val oidc = casProperties.getAuthn().getOidc();
        return new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache(),
            oidcServiceJsonWebKeystoreCache(),
            oidc.getIssuer());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcUserProfileSigningAndEncryptionService")
    public OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService() {
        val oidc = casProperties.getAuthn().getOidc();
        return new OidcUserProfileSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache(),
            oidcServiceJsonWebKeystoreCache(),
            oidc.getIssuer());
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
    @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
    @RefreshScope
    public LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache() {
        val oidc = casProperties.getAuthn().getOidc();
        return Caffeine.newBuilder().maximumSize(1)
            .expireAfterWrite(Duration.ofMinutes(oidc.getJwks().getJwksCacheInMinutes()))
            .build(oidcDefaultJsonWebKeystoreCacheLoader());
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
    @RefreshScope
    public CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader() {
        return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServiceJsonWebKeystoreCacheLoader")
    public CacheLoader<OAuthRegisteredService, Optional<PublicJsonWebKey>> oidcServiceJsonWebKeystoreCacheLoader() {
        return new OidcServiceJsonWebKeystoreCacheLoader(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServerDiscoverySettingsFactory")
    public FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory() {
        return new OidcServerDiscoverySettingsFactory(casProperties);
    }

    @Bean(initMethod = "generate")
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
    public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
        val oidc = casProperties.getAuthn().getOidc();
        if (StringUtils.isNotBlank(oidc.getJwks().getRest().getUrl())) {
            return new OidcRestfulJsonWebKeystoreGeneratorService(oidc);
        }
        return new OidcDefaultJsonWebKeystoreGeneratorService(oidc);
    }

    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        val oidc = casProperties.getAuthn().getOidc();
        val mode = OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
            oidc.getDynamicClientRegistrationMode(),
            OidcConstants.DynamicClientRegistrationMode.PROTECTED.name()));

        return new OidcHandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor.getObject(),
            requiresAuthenticationAuthorizeInterceptor(),
            requiresAuthenticationDynamicRegistrationInterceptor(),
            requiresAuthenticationClientConfigurationInterceptor(),
            mode,
            accessTokenGrantRequestExtractors.getObject(),
            servicesManager.getObject(),
            oauthDistributedSessionStore.getObject());
    }

    @RefreshScope
    @Bean
    public Collection<OidcCustomScopeAttributeReleasePolicy> userDefinedScopeBasedAttributeReleasePolicies() {
        val oidc = casProperties.getAuthn().getOidc();
        return oidc.getUserDefinedScopes().entrySet()
            .stream()
            .map(k -> new OidcCustomScopeAttributeReleasePolicy(k.getKey(), CollectionUtils.wrapList(k.getValue().split(","))))
            .collect(Collectors.toSet());
    }

    @Bean
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder() {
        return new OidcImplicitIdTokenAuthorizationResponseBuilder(oidcIdTokenGenerator(), oauthTokenGenerator.getObject(),
            accessTokenExpirationPolicy.getObject(), grantingTicketExpirationPolicy.getObject(),
            servicesManager.getObject(), accessTokenJwtBuilder.getObject(), casProperties);
    }

    @Bean
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder() {
        return new OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(oidcIdTokenGenerator(), oauthTokenGenerator.getObject(),
            accessTokenExpirationPolicy.getObject(), grantingTicketExpirationPolicy.getObject(),
            servicesManager.getObject(), accessTokenJwtBuilder.getObject(), casProperties);
    }

    @Bean
    public RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor() {
        val oidc = casProperties.getAuthn().getOidc();
        return new OidcRegisteredServiceJwtAccessTokenCipherExecutor(oidcDefaultJsonWebKeystoreCache(),
            oidcServiceJsonWebKeystoreCache(),
            oidc.getIssuer());
    }

    @Bean
    public OAuthAuthenticationClientProvider oidcClientConfigurationAuthenticationClientProvider() {
        return () -> {
            val accessTokenClient = new HeaderClient();
            accessTokenClient.setCredentialsExtractor(new BearerAuthExtractor());
            accessTokenClient.setAuthenticator(new OidcClientConfigurationAccessTokenAuthenticator(ticketRegistry.getObject(),
                accessTokenJwtBuilder.getObject()));
            accessTokenClient.setName(OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
            accessTokenClient.init();
            return accessTokenClient;
        };
    }

    @Bean
    public OAuthAuthenticationClientProvider oidcPrivateKeyJwtClientProvider() {
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
    public OAuthAuthenticationClientProvider oidcClientSecretJwtClientProvider() {
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
    public Authenticator<TokenCredentials> oAuthAccessTokenAuthenticator() {
        return new OidcAccessTokenAuthenticator(ticketRegistry.getObject(),
            oidcTokenSigningAndEncryptionService(), servicesManager.getObject(),
            accessTokenJwtBuilder.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer oidcCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(oidcWebflowConfigurer());
    }

    @ConditionalOnMissingBean(name = "oidcUserProfileViewRenderer")
    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer() {
        return new OidcUserProfileViewRenderer(casProperties.getAuthn().getOauth(),
            servicesManager.getObject(),
            oidcUserProfileSigningAndEncryptionService());
    }

    @Bean
    public View oidcConfirmView() {
        return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oidc/confirm");
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutMessageCreator")
    @Bean
    @RefreshScope
    public SingleLogoutMessageCreator oidcSingleLogoutMessageCreator() {
        return new OidcSingleLogoutMessageCreator(buildConfigurationContext());
    }

    @ConditionalOnMissingBean(name = "oidcSingleLogoutServiceMessageHandler")
    @Bean
    @RefreshScope
    public SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler() {
        return new OidcSingleLogoutServiceMessageHandler(httpClient.getObject(),
            oidcSingleLogoutMessageCreator(),
            servicesManager.getObject(),
            singleLogoutServiceLogoutUrlBuilder.getObject(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getObject(),
            casProperties.getAuthn().getOidc().getIssuer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcLogoutExecutionPlanConfigurer")
    public LogoutExecutionPlanConfigurer oidcLogoutExecutionPlanConfigurer() {
        return plan -> plan.registerSingleLogoutServiceMessageHandler(oidcSingleLogoutServiceMessageHandler());
    }

    private OAuth20ConfigurationContext buildConfigurationContext() {
        return OAuth20ConfigurationContext.builder()
            .registeredServiceCipherExecutor(oauthRegisteredServiceCipherExecutor.getObject())
            .sessionStore(oauthDistributedSessionStore.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .accessTokenFactory(defaultAccessTokenFactory.getObject())
            .deviceTokenFactory(defaultDeviceTokenFactory.getObject())
            .clientRegistrationRequestSerializer(clientRegistrationRequestSerializer())
            .clientIdGenerator(new DefaultRandomStringGenerator())
            .clientSecretGenerator(new DefaultRandomStringGenerator())
            .principalFactory(oidcPrincipalFactory())
            .webApplicationServiceServiceFactory(webApplicationServiceFactory.getObject())
            .casProperties(casProperties)
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .resourceLoader(resourceLoader)
            .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator.getObject())
            .oauthConfig(oauthSecConfig.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .callbackAuthorizeViewResolver(callbackAuthorizeViewResolver())
            .profileScopeToAttributesFilter(profileScopeToAttributesFilter())
            .accessTokenGenerator(oauthTokenGenerator.getObject())
            .accessTokenResponseGenerator(oidcAccessTokenResponseGenerator())
            .accessTokenExpirationPolicy(accessTokenExpirationPolicy.getObject())
            .deviceTokenExpirationPolicy(deviceTokenExpirationPolicy.getObject())
            .accessTokenGrantRequestValidators(oauthTokenRequestValidators.getObject())
            .userProfileDataCreator(oidcUserProfileDataCreator())
            .userProfileViewRenderer(oidcUserProfileViewRenderer())
            .oAuthCodeFactory(defaultOAuthCodeFactory.getObject())
            .consentApprovalViewResolver(consentApprovalViewResolver())
            .authenticationBuilder(authenticationBuilder.getObject())
            .oauthAuthorizationResponseBuilders(oauthAuthorizationResponseBuilders.getObject())
            .oauthRequestValidators(oauthRequestValidators.getObject())
            .singleLogoutServiceLogoutUrlBuilder(singleLogoutServiceLogoutUrlBuilder.getObject())
            .idTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder.getObject())
            .build();
    }
}
