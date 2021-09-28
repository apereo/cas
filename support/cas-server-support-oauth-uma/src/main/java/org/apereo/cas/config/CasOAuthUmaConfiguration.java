package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.claim.DefaultUmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettings;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettingsFactory;
import org.apereo.cas.uma.ticket.permission.DefaultUmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketExpirationPolicyBuilder;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.ticket.resource.repository.impl.DefaultResourceSetRepository;
import org.apereo.cas.uma.ticket.rpt.UmaIdTokenGeneratorService;
import org.apereo.cas.uma.ticket.rpt.UmaRequestingPartyTokenSigningService;
import org.apereo.cas.uma.web.authn.UmaAuthorizationApiTokenAuthenticator;
import org.apereo.cas.uma.web.authn.UmaRequestingPartyTokenAuthenticator;
import org.apereo.cas.uma.web.controllers.authz.UmaAuthorizationRequestEndpointController;
import org.apereo.cas.uma.web.controllers.claims.UmaRequestingPartyClaimsCollectionEndpointController;
import org.apereo.cas.uma.web.controllers.discovery.UmaWellKnownEndpointController;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaCreatePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaDeletePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaFindPolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaUpdatePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaCreateResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaDeleteResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaFindResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaUpdateResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.rpt.UmaRequestingPartyTokenJwksEndpointController;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuth20Constants.*;

