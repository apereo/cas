package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.CasJavaClientProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
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
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ProxyGrantingTicketIdGenerator;
import org.apereo.cas.util.ProxyTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.cipher.ProtocolTicketCipherExecutor;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(value = {CasCoreUtilConfiguration.class, CasCoreTicketIdGeneratorsConfiguration.class})
@Slf4j
public class CasCoreTicketsConfiguration implements TransactionManagementConfigurer {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private ObjectProvider<Map<String, UniqueTicketIdGenerator>> uniqueIdGeneratorsMap;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

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
                    httpsConnection.setSSLSocketFactory(sslContext.getIfAvailable().getSocketFactory());
                    httpsConnection.setHostnameVerifier(hostnameVerifier.getIfAvailable());
                }
                return (HttpURLConnection) conn;
            }
        };
        validator.setURLConnectionFactory(factory);
        return validator;
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

    @ConditionalOnMissingBean(name = "defaultProxyGrantingTicketFactory")
    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        return new DefaultProxyGrantingTicketFactory(
            proxyGrantingTicketUniqueIdGenerator(),
            grantingTicketExpirationPolicy(),
            protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultProxyTicketFactory")
    @RefreshScope
    @Bean
    @Lazy
    public ProxyTicketFactory defaultProxyTicketFactory() {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultProxyTicketFactory(proxyTicketExpirationPolicy(),
            uniqueIdGeneratorsMap.getIfAvailable(),
            protocolTicketCipherExecutor(),
            onlyTrackMostRecentSession,
            servicesManager.getIfAvailable());
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
    public TransientSessionTicketFactory defaultTransientSessionTicketFactory() {
        return new DefaultTransientSessionTicketFactory(transientSessionTicketExpirationPolicy());
    }


    @ConditionalOnMissingBean(name = "transientSessionTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy transientSessionTicketExpirationPolicy() {
        return new HardTimeoutExpirationPolicy(casProperties.getTicket().getTst().getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "defaultServiceTicketFactory")
    @Bean
    @Lazy
    public ServiceTicketFactory defaultServiceTicketFactory() {
        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultServiceTicketFactory(serviceTicketExpirationPolicy(),
            uniqueIdGeneratorsMap.getIfAvailable(),
            onlyTrackMostRecentSession,
            protocolTicketCipherExecutor(),
            servicesManager.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "defaultTicketGrantingTicketFactory")
    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DefaultTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator(),
            grantingTicketExpirationPolicy(),
            protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultTicketFactory")
    @Bean
    public TicketFactory defaultTicketFactory() {
        val factory = new DefaultTicketFactory();
        factory.addTicketFactory(TransientSessionTicket.class, defaultTransientSessionTicketFactory())
            .addTicketFactory(ProxyGrantingTicket.class, defaultProxyGrantingTicketFactory())
            .addTicketFactory(TicketGrantingTicket.class, defaultTicketGrantingTicketFactory())
            .addTicketFactory(ServiceTicket.class, defaultServiceTicketFactory())
            .addTicketFactory(ProxyTicket.class, defaultProxyTicketFactory());
        return factory;
    }

    @ConditionalOnMissingBean(name = "proxy10Handler")
    @Bean
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @ConditionalOnMissingBean(name = "proxy20Handler")
    @Bean
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public ProxyHandler proxy20Handler() {
        return new Cas20ProxyHandler(httpClient.getIfAvailable(), proxy20TicketUniqueIdGenerator());
    }

    @ConditionalOnMissingBean(name = "ticketRegistry")
    @Bean
    public TicketRegistry ticketRegistry() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and managing tickets. "
            + "Tickets that are issued during runtime will be LOST when the web server is restarted. This MAY impact SSO functionality.");
        val mem = casProperties.getTicket().getRegistry().getInMemory();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(mem.getCrypto(), "inMemory");

        if (mem.isCache()) {
            val logoutManager = applicationContext.getBean("logoutManager", LogoutManager.class);
            return new CachingTicketRegistry(cipher, logoutManager);
        }
        return new DefaultTicketRegistry(mem.getInitialCapacity(), mem.getLoadFactor(), mem.getConcurrency(), cipher);
    }

    @ConditionalOnMissingBean(name = "defaultTicketRegistrySupport")
    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        return new DefaultTicketRegistrySupport(ticketRegistry.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "grantingTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy grantingTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        if (tgt.getRememberMe().isEnabled()) {
            val p = rememberMeExpirationPolicy();
            LOGGER.debug("Final effective time-to-live of remember-me expiration policy is [{}] seconds", p.getTimeToLive());
            return p;
        }
        val p = ticketGrantingTicketExpirationPolicy();
        LOGGER.debug("Final effective time-to-live of ticket-granting ticket expiration policy is [{}] seconds", p.getTimeToLive());
        return p;
    }

    @Bean
    public ExpirationPolicy rememberMeExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        LOGGER.debug("Remember me expiration policy is being configured based on hard timeout of [{}] seconds",
            tgt.getRememberMe().getTimeToKillInSeconds());
        val rememberMePolicy = new HardTimeoutExpirationPolicy(tgt.getRememberMe().getTimeToKillInSeconds());
        val p = new RememberMeDelegatingExpirationPolicy();
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, rememberMePolicy);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, ticketGrantingTicketExpirationPolicy());
        return p;
    }

    @ConditionalOnMissingBean(name = "serviceTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy serviceTicketExpirationPolicy() {
        val st = casProperties.getTicket().getSt();
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
            st.getNumberOfUses(),
            st.getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "proxyTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy proxyTicketExpirationPolicy() {
        val pt = casProperties.getTicket().getPt();
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(
            pt.getNumberOfUses(),
            pt.getTimeToKillInSeconds());
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
            return new ProtocolTicketCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg(),
                crypto.getSigning().getKeySize(),
                crypto.getEncryption().getKeySize());
        }
        LOGGER.trace("Protocol tickets generated by CAS are not signed/encrypted.");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy ticketGrantingTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        if (tgt.getMaxTimeToLiveInSeconds() <= 0 && tgt.getTimeToKillInSeconds() <= 0) {
            LOGGER.warn("Ticket-granting ticket expiration policy is set to NEVER expire tickets.");
            return new NeverExpiresExpirationPolicy();
        }

        if (tgt.getTimeout().getMaxTimeToLiveInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a timeout of [{}] seconds",
                tgt.getTimeout().getMaxTimeToLiveInSeconds());
            return new TimeoutExpirationPolicy(tgt.getTimeout().getMaxTimeToLiveInSeconds());
        }

        if (tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds() > 0
            && tgt.getThrottledTimeout().getTimeToKillInSeconds() > 0) {
            val p = new ThrottledUseAndTimeoutExpirationPolicy();
            p.setTimeToKillInSeconds(tgt.getThrottledTimeout().getTimeToKillInSeconds());
            p.setTimeInBetweenUsesInSeconds(tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds());
            LOGGER.debug("Ticket-granting ticket expiration policy is based on throttled timeouts");
            return p;
        }

        if (tgt.getHardTimeout().getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a hard timeout of [{}] seconds",
                tgt.getHardTimeout().getTimeToKillInSeconds());
            return new HardTimeoutExpirationPolicy(tgt.getHardTimeout().getTimeToKillInSeconds());
        }

        if (tgt.getMaxTimeToLiveInSeconds() > 0 && tgt.getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on hard/idle timeouts of [{}]/[{}] seconds",
                tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
            return new TicketGrantingTicketExpirationPolicy(tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
        }
        LOGGER.warn("Ticket-granting ticket expiration policy is set to ALWAYS expire tickets.");
        return new AlwaysExpiresExpirationPolicy();
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
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.trace("Configuring ticket metadata registration plan [{}]", name);
            c.configureTicketCatalog(plan);
        });
        return plan;
    }
}
