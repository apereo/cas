package org.apereo.cas.oidc.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.mapping.DefaultOidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactory;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequestSerializer;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.profile.OidcProfileScopeToAttributesFilter;
import org.apereo.cas.oidc.profile.OidcRegisteredServicePreProcessorEventListener;
import org.apereo.cas.oidc.profile.OidcUserProfileDataCreator;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionService;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.oidc.web.OidcCallbackAuthorizeViewResolver;
import org.apereo.cas.oidc.web.OidcCasClientRedirectActionBuilder;
import org.apereo.cas.oidc.web.OidcConsentApprovalViewResolver;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAuthorizationResponseBuilder;
import org.apereo.cas.oidc.web.OidcSecurityInterceptor;
import org.apereo.cas.oidc.web.controllers.OidcAccessTokenEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcIntrospectionEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcJwksEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcRevocationEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcUserProfileEndpointController;
import org.apereo.cas.oidc.web.controllers.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.flow.OidcAuthenticationContextWebflowEventResolver;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIAction;
import org.apereo.cas.oidc.web.flow.OidcWebflowConfigurer;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcConfiguration implements WebMvcConfigurer, CasWebflowExecutionPlanConfigurer {

    @Autowired
    @Qualifier("accessTokenGrantAuditableRequestExtractor")
    private AuditableExecution accessTokenGrantAuditableRequestExtractor;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("oauthAuthorizationRequestValidators")
    private Set<OAuth20AuthorizationRequestValidator> oauthRequestValidators;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    private OAuth20TokenGenerator oauthTokenGenerator;

    @Autowired
    @Qualifier("oauthAuthorizationResponseBuilders")
    private Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("accessTokenExpirationPolicy")
    private ExpirationPolicy accessTokenExpirationPolicy;

    @Autowired
    @Qualifier("deviceTokenExpirationPolicy")
    private ExpirationPolicy deviceTokenExpirationPolicy;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private SecurityInterceptor requiresAuthenticationAccessTokenInterceptor;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private ObjectProvider<MultifactorAuthenticationProviderSelector> multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private OAuth20CasAuthenticationBuilder authenticationBuilder;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy oauth20AuthenticationServiceSelectionStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory defaultOAuthCodeFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("oauthUserProfileViewRenderer")
    private OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @Autowired
    @Qualifier("oauthTokenRequestValidators")
    private Collection<OAuth20TokenRequestValidator> oauthTokenRequestValidators;

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
        return new OidcCallbackAuthorizeViewResolver(oidcAuthorizationRequestSupport());
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor() {
        val clients = Stream.of(
            Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
            Authenticators.CAS_OAUTH_CLIENT_USER_FORM).collect(Collectors.joining(","));
        return new SecurityInterceptor(oauthSecConfig, clients);
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor() {
        val name = oauthSecConfig.getClients().findClient(CasClient.class).getName();
        return new OidcSecurityInterceptor(oauthSecConfig, name, oidcAuthorizationRequestSupport());
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @RefreshScope
    @Bean
    public OidcIdTokenGeneratorService oidcIdTokenGenerator() {
        return new OidcIdTokenGeneratorService(
            casProperties,
            oidcTokenSigningAndEncryptionService(),
            servicesManager);
    }

    @Bean
    @RefreshScope
    public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator());
    }

    @Bean
    public OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport() {
        return new OidcAuthorizationRequestSupport(ticketGrantingTicketCookieGenerator.getIfAvailable(), ticketRegistrySupport);
    }

    @ConditionalOnMissingBean(name = "oidcPrincipalFactory")
    @Bean
    public PrincipalFactory oidcPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper() {
        val mappings = casProperties.getAuthn().getOidc().getClaimsMap();
        return new DefaultOidcAttributeToScopeClaimMapper(mappings);
    }

    @Bean
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory(), servicesManager,
            userDefinedScopeBasedAttributeReleasePolicies(), casProperties);
    }

    @RefreshScope
    @Bean
    public OidcIntrospectionEndpointController oidcIntrospectionEndpointController() {
        return new OidcIntrospectionEndpointController(
            servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            centralAuthenticationService,
            registeredServiceAccessStrategyEnforcer);
    }

    @RefreshScope
    @Bean
    public OidcRevocationEndpointController oidcRevocationEndpointController() {
        return new OidcRevocationEndpointController(
            servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer);
    }

    @RefreshScope
    @Bean
    public OidcAccessTokenEndpointController oidcAccessTokenController() {
        return new OidcAccessTokenEndpointController(
            servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            oauthTokenGenerator,
            oidcAccessTokenResponseGenerator(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            accessTokenExpirationPolicy,
            deviceTokenExpirationPolicy,
            oauthTokenRequestValidators,
            accessTokenGrantAuditableRequestExtractor);
    }

    @ConditionalOnMissingBean(name = "clientRegistrationRequestSerializer")
    @Bean
    public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer() {
        return new OidcClientRegistrationRequestSerializer();
    }

    @RefreshScope
    @Bean
    public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController() {
        return new OidcDynamicClientRegistrationEndpointController(
            servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            clientRegistrationRequestSerializer(),
            new DefaultRandomStringGenerator(),
            new DefaultRandomStringGenerator(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcJwksEndpointController oidcJwksController() {
        return new OidcJwksEndpointController(servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(), casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @Autowired
    @RefreshScope
    @Bean
    public OidcWellKnownEndpointController oidcWellKnownController(@Qualifier("oidcServerDiscoverySettingsFactory") final OidcServerDiscoverySettings discoverySettings) {
        return new OidcWellKnownEndpointController(servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            discoverySettings,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcUserProfileEndpointController oidcProfileController() {
        return new OidcUserProfileEndpointController(servicesManager, ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(), webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            oauthUserProfileViewRenderer, oidcUserProfileDataCreator());
    }

    @Bean
    public OAuth20UserProfileDataCreator oidcUserProfileDataCreator() {
        return new OidcUserProfileDataCreator(servicesManager, profileScopeToAttributesFilter());
    }

    @RefreshScope
    @Bean
    public OidcAuthorizeEndpointController oidcAuthorizeController() {
        return new OidcAuthorizeEndpointController(servicesManager,
            ticketRegistry,
            defaultAccessTokenFactory,
            oidcPrincipalFactory(),
            webApplicationServiceFactory,
            defaultOAuthCodeFactory,
            consentApprovalViewResolver(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            authenticationBuilder,
            oauthAuthorizationResponseBuilders,
            oauthRequestValidators,
            registeredServiceAccessStrategyEnforcer);
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver() {
        val r = new OidcAuthenticationContextWebflowEventResolver(
            authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService,
            servicesManager,
            ticketRegistrySupport,
            warnCookieGenerator,
            authenticationRequestServiceSelectionStrategies,
            multifactorAuthenticationProviderSelector.getIfAvailable());

        this.initialAuthenticationAttemptWebflowEventResolver.addDelegate(r);
        return r;
    }

    @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer oidcWebflowConfigurer() {
        val cfg = new OidcWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, oidcRegisteredServiceUIAction(), applicationContext, casProperties);
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        return cfg;
    }

    @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
    @Bean
    public Action oidcRegisteredServiceUIAction() {
        return new OidcRegisteredServiceUIAction(this.servicesManager, oauth20AuthenticationServiceSelectionStrategy);
    }

    @Bean
    public OidcIdTokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService() {
        val oidc = casProperties.getAuthn().getOidc();
        return new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache(),
            oidcServiceJsonWebKeystoreCache(),
            oidc.getIssuer());
    }

    @Bean
    public LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> oidcServiceJsonWebKeystoreCache() {
        val oidc = casProperties.getAuthn().getOidc();
        val cache =
            Caffeine.newBuilder().maximumSize(1)
                .expireAfterWrite(oidc.getJwksCacheInMinutes(), TimeUnit.MINUTES)
                .build(oidcServiceJsonWebKeystoreCacheLoader());
        return cache;
    }

    @Bean
    public LoadingCache<String, Optional<RsaJsonWebKey>> oidcDefaultJsonWebKeystoreCache() {
        val oidc = casProperties.getAuthn().getOidc();
        val cache =
            Caffeine.newBuilder().maximumSize(1)
                .expireAfterWrite(oidc.getJwksCacheInMinutes(), TimeUnit.MINUTES)
                .build(oidcDefaultJsonWebKeystoreCacheLoader());
        return cache;
    }

    @Bean
    public OidcDefaultJsonWebKeystoreCacheLoader oidcDefaultJsonWebKeystoreCacheLoader() {
        return new OidcDefaultJsonWebKeystoreCacheLoader(casProperties.getAuthn().getOidc().getJwksFile());
    }

    @Bean
    public CacheLoader<OidcRegisteredService, Optional<RsaJsonWebKey>> oidcServiceJsonWebKeystoreCacheLoader() {
        return new OidcServiceJsonWebKeystoreCacheLoader(resourceLoader);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcServerDiscoverySettingsFactory")
    public FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory() {
        return new OidcServerDiscoverySettingsFactory(casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
    public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
        val s = new OidcJsonWebKeystoreGeneratorService(casProperties.getAuthn().getOidc());
        s.generate();
        return s;
    }

    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        val oidc = casProperties.getAuthn().getOidc();
        val mode =
            OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
                oidc.getDynamicClientRegistrationMode(),
                OidcConstants.DynamicClientRegistrationMode.PROTECTED.name()));

        return new OidcHandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor,
            requiresAuthenticationAuthorizeInterceptor(),
            requiresAuthenticationDynamicRegistrationInterceptor(),
            mode, accessTokenGrantRequestExtractors);
    }

    @RefreshScope
    @Bean
    public Collection<BaseOidcScopeAttributeReleasePolicy> userDefinedScopeBasedAttributeReleasePolicies() {
        val oidc = casProperties.getAuthn().getOidc();
        return oidc.getUserDefinedScopes().entrySet()
            .stream()
            .map(k -> new OidcCustomScopeAttributeReleasePolicy(k.getKey(), CollectionUtils.wrapList(k.getValue().split(","))))
            .collect(Collectors.toSet());
    }

    @Bean
    public OidcRegisteredServicePreProcessorEventListener oidcRegisteredServicePreProcessorEventListener() {
        return new OidcRegisteredServicePreProcessorEventListener(profileScopeToAttributesFilter());
    }

    @Bean
    public OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder() {
        return new OidcImplicitIdTokenAuthorizationResponseBuilder(oidcIdTokenGenerator(), oauthTokenGenerator,
            accessTokenExpirationPolicy, grantingTicketExpirationPolicy);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(oidcWebflowConfigurer());
    }
}
