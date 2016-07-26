package org.apereo.cas.configuration.support;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultTicketCipherExecutor;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.util.NoOpCipherExecutor;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.provider.Provider;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * <code>@Bean</code> creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class Beans {

    private static final Logger LOGGER = LoggerFactory.getLogger(Beans.class);

    protected Beans() {
    }

    /**
     * New hickari data source.
     *
     * @param jpaProperties the jpa properties
     * @return the hikari data source
     */
    public static HikariDataSource newHickariDataSource(final AbstractJpaProperties jpaProperties) {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(jpaProperties.getDriverClass());
            bean.setJdbcUrl(jpaProperties.getUrl());
            bean.setUsername(jpaProperties.getUser());
            bean.setPassword(jpaProperties.getPassword());

            bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
            bean.setMinimumIdle(jpaProperties.getPool().getMaxIdleTime());
            bean.setIdleTimeout(jpaProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
            bean.setInitializationFailFast(jpaProperties.isFailFast());
            bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
            bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
            bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
            bean.setAutoCommit(jpaProperties.isAutocommit());
            bean.setLoginTimeout(jpaProperties.getPool().getMaxWait());
            bean.setValidationTimeout(jpaProperties.getPool().getMaxWait());
            return bean;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * New hibernate jpa vendor adapter.
     *
     * @param databaseProperties the database properties
     * @return the hibernate jpa vendor adapter
     */
    public static HibernateJpaVendorAdapter newHibernateJpaVendorAdapter(final DatabaseProperties databaseProperties) {
        final HibernateJpaVendorAdapter bean = new HibernateJpaVendorAdapter();
        bean.setGenerateDdl(databaseProperties.isGenDdl());
        bean.setShowSql(databaseProperties.isShowSql());
        return bean;
    }

    /**
     * New thread pool executor factory bean thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(config.getMinSize());
        bean.setMaxPoolSize(config.getMaxSize());
        bean.setKeepAliveSeconds(config.getMaxWait());
        return bean;
    }

    /**
     * New entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                     final AbstractJpaProperties jpaProperties) {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotEmpty(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan());
        bean.setDataSource(config.getDataSource());

        final Properties properties = new Properties();
        properties.put("hibernate.dialect", jpaProperties.getDialect());
        properties.put("hibernate.hbm2ddl.auto", jpaProperties.getDdlAuto());
        properties.put("hibernate.jdbc.batch_size", jpaProperties.getBatchSize());
        bean.setJpaProperties(properties);
        return bean;
    }

    /**
     * New attribute repository person attribute dao.
     *
     * @param attributes the attributes
     * @return the person attribute dao
     */
    public static IPersonAttributeDao newAttributeRepository(final Map<String, String> attributes) {
        try {
            final NamedStubPersonAttributeDao dao = new NamedStubPersonAttributeDao();
            final Map pdirMap = new HashMap<>();
            attributes.entrySet().forEach(entry -> {
                pdirMap.put(entry.getKey(), Lists.newArrayList(entry.getValue()));
            });
            dao.setBackingMap(pdirMap);
            return dao;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * New password encoder password encoder.
     *
     * @param properties the properties
     * @return the password encoder
     */
    public static PasswordEncoder newPasswordEncoder(final PasswordEncoderProperties properties) {
        switch (properties.getType()) {
            case NONE:
                return NoOpPasswordEncoder.getInstance();
            case DEFAULT:
                return new DefaultPasswordEncoder(properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
            case STANDARD:
                return new StandardPasswordEncoder(properties.getSecret());
            default:
                return new BCryptPasswordEncoder(properties.getStrength(), new SecureRandom(properties.getSecret().getBytes()));
        }
    }


    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {

        PrincipalNameTransformer res = null;
        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            final PrefixSuffixPrincipalNameTransformer t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
        } else {
            res = formUserId -> formUserId;
        }

        switch (p.getCaseConversion()) {
            case UPPERCASE:
                final ConvertCasePrincipalNameTransformer t = new ConvertCasePrincipalNameTransformer(res);
                t.setToUpperCase(true);
                return t;

            case LOWERCASE:
                final ConvertCasePrincipalNameTransformer t1 = new ConvertCasePrincipalNameTransformer(res);
                t1.setToUpperCase(false);
                return t1;
            default:
                //nothing
        }
        return res;
    }

    /**
     * New pooled connection factory pooled connection factory.
     *
     * @param l the l
     * @return the pooled connection factory
     */
    public static PooledConnectionFactory newPooledConnectionFactory(final AbstractLdapProperties l) {
        final PoolConfig pc = new PoolConfig();
        pc.setMinPoolSize(l.getMinPoolSize());
        pc.setMaxPoolSize(l.getMaxPoolSize());
        pc.setValidateOnCheckOut(l.isValidateOnCheckout());
        pc.setValidatePeriodically(l.isValidatePeriodically());
        pc.setValidatePeriod(newDuration(l.getValidatePeriod()));

        final ConnectionConfig cc = new ConnectionConfig();
        cc.setLdapUrl(l.getLdapUrl());
        cc.setUseSSL(l.isUseSsl());
        cc.setUseStartTLS(l.isUseStartTls());
        cc.setConnectTimeout(newDuration(l.getConnectTimeout()));

        if (l.getTrustCertificates() != null) {
            final X509CredentialConfig cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(l.getTrustCertificates());
            cc.setSslConfig(new SslConfig());
        } else if (l.getKeystore() != null) {
            final KeyStoreCredentialConfig cfg = new KeyStoreCredentialConfig();
            cfg.setKeyStore(l.getKeystore());
            cfg.setKeyStorePassword(l.getKeystorePassword());
            cfg.setKeyStoreType(l.getKeystoreType());
            cc.setSslConfig(new SslConfig(cfg));
        } else {
            cc.setSslConfig(new SslConfig());
        }

        if (StringUtils.equals(l.getBindCredential(), "*") && StringUtils.equals(l.getBindDn(), "*")) {
            cc.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        } else if (StringUtils.isNotBlank(l.getBindDn()) && StringUtils.isNotBlank(l.getBindCredential())) {
            cc.setConnectionInitializer(new BindConnectionInitializer(l.getBindDn(),
                    new Credential(l.getBindCredential())));
        }

        final DefaultConnectionFactory bindCf = new DefaultConnectionFactory(cc);

        if (l.getProviderClass() != null) {
            try {
                final Class clazz = ClassUtils.getClass(l.getProviderClass());
                bindCf.setProvider(Provider.class.cast(clazz.newInstance()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        final BlockingConnectionPool cp = new BlockingConnectionPool(pc, bindCf);

        cp.setBlockWaitTime(newDuration(l.getBlockWaitTime()));
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        strategy.setIdleTime(newDuration(l.getIdleTime()));
        strategy.setPrunePeriod(newDuration(l.getPrunePeriod()));

        cp.setPruneStrategy(strategy);
        cp.setValidator(new SearchValidator());
        cp.setFailFastInitialize(l.isFailFast());
        cp.initialize();
        return new PooledConnectionFactory(cp);
    }

    /**
     * New duration.
     *
     * @param length the length in seconds.
     * @return the duration
     */
    public static Duration newDuration(final long length) {
        return Duration.ofSeconds(length);
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry the registry
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final CryptographyProperties registry) {
        if (StringUtils.isNotBlank(registry.getEncryption().getKey())
                && StringUtils.isNotBlank(registry.getEncryption().getKey())) {
            return new DefaultTicketCipherExecutor(
                    registry.getEncryption().getKey(),
                    registry.getSigning().getKey(),
                    registry.getAlg(),
                    registry.getSigning().getKeySize(),
                    registry.getEncryption().getKeySize());
        }
        LOGGER.info("Ticket registry encryption/signing is turned off. This may NOT be safe in a "
                + "clustered production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "ticket registry tickets.");
        return new NoOpCipherExecutor();
    }
}
