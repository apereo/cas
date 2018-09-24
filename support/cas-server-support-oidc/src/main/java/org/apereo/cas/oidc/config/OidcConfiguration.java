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
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
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
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksEndpointController;
import org.apereo.cas.oidc.web.controllers.logout.OidcLogoutEndpointController;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcAccessTokenEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
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
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.IdTokenSigningAndEncryptionService;
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
    private ObjectProvider<ExpirationPolicy> grantingTicketExpirationPolicy;

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
    private ObjectProvider<ExpirationPolicy> accessTokenExpirationPolicy;

    @Autowired
    @Qualifier("deviceTokenExpirationPolicy")
    private ObjectProvider<ExpirationPolicy> deviceTokenExpirationPolicy;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private ObjectProvider<SecurityInterceptor> requiresAuthenticationAccessTokenInterceptor;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private ObjectProvider<MultifactorAuthenticationProviderSelector> multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private ObjectProvider<OAuth20CasAuthenticationBuilder> authenticationBuilder;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

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
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    private ObjectProvider<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private ObjectProvider<AccessTokenFactory> defaultAccessTokenFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private ObjectProvider<OAuthCodeFactory> defaultOAuthCodeFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("oauthUserProfileViewRenderer")
    private ObjectProvider<OAuth20UserProfileViewRenderer> oauthUserProfileViewRenderer;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private ObjectProvider<Collection<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors;

    @Autowired
    @Qualifier("oauthTokenRequestValidators")
    private ObjectProvider<Collection<OAuth20TokenRequestValidator>> oauthTokenRequestValidators;

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
        val clients = String.join(",",
            Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
            Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
        return new SecurityInterceptor(oauthSecConfig.getIfAvailable(), clients);
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor() {
        val name = oauthSecConfig.getIfAvailable().getClients().findClient(CasClient.class).getName();
        return new OidcSecurityInterceptor(oauthSecConfig.getIfAvailable(), name, oidcAuthorizationRequestSupport());
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @RefreshScope
    @Bean
    public IdTokenGeneratorService oidcIdTokenGenerator() {
        return new OidcIdTokenGeneratorService(
            casProperties,
            oidcTokenSigningAndEncryptionService(),
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable());
    }

    @Bean
    @RefreshScope
    public OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        return new OidcAccessTokenResponseGenerator(oidcIdTokenGenerator());
    }

    @Bean
    public OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport() {
        return new OidcAuthorizationRequestSupport(ticketGrantingTicketCookieGenerator.getIfAvailable(), ticketRegistrySupport.getIfAvailable());
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
        return new OidcProfileScopeToAttributesFilter(oidcPrincipalFactory(), servicesManager.getIfAvailable(),
            userDefinedScopeBasedAttributeReleasePolicies(), casProperties);
    }

    @RefreshScope
    @Bean
    public OidcIntrospectionEndpointController oidcIntrospectionEndpointController() {
        return new OidcIntrospectionEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcLogoutEndpointController oidcLogoutEndpointController() {
        return new OidcLogoutEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer.getIfAvailable(),
            oidcTokenSigningAndEncryptionService(),
            singleLogoutServiceLogoutUrlBuilder.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcRevocationEndpointController oidcRevocationEndpointController() {
        return new OidcRevocationEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcAccessTokenEndpointController oidcAccessTokenController() {
        return new OidcAccessTokenEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            oauthTokenGenerator.getIfAvailable(),
            oidcAccessTokenResponseGenerator(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            accessTokenExpirationPolicy.getIfAvailable(),
            deviceTokenExpirationPolicy.getIfAvailable(),
            oauthTokenRequestValidators.getIfAvailable(),
            accessTokenGrantAuditableRequestExtractor.getIfAvailable());
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
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
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
        return new OidcJwksEndpointController(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @Autowired
    @RefreshScope
    @Bean
    public OidcWellKnownEndpointController oidcWellKnownController(@Qualifier("oidcServerDiscoverySettingsFactory") final OidcServerDiscoverySettings discoverySettings) {
        return new OidcWellKnownEndpointController(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            discoverySettings,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public OidcUserProfileEndpointController oidcProfileController() {
        return new OidcUserProfileEndpointController(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            oauthUserProfileViewRenderer.getIfAvailable(),
            oidcUserProfileDataCreator());
    }

    @Bean
    public OAuth20UserProfileDataCreator oidcUserProfileDataCreator() {
        return new OidcUserProfileDataCreator(servicesManager.getIfAvailable(), profileScopeToAttributesFilter());
    }

    @RefreshScope
    @Bean
    public OidcAuthorizeEndpointController oidcAuthorizeController() {
        return new OidcAuthorizeEndpointController(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory.getIfAvailable(),
            oidcPrincipalFactory(),
            webApplicationServiceFactory.getIfAvailable(),
            defaultOAuthCodeFactory.getIfAvailable(),
            consentApprovalViewResolver(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            authenticationBuilder.getIfAvailable(),
            oauthAuthorizationResponseBuilders.getIfAvailable(),
            oauthRequestValidators.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver() {
        val r = new OidcAuthenticationContextWebflowEventResolver(
            authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            multifactorAuthenticationProviderSelector.getIfAvailable());

        this.initialAuthenticationAttemptWebflowEventResolver.getIfAvailable().addDelegate(r);
        return r;
    }

    @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer oidcWebflowConfigurer() {
        val cfg = new OidcWebflowConfigurer(flowBuilderServices.getIfAvailable(),
            loginFlowDefinitionRegistry.getIfAvailable(), oidcRegisteredServiceUIAction(), applicationContext, casProperties);
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry.getIfAvailable());
        return cfg;
    }

    @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
    @Bean
    public Action oidcRegisteredServiceUIAction() {
        return new OidcRegisteredServiceUIAction(this.servicesManager.getIfAvailable(), oauth20AuthenticationServiceSelectionStrategy.getIfAvailable());
    }

    @Bean
    public IdTokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService() {
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

        return new OidcHandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor.getIfAvailable(),
            requiresAuthenticationAuthorizeInterceptor(),
            requiresAuthenticationDynamicRegistrationInterceptor(),
            mode, accessTokenGrantRequestExtractors.getIfAvailable());
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
        return new OidcImplicitIdTokenAuthorizationResponseBuilder(oidcIdTokenGenerator(), oauthTokenGenerator.getIfAvailable(),
            accessTokenExpirationPolicy.getIfAvailable(), grantingTicketExpirationPolicy.getIfAvailable());
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(oidcWebflowConfigurer());
    }
}