/**
 * This is {@link CasOAuthUmaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casOAuthUmaConfiguration", proxyBeanMethods = false)
public class CasOAuthUmaConfiguration {

    private static SecurityInterceptor getSecurityInterceptor(final Authenticator authenticator,
                                                              final String clientName,
                                                              final SessionStore oauthDistributedSessionStore,
                                                              final CasConfigurationProperties casProperties) {
        val headerClient = new HeaderClient(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER.concat(" "), authenticator);
        headerClient.setName(clientName);
        val clients = Stream.of(headerClient.getName()).collect(Collectors.joining(","));
        val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), headerClient);
        config.setSessionStore(oauthDistributedSessionStore);
        val interceptor = new SecurityInterceptor(config, clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Bean
    @Autowired
    public UmaConfigurationContext umaConfigurationContext(
        @Qualifier("defaultUmaPermissionTicketFactory")
        final UmaPermissionTicketFactory defaultUmaPermissionTicketFactory,
        @Qualifier("umaResourceSetClaimPermissionExaminer")
        final UmaResourceSetClaimPermissionExaminer umaResourceSetClaimPermissionExaminer,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("oauthDistributedSessionStore")
        final SessionStore oauthDistributedSessionStore,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        @Qualifier("umaRequestingPartyTokenGenerator")
        final IdTokenGeneratorService umaRequestingPartyTokenGenerator,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier("umaResourceSetRepository")
        final ResourceSetRepository umaResourceSetRepository,
        final CasConfigurationProperties casProperties) {
        return UmaConfigurationContext.builder()
            .accessTokenGenerator(oauthTokenGenerator)
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .claimPermissionExaminer(umaResourceSetClaimPermissionExaminer)
            .requestingPartyTokenGenerator(umaRequestingPartyTokenGenerator)
            .servicesManager(servicesManager)
            .sessionStore(oauthDistributedSessionStore)
            .ticketRegistry(ticketRegistry)
            .centralAuthenticationService(centralAuthenticationService)
            .umaPermissionTicketFactory(defaultUmaPermissionTicketFactory)
            .umaResourceSetRepository(umaResourceSetRepository)
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "umaServerDiscoverySettingsFactory")
    @Autowired
    public FactoryBean<UmaServerDiscoverySettings> umaServerDiscoverySettingsFactory(final CasConfigurationProperties casProperties) {
        return new UmaServerDiscoverySettingsFactory(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "umaResourceSetClaimPermissionExaminer")
    @RefreshScope
    public UmaResourceSetClaimPermissionExaminer umaResourceSetClaimPermissionExaminer() {
        return new DefaultUmaResourceSetClaimPermissionExaminer();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "umaRequestingPartyTokenGenerator")
    @Autowired
    public IdTokenGeneratorService umaRequestingPartyTokenGenerator(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier("oauthDistributedSessionCookieGenerator")
        final CasCookieBuilder oauthDistributedSessionCookieGenerator,
        @Qualifier("oauthDistributedSessionStore")
        final SessionStore oauthDistributedSessionStore,
        @Qualifier("oauthTokenGenerator")
        final OAuth20TokenGenerator oauthTokenGenerator) {
        val uma = casProperties.getAuthn().getOauth().getUma();
        val jwks = uma.getRequestingPartyToken().getJwksFile().getLocation();
        val signingService = new UmaRequestingPartyTokenSigningService(jwks, uma.getCore().getIssuer());
        val context = OAuth20ConfigurationContext.builder()
            .ticketRegistry(ticketRegistry)
            .servicesManager(servicesManager)
            .idTokenSigningAndEncryptionService(signingService)
            .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator)
            .sessionStore(oauthDistributedSessionStore)
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .accessTokenGenerator(oauthTokenGenerator)
            .applicationContext(applicationContext).build();
        return new UmaIdTokenGeneratorService(context);
    }

    @Bean
    @Autowired
    public UmaAuthorizationRequestEndpointController umaAuthorizationRequestEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaAuthorizationRequestEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaRequestingPartyTokenJwksEndpointController umaRequestingPartyTokenJwksEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaRequestingPartyTokenJwksEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaRequestingPartyClaimsCollectionEndpointController umaRequestingPartyClaimsCollectionEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaRequestingPartyClaimsCollectionEndpointController(umaConfigurationContext);
    }

    @Autowired
    @Bean
    public UmaWellKnownEndpointController umaWellKnownEndpointController(
        @Qualifier("umaServerDiscoverySettingsFactory")
        final UmaServerDiscoverySettings discoverySettings) {
        return new UmaWellKnownEndpointController(discoverySettings);
    }

    @Bean
    @Autowired
    public UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaPermissionRegistrationEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaCreateResourceSetRegistrationEndpointController umaCreateResourceSetRegistrationEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaCreateResourceSetRegistrationEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaDeleteResourceSetRegistrationEndpointController umaDeleteResourceSetRegistrationEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaDeleteResourceSetRegistrationEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaUpdateResourceSetRegistrationEndpointController umaUpdateResourceSetRegistrationEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaUpdateResourceSetRegistrationEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaFindResourceSetRegistrationEndpointController umaFindResourceSetRegistrationEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaFindResourceSetRegistrationEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaCreatePolicyForResourceSetEndpointController umaCreatePolicyForResourceSetEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaCreatePolicyForResourceSetEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaDeletePolicyForResourceSetEndpointController umaDeletePolicyForResourceSetEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaDeletePolicyForResourceSetEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaUpdatePolicyForResourceSetEndpointController umaUpdatePolicyForResourceSetEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaUpdatePolicyForResourceSetEndpointController(umaConfigurationContext);
    }

    @Bean
    @Autowired
    public UmaFindPolicyForResourceSetEndpointController umaFindPolicyForResourceSetEndpointController(
        @Qualifier("umaConfigurationContext")
        final UmaConfigurationContext umaConfigurationContext) {
        return new UmaFindPolicyForResourceSetEndpointController(umaConfigurationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "umaResourceSetRepository")
    public ResourceSetRepository umaResourceSetRepository() {
        return new DefaultResourceSetRepository();
    }

    @ConditionalOnMissingBean(name = "umaPermissionTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator umaPermissionTicketIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "umaPermissionTicketExpirationPolicy")
    @Autowired
    public ExpirationPolicyBuilder umaPermissionTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
        return new UmaPermissionTicketExpirationPolicyBuilder(casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultUmaPermissionTicketFactory")
    public UmaPermissionTicketFactory defaultUmaPermissionTicketFactory(
        @Qualifier("umaPermissionTicketIdGenerator")
        final UniqueTicketIdGenerator umaPermissionTicketIdGenerator,
        @Qualifier("umaPermissionTicketExpirationPolicy")
        final ExpirationPolicyBuilder umaPermissionTicketExpirationPolicy) {
        return new DefaultUmaPermissionTicketFactory(umaPermissionTicketIdGenerator, umaPermissionTicketExpirationPolicy);
    }

    @ConditionalOnMissingBean(name = "defaultUmaPermissionTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultUmaPermissionTicketFactoryConfigurer(
        @Qualifier("defaultUmaPermissionTicketFactory")
        final UmaPermissionTicketFactory defaultUmaPermissionTicketFactory) {
        return () -> defaultUmaPermissionTicketFactory;
    }

    @Bean
    @Autowired
    public SecurityInterceptor umaRequestingPartyTokenSecurityInterceptor(
        final CasConfigurationProperties casProperties,
        @Qualifier("oauthDistributedSessionStore")
        final SessionStore oauthDistributedSessionStore,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder) {
        val authenticator = new UmaRequestingPartyTokenAuthenticator(centralAuthenticationService, accessTokenJwtBuilder);
        return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_RPT_AUTH", oauthDistributedSessionStore, casProperties);
    }

    @Bean
    @Autowired
    public SecurityInterceptor umaAuthorizationApiTokenSecurityInterceptor(
        final CasConfigurationProperties casProperties,
        @Qualifier("oauthDistributedSessionStore")
        final SessionStore oauthDistributedSessionStore,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("accessTokenJwtBuilder")
        final JwtBuilder accessTokenJwtBuilder) {
        val authenticator = new UmaAuthorizationApiTokenAuthenticator(centralAuthenticationService, accessTokenJwtBuilder);
        return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_AAT_AUTH",
            oauthDistributedSessionStore, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "umaWebMvcConfigurer")
    public WebMvcConfigurer umaWebMvcConfigurer(
        @Qualifier("umaAuthorizationApiTokenSecurityInterceptor")
        final SecurityInterceptor umaAuthorizationApiTokenSecurityInterceptor,
        @Qualifier("umaRequestingPartyTokenSecurityInterceptor")
        final SecurityInterceptor umaRequestingPartyTokenSecurityInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(umaRequestingPartyTokenSecurityInterceptor).order(100)
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_PERMISSION_URL).concat("*"))
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL).concat("*"))
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/*/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_CLAIMS_COLLECTION_URL).concat("*"));
                registry.addInterceptor(umaAuthorizationApiTokenSecurityInterceptor).order(100)
                    .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL).concat("*"));
            }
        };
    }

}
