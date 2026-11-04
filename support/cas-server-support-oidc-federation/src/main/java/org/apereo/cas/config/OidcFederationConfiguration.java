package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.oidc.discovery.OidcFederationEntityMetadataContributor;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticClientRegistration;
import org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticRegistrationInterceptor;
import org.apereo.cas.oidc.federation.signature.OidcFederationDefaultEntityStatementService;
import org.apereo.cas.oidc.federation.signature.OidcFederationDefaultJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationDefaultTrustMarkIssuer;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationDefaultTrustMarkValidator;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkIssuer;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkValidator;
import org.apereo.cas.oidc.federation.validator.OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.oidc.federation.web.OidcFederationJwksEndpoint;
import org.apereo.cas.oidc.federation.web.OidcFetchFederationEndpointController;
import org.apereo.cas.oidc.federation.web.OidcHistoricalKeysFederationEndpointController;
import org.apereo.cas.oidc.federation.web.OidcListFederationEndpointController;
import org.apereo.cas.oidc.federation.web.OidcTrustMarkFederationEndpointController;
import org.apereo.cas.oidc.federation.web.OidcWellKnownFederationEndpointController;
import org.apereo.cas.oidc.issuer.OidcDefaultIssuerService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.web.OidcRequestObjectDecrypter;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.apereo.cas.oidc.OidcConstants.*;

