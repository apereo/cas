package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionResponseGenerator;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.claim.DefaultUmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettings;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettingsFactory;
import org.apereo.cas.uma.ticket.permission.DefaultUmaPermissionTicket;
import org.apereo.cas.uma.ticket.permission.DefaultUmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketExpirationPolicyBuilder;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
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
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.SecurityLogicInterceptor;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContextFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.Nonnull;
import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasOAuthUmaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth, module = "uma")
@AutoConfiguration
public class CasOAuthUmaAutoConfiguration {

    @Configuration(value = "CasOAuthUmaTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaTokenConfiguration {
        
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaRequestingPartyTokenGenerator")
        public IdTokenGeneratorService umaRequestingPartyTokenGenerator(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final ObjectProvider<UmaConfigurationContext> context) {
            return new UmaIdTokenGeneratorService(context);
        }
    }

    @Configuration(value = "CasOAuthUmaResourcesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaResourcesConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "umaResourceSetClaimPermissionExaminer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UmaResourceSetClaimPermissionExaminer umaResourceSetClaimPermissionExaminer() {
            return new DefaultUmaResourceSetClaimPermissionExaminer();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaResourceSetRepository")
        public ResourceSetRepository umaResourceSetRepository() {
            return new DefaultResourceSetRepository();
        }

    }

    @Configuration(value = "CasOAuthUmaContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaContextConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaTokenSigningAndEncryptionService")
        public OAuth20TokenSigningAndEncryptionService umaTokenSigningAndEncryptionService(
            final CasConfigurationProperties casProperties) {
            return new UmaRequestingPartyTokenSigningService(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UmaConfigurationContext umaConfigurationContext(
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier("umaTokenSigningAndEncryptionService")
            final OAuth20TokenSigningAndEncryptionService umaTokenSigningAndEncryptionService,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier("umaResourceSetClaimPermissionExaminer")
            final UmaResourceSetClaimPermissionExaminer umaResourceSetClaimPermissionExaminer,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("oauthTokenGenerator")
            final OAuth20TokenGenerator oauthTokenGenerator,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier("umaRequestingPartyTokenGenerator")
            final IdTokenGeneratorService umaRequestingPartyTokenGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("umaResourceSetRepository")
            final ResourceSetRepository umaResourceSetRepository,
            final CasConfigurationProperties casProperties,
            final List<OAuth20IntrospectionResponseGenerator> oauthIntrospectionResponseGenerator,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("taskScheduler")
            final TaskScheduler taskScheduler,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationManager,
            @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
            final CipherExecutor webflowCipherExecutor) {

            return UmaConfigurationContext
                .builder()
                .httpClient(httpClient)
                .communicationsManager(communicationManager)
                .authenticationAttributeReleasePolicy(authenticationAttributeReleasePolicy)
                .applicationContext(applicationContext)
                .accessTokenGenerator(oauthTokenGenerator)
                .casProperties(casProperties)
                .accessTokenJwtBuilder(accessTokenJwtBuilder)
                .claimPermissionExaminer(umaResourceSetClaimPermissionExaminer)
                .requestingPartyTokenGenerator(umaRequestingPartyTokenGenerator)
                .servicesManager(servicesManager)
                .sessionStore(oauthDistributedSessionStore)
                .ticketRegistry(ticketRegistry)
                .centralAuthenticationService(centralAuthenticationService)
                .umaResourceSetRepository(umaResourceSetRepository)
                .idTokenSigningAndEncryptionService(umaTokenSigningAndEncryptionService)
                .ticketFactory(ticketFactory)
                .introspectionResponseGenerator(oauthIntrospectionResponseGenerator)
                .principalResolver(defaultPrincipalResolver)
                .taskScheduler(taskScheduler)
                .webflowCipherExecutor(webflowCipherExecutor)
                .build();
        }
    }

    @Configuration(value = "CasOAuthUmaInterceptorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaInterceptorConfiguration {
        private static SecurityLogicInterceptor getSecurityInterceptor(
            final Authenticator authenticator,
            final String clientName,
            final SessionStore oauthDistributedSessionStore,
            final CasConfigurationProperties casProperties) {
            val headerClient = new HeaderClient(HttpHeaders.AUTHORIZATION,
                OAuth20Constants.TOKEN_TYPE_BEARER.concat(" "), authenticator);
            headerClient.setName(clientName);
            headerClient.init();
            val clients = Stream.of(headerClient.getName()).collect(Collectors.joining(","));
            val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), headerClient);
            config.setSessionStoreFactory(objects -> oauthDistributedSessionStore);
            config.setWebContextFactory(JEEContextFactory.INSTANCE);
            return new SecurityLogicInterceptor(config, clients);
        }
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SecurityLogicInterceptor umaRequestingPartyTokenSecurityInterceptor(
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final JwtBuilder accessTokenJwtBuilder) {
            val authenticator = new UmaRequestingPartyTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
            return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_RPT_AUTH", oauthDistributedSessionStore, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SecurityLogicInterceptor umaAuthorizationApiTokenSecurityInterceptor(
            final CasConfigurationProperties casProperties,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final JwtBuilder accessTokenJwtBuilder) {
            val authenticator = new UmaAuthorizationApiTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
            return getSecurityInterceptor(authenticator, "CAS_UMA_CLIENT_AAT_AUTH",
                oauthDistributedSessionStore, casProperties);
        }

    }

