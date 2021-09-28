package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.authentication.policy.UniquePrincipalAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.builder.ProxyGrantingTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.ProxyTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.ServiceTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.factory.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.ticket.registry.CachingTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.NoOpLockingStrategy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ProxyGrantingTicketIdGenerator;
import org.apereo.cas.util.ProxyTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.ProtocolTicketCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreTicketsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@Slf4j
public class CasCoreTicketsConfiguration {
    @ConditionalOnMissingBean(name = "defaultProxyGrantingTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory(
        @Qualifier("proxyGrantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder proxyGrantingTicketExpirationPolicy,
        @Qualifier("proxyGrantingTicketUniqueIdGenerator")
        final UniqueTicketIdGenerator proxyGrantingTicketUniqueIdGenerator,
        @Qualifier("protocolTicketCipherExecutor")
        final CipherExecutor protocolTicketCipherExecutor,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new DefaultProxyGrantingTicketFactory(
            proxyGrantingTicketUniqueIdGenerator,
            proxyGrantingTicketExpirationPolicy,
            protocolTicketCipherExecutor,
            servicesManager);
    }

    @ConditionalOnMissingBean(name = "defaultProxyGrantingTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultProxyGrantingTicketFactoryConfigurer(
        @Qualifier("defaultProxyGrantingTicketFactory")
        final ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory) {
        return () -> defaultProxyGrantingTicketFactory;
    }

    @ConditionalOnMissingBean(name = "defaultProxyTicketFactory")
    @RefreshScope
    @Bean
    @Autowired
    public ProxyTicketFactory defaultProxyTicketFactory(
        @Qualifier("protocolTicketCipherExecutor")
        final CipherExecutor protocolTicketCipherExecutor,
        @Qualifier("proxyTicketExpirationPolicy")
        final ExpirationPolicyBuilder proxyTicketExpirationPolicy,
        final CasConfigurationProperties casProperties,
        @Qualifier("uniqueIdGeneratorsMap")
        final Map<String, UniqueTicketIdGenerator> uniqueIdGeneratorsMap,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().getCore().isOnlyTrackMostRecentSession();
        return new DefaultProxyTicketFactory(proxyTicketExpirationPolicy, uniqueIdGeneratorsMap,
            protocolTicketCipherExecutor, onlyTrackMostRecentSession, servicesManager);
    }

    @ConditionalOnMissingBean(name = "defaultProxyTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultProxyTicketFactoryConfigurer(
        @Qualifier("defaultProxyTicketFactory")
        final ProxyTicketFactory defaultProxyTicketFactory) {
        return () -> defaultProxyTicketFactory;
    }

    @ConditionalOnMissingBean(name = "proxyGrantingTicketUniqueIdGenerator")
    @Bean
    @RefreshScope
    @Autowired
    public UniqueTicketIdGenerator proxyGrantingTicketUniqueIdGenerator(
        final CasConfigurationProperties casProperties) {
        return new ProxyGrantingTicketIdGenerator(
            casProperties.getTicket().getTgt().getCore().getMaxLength(),
            casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketUniqueIdGenerator")
    @Bean
    @RefreshScope
    @Autowired
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator(
        final CasConfigurationProperties casProperties) {
        return new TicketGrantingTicketIdGenerator(
            casProperties.getTicket().getTgt().getCore().getMaxLength(),
            casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "proxy20TicketUniqueIdGenerator")
    @Bean
    @Autowired
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator(
        final CasConfigurationProperties casProperties) {
        return new ProxyTicketIdGenerator(
            casProperties.getTicket().getPgt().getMaxLength(),
            casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "defaultTransientSessionTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public TransientSessionTicketFactory defaultTransientSessionTicketFactory(
        @Qualifier("transientSessionTicketExpirationPolicy")
        final ExpirationPolicyBuilder transientSessionTicketExpirationPolicy) {
        return new DefaultTransientSessionTicketFactory(transientSessionTicketExpirationPolicy);
    }

    @ConditionalOnMissingBean(name = "defaultTransientSessionTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultTransientSessionTicketFactoryConfigurer(
        @Qualifier("defaultTransientSessionTicketFactory")
        final TransientSessionTicketFactory defaultTransientSessionTicketFactory) {
        return () -> defaultTransientSessionTicketFactory;
    }

    @ConditionalOnMissingBean(name = "transientSessionTicketExpirationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder transientSessionTicketExpirationPolicy(
        final CasConfigurationProperties casProperties) {
        return new TransientSessionTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "defaultServiceTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public ServiceTicketFactory defaultServiceTicketFactory(
        @Qualifier("protocolTicketCipherExecutor")
        final CipherExecutor protocolTicketCipherExecutor,
        @Qualifier("serviceTicketExpirationPolicy")
        final ExpirationPolicyBuilder serviceTicketExpirationPolicy,
        final CasConfigurationProperties casProperties,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("uniqueIdGeneratorsMap")
        final Map<String, UniqueTicketIdGenerator> uniqueIdGeneratorsMap) {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().getCore().isOnlyTrackMostRecentSession();
        return new DefaultServiceTicketFactory(serviceTicketExpirationPolicy,
            uniqueIdGeneratorsMap, onlyTrackMostRecentSession,
            protocolTicketCipherExecutor, servicesManager);
    }

    @ConditionalOnMissingBean(name = "defaultServiceTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultServiceTicketFactoryConfigurer(
        @Qualifier("defaultServiceTicketFactory")
        final ServiceTicketFactory defaultServiceTicketFactory) {
        return () -> defaultServiceTicketFactory;
    }

    @ConditionalOnMissingBean(name = "defaultTicketGrantingTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory(
        @Qualifier("grantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
        @Qualifier("protocolTicketCipherExecutor")
        final CipherExecutor protocolTicketCipherExecutor,
        @Qualifier("ticketGrantingTicketUniqueIdGenerator")
        final UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new DefaultTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator,
            grantingTicketExpirationPolicy, protocolTicketCipherExecutor, servicesManager);
    }

    @ConditionalOnMissingBean(name = "defaultTicketGrantingTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer defaultTicketGrantingTicketFactoryConfigurer(
        @Qualifier("defaultTicketGrantingTicketFactory")
        final TicketGrantingTicketFactory defaultTicketGrantingTicketFactory) {
        return () -> defaultTicketGrantingTicketFactory;
    }

    @ConditionalOnMissingBean(name = "defaultTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactory defaultTicketFactory(final List<TicketFactoryExecutionPlanConfigurer> configurers) {
        val parentFactory = new DefaultTicketFactory();
        configurers.forEach(configurer -> {
            val factory = configurer.configureTicketFactory();
            LOGGER.trace("Registering ticket factory via [{}]", factory.getName());
            parentFactory.addTicketFactory(factory.getTicketType(), factory);
        });
        return parentFactory;
    }

    @ConditionalOnMissingBean(name = "proxy10Handler")
    @Bean
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @ConditionalOnMissingBean(name = "proxy20Handler")
    @Bean
    @Autowired
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy20Handler(
        @Qualifier("proxy20TicketUniqueIdGenerator")
        final UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator,
        @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
        final HttpClient httpClient) {
        return new Cas20ProxyHandler(httpClient, proxy20TicketUniqueIdGenerator);
    }

    @ConditionalOnMissingBean(name = TicketRegistry.BEAN_NAME)
    @Bean
    @RefreshScope
    @Autowired
    public TicketRegistry ticketRegistry(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and managing tickets. "
            + "Tickets that are issued during runtime will be LOST when the web server is restarted. This MAY impact SSO functionality.");
        val mem = casProperties.getTicket().getRegistry().getInMemory();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(mem.getCrypto(), "in-memory");

        if (mem.isCache()) {
            val logoutManager = applicationContext.getBean(LogoutManager.DEFAULT_BEAN_NAME, LogoutManager.class);
            return new CachingTicketRegistry(cipher, logoutManager);
        }
        val storageMap = new ConcurrentHashMap<String, Ticket>(mem.getInitialCapacity(), mem.getLoadFactor(), mem.getConcurrency());
        return new DefaultTicketRegistry(storageMap, cipher);
    }

    @ConditionalOnMissingBean(name = "defaultTicketRegistrySupport")
    @Bean
    @Autowired
    public TicketRegistrySupport defaultTicketRegistrySupport(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return new DefaultTicketRegistrySupport(ticketRegistry);
    }

    @ConditionalOnMissingBean(name = "grantingTicketExpirationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder grantingTicketExpirationPolicy(
        final CasConfigurationProperties casProperties) {
        return new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "proxyGrantingTicketExpirationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder proxyGrantingTicketExpirationPolicy(
        @Qualifier("grantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
        final CasConfigurationProperties casProperties) {
        return new ProxyGrantingTicketExpirationPolicyBuilder(grantingTicketExpirationPolicy, casProperties);
    }

    @ConditionalOnMissingBean(name = "serviceTicketExpirationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder serviceTicketExpirationPolicy(
        final CasConfigurationProperties casProperties) {
        return new ServiceTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "proxyTicketExpirationPolicy")
    @Bean
    @RefreshScope
    @Autowired
    public ExpirationPolicyBuilder proxyTicketExpirationPolicy(
        final CasConfigurationProperties casProperties) {
        return new ProxyTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "lockingStrategy")
    @Bean
    public LockingStrategy lockingStrategy() {
        return new NoOpLockingStrategy();
    }

    @ConditionalOnMissingBean(name = "ticketTransactionManager")
    @Bean
    public PlatformTransactionManager ticketTransactionManager() {
        return new PseudoPlatformTransactionManager();
    }

    @RefreshScope
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "protocolTicketCipherExecutor")
    public CipherExecutor protocolTicketCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val crypto = casProperties.getTicket().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, ProtocolTicketCipherExecutor.class);
        }
        LOGGER.trace("Protocol tickets generated by CAS are not signed/encrypted.");
        return CipherExecutor.noOp();
    }

    @Bean
    @Autowired
    public TransactionManagementConfigurer transactionManagementConfigurer(
        @Qualifier("ticketTransactionManager")
        final PlatformTransactionManager ticketTransactionManager) {
        return () -> ticketTransactionManager;
    }

    @ConditionalOnMissingBean(name = "ticketCatalog")
    @Autowired
    @Bean
    public TicketCatalog ticketCatalog(
        final CasConfigurationProperties casProperties,
        final List<TicketCatalogConfigurer> configurers) {
        val plan = new DefaultTicketCatalog();
        configurers.forEach(c -> {
            LOGGER.trace("Configuring ticket metadata registration plan [{}]", c.getName());
            c.configureTicketCatalog(plan, casProperties);
        });
        return plan;
    }

    @ConditionalOnMissingBean(name = "ticketAuthenticationPolicyExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer ticketAuthenticationPolicyExecutionPlanConfigurer(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        final CasConfigurationProperties casProperties) {
        return plan -> {
            val policyProps = casProperties.getAuthn().getPolicy();
            if (policyProps.getUniquePrincipal().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", UniquePrincipalAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new UniquePrincipalAuthenticationPolicy(ticketRegistry));
            }
        };
    }
}
