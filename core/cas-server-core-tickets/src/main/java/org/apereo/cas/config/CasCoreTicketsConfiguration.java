package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.authentication.policy.UniquePrincipalAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.CasJavaClientProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
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
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.ProxyTicket;
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
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(value = {CasCoreUtilConfiguration.class, CasCoreTicketIdGeneratorsConfiguration.class})
@Slf4j
public class CasCoreTicketsConfiguration implements TransactionManagementConfigurer {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private ObjectProvider<Map<String, UniqueTicketIdGenerator>> uniqueIdGeneratorsMap;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("hostnameVerifier")
    private ObjectProvider<HostnameVerifier> hostnameVerifier;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @ConditionalOnMissingBean(name = "casClientTicketValidator")
    @Bean
    public AbstractUrlBasedTicketValidator casClientTicketValidator() {
        val prefix = StringUtils.defaultString(casProperties.getClient().getPrefix(), casProperties.getServer().getPrefix());
        val validator = buildCasClientTicketValidator(prefix);

        val factory = new HttpURLConnectionFactory() {
            private static final long serialVersionUID = 3692658214483917813L;

            @Override
            public HttpURLConnection buildHttpURLConnection(final URLConnection conn) {
                if (conn instanceof HttpsURLConnection) {
                    val httpsConnection = (HttpsURLConnection) conn;
                    httpsConnection.setSSLSocketFactory(sslContext.getObject().getSocketFactory());
                    httpsConnection.setHostnameVerifier(hostnameVerifier.getObject());
                }
                return (HttpURLConnection) conn;
            }
        };
        validator.setURLConnectionFactory(factory);
        return validator;
    }