    @Configuration(value = "CasOAuthUmaDiscoveryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaDiscoveryConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaServerDiscoverySettingsFactory")
        public FactoryBean<UmaServerDiscoverySettings> umaServerDiscoverySettingsFactory(
            final CasConfigurationProperties casProperties) {
            return new UmaServerDiscoverySettingsFactory(casProperties);
        }
    }

    @Configuration(value = "CasOAuthUmaWebMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaWebMvcConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "umaWebMvcConfigurer")
        public WebMvcConfigurer umaWebMvcConfigurer(
            @Qualifier("umaAuthorizationApiTokenSecurityInterceptor")
            final ObjectProvider<SecurityLogicInterceptor> umaAuthorizationApiTokenSecurityInterceptor,
            @Qualifier("umaRequestingPartyTokenSecurityInterceptor")
            final ObjectProvider<SecurityLogicInterceptor> umaRequestingPartyTokenSecurityInterceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(
                    @Nonnull
                    final InterceptorRegistry registry) {
                    registry.addInterceptor(new RefreshableHandlerInterceptor(umaRequestingPartyTokenSecurityInterceptor)).order(100)
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_PERMISSION_URL).concat("*"))
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL).concat("*"))
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL).concat("/*"))

                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/*/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_POLICY_URL).concat("*"))
                        
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_POLICY_URL).concat("/*"))
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/*/").concat(OAuth20Constants.UMA_POLICY_URL).concat("/*"))

                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_CLAIMS_COLLECTION_URL).concat("*"));
                    registry.addInterceptor(new RefreshableHandlerInterceptor(umaAuthorizationApiTokenSecurityInterceptor)).order(100)
                        .addPathPatterns(OAuth20Constants.BASE_OAUTH20_URL.concat("/").concat(OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL).concat("*"));
                }
            };
        }

    }

    @Configuration(value = "CasOAuthUmaTicketsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaTicketsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaComponentSerializationPlanConfigurer")
        public ComponentSerializationPlanConfigurer umaComponentSerializationPlanConfigurer() {
            return plan -> {
                plan.registerSerializableClass(DefaultUmaPermissionTicket.class);
                plan.registerSerializableClass(ResourceSet.class);
                plan.registerSerializableClass(ResourceSetPolicy.class);
                plan.registerSerializableClass(ResourceSetPolicyPermission.class);
            };
        }
        
        @ConditionalOnMissingBean(name = "umaPermissionTicketIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator umaPermissionTicketIdGenerator() {
            return new HostNameBasedUniqueTicketIdGenerator();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "umaPermissionTicketExpirationPolicy")
        public ExpirationPolicyBuilder umaPermissionTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new UmaPermissionTicketExpirationPolicyBuilder(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultUmaPermissionTicketFactory")
        public UmaPermissionTicketFactory defaultUmaPermissionTicketFactory(
            @Qualifier("umaPermissionTicketIdGenerator")
            final UniqueTicketIdGenerator umaPermissionTicketIdGenerator,
            @Qualifier("umaPermissionTicketExpirationPolicy")
            final ExpirationPolicyBuilder umaPermissionTicketExpirationPolicy) {
            return new DefaultUmaPermissionTicketFactory(umaPermissionTicketIdGenerator, umaPermissionTicketExpirationPolicy);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oauthUmaTicketSerializationExecutionPlanConfigurer")
        public TicketSerializationExecutionPlanConfigurer oauthUmaTicketSerializationExecutionPlanConfigurer(final ConfigurableApplicationContext applicationContext) {
            return plan -> {
                plan.registerTicketSerializer(new UmaPermissionTicketStringSerializer(applicationContext));
                plan.registerTicketSerializer(UmaPermissionTicket.class.getName(), new UmaPermissionTicketStringSerializer(applicationContext));
                plan.registerTicketSerializer(UmaPermissionTicket.PREFIX, new UmaPermissionTicketStringSerializer(applicationContext));
            };
        }

        private static final class UmaPermissionTicketStringSerializer extends BaseJacksonSerializer<DefaultUmaPermissionTicket> {
            @Serial
            private static final long serialVersionUID = -2198623586274810263L;

            UmaPermissionTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
                super(MINIMAL_PRETTY_PRINTER, applicationContext, DefaultUmaPermissionTicket.class);
            }
        }
        
    }

    @Configuration(value = "CasOAuthUmaTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaTicketFactoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "defaultUmaPermissionTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer defaultUmaPermissionTicketFactoryConfigurer(
            @Qualifier("defaultUmaPermissionTicketFactory")
            final UmaPermissionTicketFactory defaultUmaPermissionTicketFactory) {
            return () -> defaultUmaPermissionTicketFactory;
        }

    }

    @Configuration(value = "CasOAuthUmaControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaControllersConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UmaAuthorizationRequestEndpointController umaAuthorizationRequestEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaAuthorizationRequestEndpointController(umaConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UmaRequestingPartyTokenJwksEndpointController umaRequestingPartyTokenJwksEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaRequestingPartyTokenJwksEndpointController(umaConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UmaRequestingPartyClaimsCollectionEndpointController umaRequestingPartyClaimsCollectionEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaRequestingPartyClaimsCollectionEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaWellKnownEndpointController umaWellKnownEndpointController(
            @Qualifier("umaServerDiscoverySettingsFactory")
            final UmaServerDiscoverySettings discoverySettings) {
            return new UmaWellKnownEndpointController(discoverySettings);
        }

        @Bean
        public UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaPermissionRegistrationEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaCreateResourceSetRegistrationEndpointController umaCreateResourceSetRegistrationEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaCreateResourceSetRegistrationEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaDeleteResourceSetRegistrationEndpointController umaDeleteResourceSetRegistrationEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaDeleteResourceSetRegistrationEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaUpdateResourceSetRegistrationEndpointController umaUpdateResourceSetRegistrationEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaUpdateResourceSetRegistrationEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaFindResourceSetRegistrationEndpointController umaFindResourceSetRegistrationEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaFindResourceSetRegistrationEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaCreatePolicyForResourceSetEndpointController umaCreatePolicyForResourceSetEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaCreatePolicyForResourceSetEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaDeletePolicyForResourceSetEndpointController umaDeletePolicyForResourceSetEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaDeletePolicyForResourceSetEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaUpdatePolicyForResourceSetEndpointController umaUpdatePolicyForResourceSetEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaUpdatePolicyForResourceSetEndpointController(umaConfigurationContext);
        }

        @Bean
        public UmaFindPolicyForResourceSetEndpointController umaFindPolicyForResourceSetEndpointController(
            @Qualifier(UmaConfigurationContext.BEAN_NAME)
            final UmaConfigurationContext umaConfigurationContext) {
            return new UmaFindPolicyForResourceSetEndpointController(umaConfigurationContext);
        }

    }

}
