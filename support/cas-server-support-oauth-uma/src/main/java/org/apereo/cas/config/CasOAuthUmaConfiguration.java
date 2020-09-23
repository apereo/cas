package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

/**
 * This is {@link CasOAuthUmaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casOAuthUmaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthUmaConfiguration implements WebMvcConfigurer {

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("oauthDistributedSessionCookieGenerator")
    private ObjectProvider<CasCookieBuilder> oauthDistributedSessionCookieGenerator;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    private ObjectProvider<SessionStore> oauthDistributedSessionStore;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    private ObjectProvider<OAuth20TokenGenerator> oauthTokenGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "umaServerDiscoverySettingsFactory")
    public FactoryBean<UmaServerDiscoverySettings> umaServerDiscoverySettingsFactory() {
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
    public IdTokenGeneratorService umaRequestingPartyTokenGenerator() {
        val uma = casProperties.getAuthn().getUma();
        val jwks = uma.getRequestingPartyToken().getJwksFile();
        val signingService = new UmaRequestingPartyTokenSigningService(jwks, uma.getIssuer());
        val context = OAuth20ConfigurationContext.builder()
            .ticketRegistry(ticketRegistry.getObject())
            .servicesManager(servicesManager.getObject())
            .idTokenSigningAndEncryptionService(signingService)
            .oauthDistributedSessionCookieGenerator(oauthDistributedSessionCookieGenerator.getObject())
            .sessionStore(oauthDistributedSessionStore.getObject())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder.getObject())
            .accessTokenGenerator(oauthTokenGenerator.getObject())
            .build();
        return new UmaIdTokenGeneratorService(context);
    }

    @Bean
    public UmaAuthorizationRequestEndpointController umaAuthorizationRequestEndpointController() {
        return new UmaAuthorizationRequestEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaRequestingPartyTokenJwksEndpointController umaRequestingPartyTokenJwksEndpointController() {
        return new UmaRequestingPartyTokenJwksEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaRequestingPartyClaimsCollectionEndpointController umaRequestingPartyClaimsCollectionEndpointController() {
        return new UmaRequestingPartyClaimsCollectionEndpointController(buildConfigurationContext().build());
    }

    @Autowired
    @Bean
    public UmaWellKnownEndpointController umaWellKnownEndpointController(@Qualifier("umaServerDiscoverySettingsFactory") final UmaServerDiscoverySettings discoverySettings) {
        return new UmaWellKnownEndpointController(discoverySettings);
    }

    @Bean
    public UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController() {
        return new UmaPermissionRegistrationEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaCreateResourceSetRegistrationEndpointController umaCreateResourceSetRegistrationEndpointController() {
        return new UmaCreateResourceSetRegistrationEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaDeleteResourceSetRegistrationEndpointController umaDeleteResourceSetRegistrationEndpointController() {
        return new UmaDeleteResourceSetRegistrationEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaUpdateResourceSetRegistrationEndpointController umaUpdateResourceSetRegistrationEndpointController() {
        return new UmaUpdateResourceSetRegistrationEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaFindResourceSetRegistrationEndpointController umaFindResourceSetRegistrationEndpointController() {
        return new UmaFindResourceSetRegistrationEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaCreatePolicyForResourceSetEndpointController umaCreatePolicyForResourceSetEndpointController() {
        return new UmaCreatePolicyForResourceSetEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaDeletePolicyForResourceSetEndpointController umaDeletePolicyForResourceSetEndpointController() {
        return new UmaDeletePolicyForResourceSetEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaUpdatePolicyForResourceSetEndpointController umaUpdatePolicyForResourceSetEndpointController() {
        return new UmaUpdatePolicyForResourceSetEndpointController(buildConfigurationContext().build());
    }

    @Bean
    public UmaFindPolicyForResourceSetEndpointController umaFindPolicyForResourceSetEndpointController() {
        return new UmaFindPolicyForResourceSetEndpointController(buildConfigurationContext().build());
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
    public ExpirationPolicyBuilder umaPermissionTicketExpirationPolicy() {
        return new UmaPermissionTicketExpirationPolicyBuilder(casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultUmaPermissionTicketFactory")
    public UmaPermissionTicketFactory defaultUmaPermissionTicketFactory() {
        return new DefaultUmaPermissionTicketFactory(umaPermissionTicketIdGenerator(), umaPermissionTicketExpirationPolicy());
    }

    @Bean
    public SecurityInterceptor umaRequestingPartyTokenSecurityInterceptor() {
        val authenticator = new UmaRequestingPartyTokenAuthenticator(ticketRegistry.getObject(), accessTokenJwtBuilder.getObject());
        return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_RPT_AUTH");
    }

    @Bean
    public SecurityInterceptor umaAuthorizationApiTokenSecurityInterceptor() {
        val authenticator = new UmaAuthorizationApiTokenAuthenticator(ticketRegistry.getObject(), accessTokenJwtBuilder.getObject());
        return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_AAT_AUTH");
    }

    private SecurityInterceptor getSecurityInterceptor(final Authenticator authenticator, final String clientName) {
        val headerClient = new HeaderClient(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER.concat(" "), authenticator);
        headerClient.setName(clientName);
        val clients = Stream.of(headerClient.getName()).collect(Collectors.joining(","));
        val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), headerClient);
        config.setSessionStore(oauthDistributedSessionStore.getObject());
        val interceptor = new SecurityInterceptor(config, clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry
            .addInterceptor(umaRequestingPartyTokenSecurityInterceptor())
            .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_PERMISSION_URL).concat("*"))
            .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL).concat("*"))
            .addPathPatterns(BASE_OAUTH20_URL.concat("/*/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
            .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
            .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_CLAIMS_COLLECTION_URL).concat("*"));

        registry
            .addInterceptor(umaAuthorizationApiTokenSecurityInterceptor())
            .addPathPatterns(BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL).concat("*"));
    }

    private UmaConfigurationContext.UmaConfigurationContextBuilder buildConfigurationContext() {
        return UmaConfigurationContext.builder()
            .accessTokenGenerator(oauthTokenGenerator.getObject())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder.getObject())
            .claimPermissionExaminer(umaResourceSetClaimPermissionExaminer())
            .requestingPartyTokenGenerator(umaRequestingPartyTokenGenerator())
            .servicesManager(servicesManager.getObject())
            .sessionStore(oauthDistributedSessionStore.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .umaPermissionTicketFactory(defaultUmaPermissionTicketFactory())
            .umaResourceSetRepository(umaResourceSetRepository());
    }
}