/**
 * This is {@link OidcFederationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "OidcFederationConfiguration", proxyBeanMethods = false)
class OidcFederationConfiguration {
    /**
     * Ahead of the OAuth/OIDC interceptors (order 100), after authentication throttling (order 0).
     */
    private static final int AUTOMATIC_REGISTRATION_INTERCEPTOR_ORDER = 50;

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcFederationSubordinateRepository")
    public OidcFederationSubordinateRepository oidcFederationSubordinateRepository(
        final CasConfigurationProperties casProperties) {
        return new OidcFederationSubordinateRepository(casProperties.getAuthn().getOidc());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcFederationIssuerService")
    public OidcIssuerService oidcFederationIssuerService(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final CasConfigurationProperties casProperties) {
        return new OidcDefaultIssuerService(casProperties.getAuthn().getOidc(), tenantExtractor);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcFetchFederationController")
    @Bean
    public OidcFetchFederationEndpointController oidcFetchFederationController(
        final OidcFederationSubordinateRepository oidcFederationSubordinateRepository,
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        final OidcFederationEntityStatementService oidcFederationEntityStatementService,
        final CasConfigurationProperties casProperties) {
        return new OidcFetchFederationEndpointController(oidcFederationSubordinateRepository, oidcFederationIssuerService,
                oidcFederationEntityStatementService, casProperties.getAuthn().getOidc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcListFederationController")
    @Bean
    public OidcListFederationEndpointController oidcListFederationController(
            final OidcFederationSubordinateRepository oidcFederationSubordinateRepository,
            @Qualifier("oidcFederationIssuerService")
            final OidcIssuerService oidcFederationIssuerService,
            @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
            final OidcFederationEntityStatementService oidcFederationEntityStatementService,
            final CasConfigurationProperties casProperties) {
        return new OidcListFederationEndpointController(oidcFederationSubordinateRepository, oidcFederationIssuerService,
                oidcFederationEntityStatementService, casProperties.getAuthn().getOidc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcWellKnownFederationController")
    @Bean
    public OidcWellKnownFederationEndpointController oidcWellKnownFederationController(
            @Qualifier("oidcFederationIssuerService")
            final OidcIssuerService oidcFederationIssuerService,
            @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
            final OidcFederationEntityStatementService oidcFederationEntityStatementService,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final ObjectProvider<OidcServerDiscoverySettings> oidcServerDiscoverySettings,
            final ObjectProvider<OidcFederationEntityMetadataContributor> oidcFederationEntityMetadataContributors,
            final CasConfigurationProperties casProperties) {
        return new OidcWellKnownFederationEndpointController(oidcServerDiscoverySettings, oidcFederationEntityMetadataContributors,
                oidcFederationIssuerService, oidcFederationEntityStatementService, casProperties.getAuthn().getOidc());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = OidcFederationJsonWebKeystoreService.BEAN_NAME)
    public OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService(
        final CasConfigurationProperties casProperties)
            throws Exception {
        return new OidcFederationDefaultJsonWebKeystoreService(casProperties.getAuthn().getOidc());
    }

    @ConditionalOnMissingBean(name = OidcFederationTrustMarkIssuer.BEAN_NAME)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OidcFederationTrustMarkIssuer oidcFederationTrustMarkIssuer(
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService,
        final CasConfigurationProperties casProperties) {
        return new OidcFederationDefaultTrustMarkIssuer(casProperties.getAuthn().getOidc().getFederation(),
            oidcFederationIssuerService, oidcFederationWebKeystoreService);
    }

    @ConditionalOnMissingBean(name = "oidcTrustMarkFederationController")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OidcTrustMarkFederationEndpointController oidcTrustMarkFederationController(
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        final OidcFederationEntityStatementService oidcFederationEntityStatementService,
        @Qualifier(OidcFederationTrustMarkIssuer.BEAN_NAME)
        final OidcFederationTrustMarkIssuer oidcFederationTrustMarkIssuer,
        final CasConfigurationProperties casProperties) {
        return new OidcTrustMarkFederationEndpointController(oidcFederationIssuerService,
            oidcFederationEntityStatementService, casProperties.getAuthn().getOidc(), oidcFederationTrustMarkIssuer);
    }

    @ConditionalOnMissingBean(name = "oidcHistoricalKeysFederationController")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OidcHistoricalKeysFederationEndpointController oidcHistoricalKeysFederationController(
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        final OidcFederationEntityStatementService oidcFederationEntityStatementService,
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService,
        final CasConfigurationProperties casProperties) {
        return new OidcHistoricalKeysFederationEndpointController(oidcFederationIssuerService,
            oidcFederationEntityStatementService, casProperties.getAuthn().getOidc(), oidcFederationWebKeystoreService);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OidcFederationJwksEndpoint oidcFederationJwksEndpoint(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final ObjectProvider<OidcFederationJsonWebKeystoreService> oidcFederationWebKeystoreService) {
        return new OidcFederationJwksEndpoint(casProperties, applicationContext, oidcFederationWebKeystoreService);
    }

    @ConditionalOnMissingBean(name = "oidcFederationKeystoreRotationScheduler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public Runnable oidcFederationKeystoreRotationScheduler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final ObjectProvider<OidcFederationJsonWebKeystoreService> oidcFederationWebKeystoreService) {
        return BeanSupplier.of(Runnable.class)
            .when(BeanCondition.on("cas.authn.oidc.federation.rotation.schedule.enabled").isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new OidcFederationKeystoreRotationScheduler(oidcFederationWebKeystoreService.getObject()))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "oidcFederationKeystoreRevocationScheduler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public Runnable oidcFederationKeystoreRevocationScheduler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final ObjectProvider<OidcFederationJsonWebKeystoreService> oidcFederationWebKeystoreService) {
        return BeanSupplier.of(Runnable.class)
            .when(BeanCondition.on("cas.authn.oidc.federation.revocation.schedule.enabled").isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new OidcFederationKeystoreRevocationScheduler(oidcFederationWebKeystoreService.getObject()))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = OidcFederationEntityStatementService.BEAN_NAME)
    public OidcFederationEntityStatementService oidcFederationEntityStatementService(
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        final OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService,
        final CasConfigurationProperties casProperties) {
        return new OidcFederationDefaultEntityStatementService(
            oidcFederationWebKeystoreService,
            casProperties.getAuthn().getOidc());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcFederationTrustChainResolver.BEAN_NAME)
    public OidcFederationTrustChainResolver oidcFederationTrustChainResolver(
        @Qualifier(OidcFederationTrustMarkValidator.BEAN_NAME)
        final OidcFederationTrustMarkValidator oidcFederationTrustMarkValidator,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val trustChainResolvers = OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers(
            casProperties.getAuthn().getOidc().getFederation());
        val resolvers = applicationContext.getBeansOfType(TrustChainResolver.class).values();
        trustChainResolvers.addAll(resolvers);

        return new OidcFederationDefaultTrustChainResolver(trustChainResolvers, oidcFederationTrustMarkValidator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcFederationTrustMarkValidator.BEAN_NAME)
    public OidcFederationTrustMarkValidator oidcFederationTrustMarkValidator(final CasConfigurationProperties casProperties) {
        return new OidcFederationDefaultTrustMarkValidator(casProperties.getAuthn().getOidc().getFederation());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcFederationAutomaticClientRegistration.BEAN_NAME)
    public OidcFederationAutomaticClientRegistration oidcFederationAutomaticClientRegistration(
        final CasConfigurationProperties casProperties,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(OidcFederationTrustChainResolver.BEAN_NAME)
        final OidcFederationTrustChainResolver oidcFederationTrustChainResolver) {
        val retention = Beans.newDuration(casProperties.getAuthn().getOidc().getFederation().getUnresolvedEntityRetention());
        return new OidcFederationAutomaticClientRegistration(servicesManager, oidcFederationTrustChainResolver, retention);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcFederationAutomaticRegistrationInterceptor")
    public HandlerInterceptor oidcFederationAutomaticRegistrationInterceptor(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(OidcFederationAutomaticClientRegistration.BEAN_NAME)
        final OidcFederationAutomaticClientRegistration oidcFederationAutomaticClientRegistration,
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(TicketFactory.BEAN_NAME)
        final TicketFactory ticketFactory,
        @Qualifier(OidcRequestObjectDecrypter.BEAN_NAME)
        final ObjectProvider<OidcRequestObjectDecrypter> oidcRequestObjectDecrypter) {
        return new OidcFederationAutomaticRegistrationInterceptor(oidcFederationAutomaticClientRegistration,
            oidcFederationIssuerService, ticketRegistry, ticketFactory, applicationContext, oidcRequestObjectDecrypter);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcFederationWebMvcConfigurer")
    public WebMvcConfigurer oidcFederationWebMvcConfigurer(
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcFederationIssuerService,
        @Qualifier("oidcFederationAutomaticRegistrationInterceptor")
        final ObjectProvider<HandlerInterceptor> oidcFederationAutomaticRegistrationInterceptor,
        final CasConfigurationProperties casProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull final InterceptorRegistry registry) {
                val role = casProperties.getAuthn().getOidc().getFederation().getRole();
                if (role != null && role.isOpenIdProvider()) {
                    val baseEndpoint = getOidcBaseEndpoint(oidcFederationIssuerService, casProperties);
                    registry.addInterceptor(new RefreshableHandlerInterceptor(oidcFederationAutomaticRegistrationInterceptor))
                        .order(AUTOMATIC_REGISTRATION_INTERCEPTOR_ORDER)
                        .addPathPatterns(baseEndpoint.concat("/**"));
                }
            }
        };
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "openIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator")
    public OAuth20AuthorizationRequestValidator openIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator(
        final CasConfigurationProperties casProperties,
        @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
        final ObjectProvider<OAuth20RequestParameterResolver> oauthRequestParameterResolver,
        @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(OidcFederationAutomaticClientRegistration.BEAN_NAME)
        final OidcFederationAutomaticClientRegistration oidcFederationAutomaticClientRegistration) {
        val responseTypesSupported = casProperties.getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
        return BeanSupplier.of(OAuth20AuthorizationRequestValidator.class)
            .when(() -> responseTypesSupported.contains(OAuth20ResponseTypes.CODE.getType())
                && oauthRequestParameterResolver.getIfAvailable() != null
                && casProperties.getAuthn().getOidc().getFederation().getRole().isOpenIdProvider())
            .supply(() -> new OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager,
                webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer, oauthRequestParameterResolver.getObject(),
                oidcFederationAutomaticClientRegistration))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcFederationProtocolEndpointConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebSecurityConfigurer<HttpSecurity> oidcFederationProtocolEndpointConfigurer(
        @Qualifier("oidcFederationIssuerService")
        final OidcIssuerService oidcIssuerService,
        final CasConfigurationProperties casProperties) {
        
        val role = casProperties.getAuthn().getOidc().getFederation().getRole();
        Objects.requireNonNull(role, () -> "Federation role is not defined in CAS configuration. "
            + "You MUST configure the federation role via cas.authn.oidc.federation.role=... where the value "
            + "may be one of " + Arrays.toString(OidcFederationRole.values()));

        val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService, casProperties);
        val endpoints = new ArrayList<String>();
        endpoints.add(baseEndpoint + '/' + WELL_KNOWN_OPENID_FEDERATION_URL);
        endpoints.add(baseEndpoint + HISTORICAL_KEYS_FEDERATION_URL);
        endpoints.add(baseEndpoint + TRUST_MARK_FEDERATION_URL);
        endpoints.add(baseEndpoint + TRUST_MARK_STATUS_FEDERATION_URL);
        endpoints.add(baseEndpoint + TRUST_MARK_LIST_FEDERATION_URL);

        if (role.isTrustAnchorOrIntermediate()) {
            endpoints.add(baseEndpoint + FETCH_FEDERATION_URL);
            endpoints.add(baseEndpoint + LIST_FEDERATION_URL);
        }

        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return endpoints;
            }
        };
    }

    private static String getOidcBaseEndpoint(final OidcIssuerService issuerService,
                                              final CasConfigurationProperties casProperties) {
        val issuer = issuerService.determineIssuer(Optional.empty());
        val endpoint = Strings.CI.remove(issuer, casProperties.getServer().getPrefix());
        return Strings.CI.prependIfMissing(endpoint, "/");
    }

    @RequiredArgsConstructor
    @Slf4j
    static class OidcFederationKeystoreRotationScheduler implements Runnable {
        private final OidcFederationJsonWebKeystoreService keystoreService;

        @Scheduled(
            cron = "${cas.authn.oidc.federation.rotation.schedule.cron-expression:}",
            zone = "${cas.authn.oidc.federation.rotation.schedule.cron-time-zone:}",
            initialDelayString = "${cas.authn.oidc.federation.rotation.schedule.start-delay:PT60S}",
            fixedDelayString = "${cas.authn.oidc.federation.rotation.schedule.repeat-interval:P90D}")
        @Override
        public void run() {
            FunctionUtils.doUnchecked(_ -> {
                LOGGER.info("Starting to rotate keys in the OpenID Connect Federation keystore...");
                keystoreService.rotate();
            });
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    static class OidcFederationKeystoreRevocationScheduler implements Runnable {
        private final OidcFederationJsonWebKeystoreService keystoreService;

        @Scheduled(
            cron = "${cas.authn.oidc.federation.revocation.schedule.cron-expression:}",
            zone = "${cas.authn.oidc.federation.revocation.schedule.cron-time-zone:}",
            initialDelayString = "${cas.authn.oidc.federation.revocation.schedule.start-delay:PT60S}",
            fixedDelayString = "${cas.authn.oidc.federation.revocation.schedule.repeat-interval:P14D}")
        @Override
        public void run() {
            FunctionUtils.doUnchecked(_ -> {
                LOGGER.info("Starting to retire previous keys in the OpenID Connect Federation keystore...");
                keystoreService.revoke();
            });
        }
    }
}