    @ConditionalOnMissingBean(name = "defaultProxyGrantingTicketFactory")
    @Bean
    @RefreshScope
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        return new DefaultProxyGrantingTicketFactory(
            proxyGrantingTicketUniqueIdGenerator(),
            proxyGrantingTicketExpirationPolicy(),
            protocolTicketCipherExecutor(),
            servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "defaultProxyTicketFactory")
    @RefreshScope
    @Bean
    @Lazy
    public ProxyTicketFactory defaultProxyTicketFactory() {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultProxyTicketFactory(proxyTicketExpirationPolicy(),
            uniqueIdGeneratorsMap.getObject(),
            protocolTicketCipherExecutor(),
            onlyTrackMostRecentSession,
            servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "proxyGrantingTicketUniqueIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator proxyGrantingTicketUniqueIdGenerator() {
        return new ProxyGrantingTicketIdGenerator(
            casProperties.getTicket().getTgt().getMaxLength(),
            casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketUniqueIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator() {
        return new TicketGrantingTicketIdGenerator(
            casProperties.getTicket().getTgt().getMaxLength(),
            casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "proxy20TicketUniqueIdGenerator")
    @Bean
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator() {
        return new ProxyTicketIdGenerator(
            casProperties.getTicket().getPgt().getMaxLength(),
            casProperties.getHost().getName());
    }


    @ConditionalOnMissingBean(name = "defaultTransientSessionTicketFactory")
    @Bean
    @RefreshScope
    public TransientSessionTicketFactory defaultTransientSessionTicketFactory() {
        return new DefaultTransientSessionTicketFactory(transientSessionTicketExpirationPolicy());
    }


    @ConditionalOnMissingBean(name = "transientSessionTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder transientSessionTicketExpirationPolicy() {
        return new TransientSessionTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "defaultServiceTicketFactory")
    @Bean
    @Lazy
    public ServiceTicketFactory defaultServiceTicketFactory() {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultServiceTicketFactory(serviceTicketExpirationPolicy(),
            uniqueIdGeneratorsMap.getObject(),
            onlyTrackMostRecentSession,
            protocolTicketCipherExecutor(),
            servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "defaultTicketGrantingTicketFactory")
    @Bean
    @RefreshScope
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DefaultTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator(),
            grantingTicketExpirationPolicy(),
            protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultTicketFactory")
    @Bean
    @RefreshScope
    public TicketFactory defaultTicketFactory() {
        val factory = new DefaultTicketFactory();
        factory
            .addTicketFactory(TransientSessionTicket.class, defaultTransientSessionTicketFactory())
            .addTicketFactory(ProxyGrantingTicket.class, defaultProxyGrantingTicketFactory())
            .addTicketFactory(TicketGrantingTicket.class, defaultTicketGrantingTicketFactory())
            .addTicketFactory(ServiceTicket.class, defaultServiceTicketFactory())
            .addTicketFactory(ProxyTicket.class, defaultProxyTicketFactory());
        return factory;
    }

    @ConditionalOnMissingBean(name = "proxy10Handler")
    @Bean
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @ConditionalOnMissingBean(name = "proxy20Handler")
    @Bean
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy20Handler() {
        return new Cas20ProxyHandler(httpClient.getObject(), proxy20TicketUniqueIdGenerator());
    }

    @ConditionalOnMissingBean(name = "ticketRegistry")
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and managing tickets. "
            + "Tickets that are issued during runtime will be LOST when the web server is restarted. This MAY impact SSO functionality.");
        val mem = casProperties.getTicket().getRegistry().getInMemory();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(mem.getCrypto(), "inMemory");

        if (mem.isCache()) {
            val logoutManager = applicationContext.getBean("logoutManager", LogoutManager.class);
            return new CachingTicketRegistry(cipher, logoutManager);
        }
        val storageMap = new ConcurrentHashMap<String, Ticket>(mem.getInitialCapacity(), mem.getLoadFactor(), mem.getConcurrency());
        return new DefaultTicketRegistry(storageMap, cipher);
    }

    @ConditionalOnMissingBean(name = "defaultTicketRegistrySupport")
    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        return new DefaultTicketRegistrySupport(ticketRegistry());
    }

    @ConditionalOnMissingBean(name = "grantingTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder grantingTicketExpirationPolicy() {
        return new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "proxyGrantingTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder proxyGrantingTicketExpirationPolicy() {
        return new ProxyGrantingTicketExpirationPolicyBuilder(grantingTicketExpirationPolicy(), casProperties);
    }

    @ConditionalOnMissingBean(name = "serviceTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder serviceTicketExpirationPolicy() {
        return new ServiceTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "proxyTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder proxyTicketExpirationPolicy() {
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
    @ConditionalOnMissingBean(name = "protocolTicketCipherExecutor")
    public CipherExecutor protocolTicketCipherExecutor() {
        val crypto = casProperties.getTicket().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, ProtocolTicketCipherExecutor.class);
        }
        LOGGER.trace("Protocol tickets generated by CAS are not signed/encrypted.");
        return CipherExecutor.noOp();
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return ticketTransactionManager();
    }

    @ConditionalOnMissingBean(name = "ticketCatalog")
    @Autowired
    @Bean
    public TicketCatalog ticketCatalog(final List<TicketCatalogConfigurer> configurers) {
        val plan = new DefaultTicketCatalog();
        configurers.forEach(c -> {
            LOGGER.trace("Configuring ticket metadata registration plan [{}]", c.getName());
            c.configureTicketCatalog(plan);
        });
        return plan;
    }

    @ConditionalOnMissingBean(name = "ticketAuthenticationPolicyExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer ticketAuthenticationPolicyExecutionPlanConfigurer() {
        return plan -> {
            val policyProps = casProperties.getAuthn().getPolicy();
            if (policyProps.getUniquePrincipal().isEnabled()) {
                LOGGER.trace("Activating authentication policy [{}]", UniquePrincipalAuthenticationPolicy.class.getSimpleName());
                plan.registerAuthenticationPolicy(new UniquePrincipalAuthenticationPolicy(ticketRegistry()));
            }
        };
    }

    private AbstractUrlBasedTicketValidator buildCasClientTicketValidator(final String prefix) {
        val validatorType = casProperties.getClient().getValidatorType();
        if (validatorType == CasJavaClientProperties.ClientTicketValidatorTypes.CAS10) {
            return new Cas10TicketValidator(prefix);
        }
        if (validatorType == CasJavaClientProperties.ClientTicketValidatorTypes.CAS20) {
            return new Cas20ServiceTicketValidator(prefix);
        }
        return new Cas30ServiceTicketValidator(prefix);
    }
}
