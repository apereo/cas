package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.DelegatedAuthenticationAuditResourceResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.provision.ChainingDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.replication.CookieSessionReplicationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.discovery.CasServerProfileCustomizer;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.TicketRegistrySessionStore;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.clients.DefaultDelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DefaultDelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.JdbcDelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.clients.RestfulDelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandler;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@Configuration(value = "DelegatedAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class DelegatedAuthenticationEventExecutionPlanConfiguration {

    private static final String AUTHENTICATION_DELEGATION_PREFIX = "AuthnDelegation";

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanSessionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanSessionConfiguration {

        @ConditionalOnMissingBean(name = "delegatedClientDistributedSessionStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SessionStore delegatedClientDistributedSessionStore(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientDistributedSessionCookieGenerator")
            final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry) {
            val replicationProps = casProperties.getAuthn().getPac4j().getCore().getSessionReplication();
            if (replicationProps.isReplicateSessions()) {
                return new TicketRegistrySessionStore(ticketRegistry,
                    ticketFactory, delegatedClientDistributedSessionCookieGenerator);
            }
            val sessionStore = new JEESessionStore();
            sessionStore.setPrefix(AUTHENTICATION_DELEGATION_PREFIX);
            return sessionStore;
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanCoreConfiguration {

        @ConditionalOnMissingBean(name = "delegatedClientDistributedSessionCookieCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CipherExecutor delegatedClientDistributedSessionCookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val replication = casProperties.getAuthn().getPac4j().getCore().getSessionReplication();
            return FunctionUtils.doIf(replication.isReplicateSessions(),
                () -> {
                    val cookie = replication.getCookie();
                    val crypto = cookie.getCrypto();
                    var enabled = crypto.isEnabled();
                    if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                        && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                        LOGGER.warn("Encryption/Signing is not enabled explicitly in the configuration for cookie [{}], yet signing/encryption keys "
                            + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.", cookie.getName());
                        enabled = true;
                    }
                    return enabled
                        ? CipherExecutorUtils.newStringCipherExecutor(crypto, DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor.class)
                        : CipherExecutor.noOp();
                },
                CipherExecutor::noOp).get();
        }

        @ConditionalOnMissingBean(name = "delegatedClientDistributedSessionCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder delegatedClientDistributedSessionCookieGenerator(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier("delegatedClientDistributedSessionCookieCipherExecutor")
            final CipherExecutor delegatedClientDistributedSessionCookieCipherExecutor,
            final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().getCookie();
            if (StringUtils.isBlank(cookie.getName())) {
                cookie.setName("%s%s".formatted(CookieSessionReplicationProperties.DEFAULT_COOKIE_NAME, AUTHENTICATION_DELEGATION_PREFIX));
            }
            return CookieUtils.buildCookieRetrievingGenerator(cookie,
                new DefaultCasCookieValueManager(delegatedClientDistributedSessionCookieCipherExecutor,
                    tenantExtractor, geoLocationService, DefaultCookieSameSitePolicy.INSTANCE, cookie));
        }

        @ConditionalOnMissingBean(name = "clientPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory clientPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanMetadataConfiguration {
        @ConditionalOnMissingBean(name = "clientAuthenticationMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
            return new DelegatedClientAuthenticationMetaDataPopulator();
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanHandlerConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "clientAuthenticationHandler")
        public AuthenticationHandler clientAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("clientPrincipalFactory")
            final PrincipalFactory clientPrincipalFactory,
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier(DelegatedClientUserProfileProvisioner.BEAN_NAME)
            final DelegatedClientUserProfileProvisioner clientUserProfileProvisioner,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val pac4j = casProperties.getAuthn().getPac4j().getCore();
            val handler = new DelegatedClientAuthenticationHandler(pac4j,
                servicesManager, clientPrincipalFactory, identityProviders, clientUserProfileProvisioner,
                delegatedClientDistributedSessionStore, applicationContext);
            handler.setTypedIdUsed(pac4j.isTypedIdUsed());
            handler.setPrincipalAttributeId(pac4j.getPrincipalIdAttribute());
            return handler;
        }

    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanProvisionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanProvisionConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = DelegatedClientUserProfileProvisioner.BEAN_NAME)
        public DelegatedClientUserProfileProvisioner clientUserProfileProvisioner(
            final ObjectProvider<List<Supplier<DelegatedClientUserProfileProvisioner>>> provisioners) {
            val results = provisioners.getIfAvailable(() -> CollectionUtils.wrapList(DelegatedClientUserProfileProvisioner::noOp))
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .map(Supplier::get)
                .collect(Collectors.toList());
            return new ChainingDelegatedClientUserProfileProvisioner(results);
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanClientFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanClientFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedClientNameExtractor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientNameExtractor pac4jDelegatedClientNameExtractor() {
            return DelegatedClientNameExtractor.fromHttpRequest();
        }

        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedClientFactoryCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Cache<String, List<BaseClient>> pac4jDelegatedClientFactoryCache(
            final CasConfigurationProperties casProperties) {
            val core = casProperties.getAuthn().getPac4j().getCore();
            return Caffeine.newBuilder()
                .maximumSize(core.getCacheSize())
                .expireAfterWrite(Beans.newDuration(core.getCacheDuration()))
                .build();
        }

        @Bean
        @ConditionalOnMissingBean(name = "pac4jDelegatedClientFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedIdentityProviderFactory pac4jDelegatedClientFactory(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("pac4jDelegatedClientFactoryCache")
            final Cache<String, List<BaseClient>> clientsCache,
            final CasConfigurationProperties casProperties,
            final ObjectProvider<List<DelegatedClientFactoryCustomizer>> customizerList,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {

            val customizers = Optional.ofNullable(customizerList.getIfAvailable())
                .map(result -> {
                    AnnotationAwareOrderComparator.sortIfNecessary(result);
                    return result;
                })
                .orElseGet(ArrayList::new);

            val pac4j = casProperties.getAuthn().getPac4j();
            if (StringUtils.isNotBlank(pac4j.getRest().getUrl())) {
                return new RestfulDelegatedIdentityProviderFactory(customizers,
                    casSslContext, casProperties, clientsCache, applicationContext);
            }
            return new DefaultDelegatedIdentityProviderFactory(casProperties,
                customizers, casSslContext, clientsCache, applicationContext);
        }
    }

    @Configuration(value = "DelegatedAuthenticationJdbcClientFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(JpaBeans.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "jdbc", enabledByDefault = false)
    static class DelegatedAuthenticationJdbcClientFactoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "pac4jJdbcDelegatedClientFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedIdentityProviderFactory pac4jDelegatedClientFactory(
            @Qualifier("pac4jDelegatedClientJdbcTemplate")
            final JdbcOperations jdbcTemplate,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("pac4jDelegatedClientFactoryCache")
            final Cache<String, List<BaseClient>> clientsCache,
            final CasConfigurationProperties casProperties,
            final ObjectProvider<List<DelegatedClientFactoryCustomizer>> customizerList,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {

            val customizers = Optional.ofNullable(customizerList.getIfAvailable())
                .map(result -> {
                    AnnotationAwareOrderComparator.sortIfNecessary(result);
                    return result;
                })
                .orElseGet(ArrayList::new);

            return new JdbcDelegatedIdentityProviderFactory(jdbcTemplate,
                casProperties, customizers, casSslContext, clientsCache, applicationContext);
        }

        @ConditionalOnMissingBean(name = "pac4jDelegatedClientJdbcTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public JdbcOperations pac4jDelegatedClientJdbcTemplate(
            @Qualifier("pac4jDelegatedClientDataSource")
            final DataSource pac4jDelegatedClientDataSource) {
            return new JdbcTemplate(pac4jDelegatedClientDataSource);
        }

        @ConditionalOnMissingBean(name = "pac4jDelegatedClientDataSource")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource pac4jDelegatedClientDataSource(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getPac4j().getJdbc());
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanClientConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "delegatedAuthenticationCredentialExtractor")
        public DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor(
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return new DefaultDelegatedAuthenticationCredentialExtractor(delegatedClientDistributedSessionStore);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = DelegatedIdentityProviders.BEAN_NAME)
        public DelegatedIdentityProviders delegatedIdentityProviders(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier("pac4jDelegatedClientFactory")
            final DelegatedIdentityProviderFactory pac4jDelegatedIdentityProviderFactory) {
            return new DefaultDelegatedIdentityProviders(pac4jDelegatedIdentityProviderFactory, tenantExtractor);
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanAuditConfiguration {
        @ConditionalOnMissingBean(name = "delegatedAuthenticationAuditResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver delegatedAuthenticationAuditResourceResolver() {
            return new DelegatedAuthenticationAuditResourceResolver();
        }
    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanAuditPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanAuditPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "delegatedAuthenticationAuditTrailRecordResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer delegatedAuthenticationAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationAuditResourceResolver")
            final AuditResourceResolver delegatedAuthenticationAuditResourceResolver,
            @Qualifier("authenticationActionResolver")
            final AuditActionResolver authenticationActionResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.DELEGATED_CLIENT_ACTION_RESOLVER, authenticationActionResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.DELEGATED_CLIENT_RESOURCE_RESOLVER, delegatedAuthenticationAuditResourceResolver);
            };
        }

    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanLogoutConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationLogoutExecutionPlanConfigurer")
        public LogoutExecutionPlanConfigurer delegatedAuthenticationLogoutExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return plan -> {
                val replicate = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().isReplicateSessions();
                if (replicate) {
                    plan.registerLogoutPostProcessor(ticketGrantingTicket -> {
                        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
                        if (request != null && response != null) {
                            delegatedClientDistributedSessionStore.destroySession(new JEEContext(request, response));
                        }
                    });
                }
            };
        }

    }

    @Configuration(value = "DelegatedAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DelegatedAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "pac4jAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            @Qualifier("clientAuthenticationHandler")
            final AuthenticationHandler clientAuthenticationHandler,
            @Qualifier("clientAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> {
                plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler, defaultPrincipalResolver);
                plan.registerAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
            };
        }
    }


    @Configuration(value = "DelegatedAuthenticationDiscoveryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(CasServerProfileCustomizer.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery)
    static class DelegatedAuthenticationDiscoveryConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationCasServerProfileCustomizer")
        @Bean
        public CasServerProfileCustomizer delegatedAuthenticationCasServerProfileCustomizer(
            @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
            final DelegatedIdentityProviders identityProviders,
            final CasConfigurationProperties casProperties) {
            return (profile, request, response) -> {
                val context = new JEEContext(request, response);
                val clients = identityProviders.findAllClients(context)
                    .stream()
                    .map(Client::getName)
                    .collect(Collectors.toSet());
                profile.getDetails().put("delegatedClientTypesSupported", clients);
            };
        }
    }
}
