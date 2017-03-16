package org.apereo.cas.configuration.support;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.BindRequest;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.PooledSearchEntryResolver;
import org.ldaptive.pool.BindPassivator;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ClosePassivator;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.provider.Provider;
import org.ldaptive.sasl.CramMd5Config;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.ExternalConfig;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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
            bean.setMinimumIdle(jpaProperties.getPool().getMinSize());
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

        if (StringUtils.isNotBlank(jpaProperties.getDefaultCatalog())) {
            properties.put("hibernate.default_catalog", jpaProperties.getDefaultCatalog());
        }
        if (StringUtils.isNotBlank(jpaProperties.getDefaultSchema())) {
            properties.put("hibernate.default_schema", jpaProperties.getDefaultSchema());
        }
        bean.setJpaProperties(properties);
        return bean;
    }

    /**
     * New attribute repository person attribute dao.
     *
     * @param p the properties
     * @return the person attribute dao
     */
    public static IPersonAttributeDao newStubAttributeRepository(final PrincipalAttributesProperties p) {
        try {
            final NamedStubPersonAttributeDao dao = new NamedStubPersonAttributeDao();
            final Map pdirMap = new HashMap<>();
            p.getAttributes().entrySet().forEach(entry -> {
                final String[] vals = org.springframework.util.StringUtils.commaDelimitedListToStringArray(entry.getValue());
                pdirMap.put(entry.getKey(), Lists.newArrayList(vals));
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
                LOGGER.debug("Creating BCRYPT password encoder given the strength [{}] and secret in the configuration",
                        properties.getStrength());
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating BCRYPT encoder without secret");
                    return new BCryptPasswordEncoder(properties.getStrength());
                }

                LOGGER.debug("Creating BCRYPT encoder with secret");
                return new BCryptPasswordEncoder(properties.getStrength(),
                        new SecureRandom(properties.getSecret().getBytes(StandardCharsets.UTF_8)));
        }
    }


    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {

        final PrincipalNameTransformer res;
        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            final PrefixSuffixPrincipalNameTransformer t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
            res = t;
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
     * New dn resolver entry resolver.
     *
     * @param l the ldap settings
     * @return the entry resolver
     */
    public static EntryResolver newSearchEntryResolver(final LdapAuthenticationProperties l) {
        final PooledSearchEntryResolver entryResolver = new PooledSearchEntryResolver();
        entryResolver.setBaseDn(l.getBaseDn());
        entryResolver.setUserFilter(l.getUserFilter());
        entryResolver.setSubtreeSearch(l.isSubtreeSearch());
        entryResolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        return entryResolver;
    }


    /**
     * New connection config connection config.
     *
     * @param l the ldap properties
     * @return the connection config
     */
    public static ConnectionConfig newConnectionConfig(final AbstractLdapProperties l) {
        final ConnectionConfig cc = new ConnectionConfig();
        final String urls = Arrays.stream(l.getLdapUrl().split(",")).collect(Collectors.joining(" "));
        LOGGER.debug("Transformed LDAP urls from [{}] to [{}]", l.getLdapUrl(), urls);
        cc.setLdapUrl(urls);
        cc.setUseSSL(l.isUseSsl());
        cc.setUseStartTLS(l.isUseStartTls());
        cc.setConnectTimeout(newDuration(l.getConnectTimeout()));

        if (l.getTrustCertificates() != null) {
            final X509CredentialConfig cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(l.getTrustCertificates());
            cc.setSslConfig(new SslConfig(cfg));
        } else if (l.getKeystore() != null) {
            final KeyStoreCredentialConfig cfg = new KeyStoreCredentialConfig();
            cfg.setKeyStore(l.getKeystore());
            cfg.setKeyStorePassword(l.getKeystorePassword());
            cfg.setKeyStoreType(l.getKeystoreType());
            cc.setSslConfig(new SslConfig(cfg));
        } else {
            cc.setSslConfig(new SslConfig());
        }
        if (l.getSaslMechanism() != null) {
            final BindConnectionInitializer bc = new BindConnectionInitializer();
            final SaslConfig sc;
            switch (l.getSaslMechanism()) {
                case DIGEST_MD5:
                    sc = new DigestMd5Config();
                    ((DigestMd5Config) sc).setRealm(l.getSaslRealm());
                    break;
                case CRAM_MD5:
                    sc = new CramMd5Config();
                    break;
                case EXTERNAL:
                    sc = new ExternalConfig();
                    break;
                case GSSAPI:
                    sc = new GssApiConfig();
                    ((GssApiConfig) sc).setRealm(l.getSaslRealm());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SASL mechanism " + l.getSaslMechanism().name());
            }
            sc.setAuthorizationId(l.getSaslAuthorizationId());
            sc.setMutualAuthentication(l.getSaslMutualAuth());
            sc.setQualityOfProtection(l.getSaslQualityOfProtection());
            sc.setSecurityStrength(l.getSaslSecurityStrength());
            bc.setBindSaslConfig(sc);
            cc.setConnectionInitializer(bc);
        } else if (StringUtils.equals(l.getBindCredential(), "*") && StringUtils.equals(l.getBindDn(), "*")) {
            cc.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        } else if (StringUtils.isNotBlank(l.getBindDn()) && StringUtils.isNotBlank(l.getBindCredential())) {
            cc.setConnectionInitializer(new BindConnectionInitializer(l.getBindDn(), new Credential(l.getBindCredential())));
        }
        return cc;
    }

    /**
     * New pool config pool config.
     *
     * @param l the ldap properties
     * @return the pool config
     */
    public static PoolConfig newPoolConfig(final AbstractLdapProperties l) {
        final PoolConfig pc = new PoolConfig();
        pc.setMinPoolSize(l.getMinPoolSize());
        pc.setMaxPoolSize(l.getMaxPoolSize());
        pc.setValidateOnCheckOut(l.isValidateOnCheckout());
        pc.setValidatePeriodically(l.isValidatePeriodically());
        pc.setValidatePeriod(newDuration(l.getValidatePeriod()));
        return pc;
    }

    /**
     * New connection factory connection factory.
     *
     * @param l the l
     * @return the connection factory
     */
    public static DefaultConnectionFactory newConnectionFactory(final AbstractLdapProperties l) {
        final ConnectionConfig cc = newConnectionConfig(l);
        final DefaultConnectionFactory bindCf = new DefaultConnectionFactory(cc);
        if (l.getProviderClass() != null) {
            try {
                final Class clazz = ClassUtils.getClass(l.getProviderClass());
                bindCf.setProvider(Provider.class.cast(clazz.newInstance()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return bindCf;
    }

    /**
     * New blocking connection pool connection pool.
     *
     * @param l the l
     * @return the connection pool
     */
    public static ConnectionPool newBlockingConnectionPool(final AbstractLdapProperties l) {
        final DefaultConnectionFactory bindCf = newConnectionFactory(l);
        final PoolConfig pc = newPoolConfig(l);
        final BlockingConnectionPool cp = new BlockingConnectionPool(pc, bindCf);

        cp.setBlockWaitTime(newDuration(l.getBlockWaitTime()));
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        strategy.setIdleTime(newDuration(l.getIdleTime()));
        strategy.setPrunePeriod(newDuration(l.getPrunePeriod()));

        cp.setPruneStrategy(strategy);
        cp.setValidator(new SearchValidator());
        cp.setFailFastInitialize(l.isFailFast());

        if (StringUtils.isNotBlank(l.getPoolPassivator())) {
            final AbstractLdapProperties.LdapConnectionPoolPassivator pass =
                    AbstractLdapProperties.LdapConnectionPoolPassivator.valueOf(l.getPoolPassivator().toUpperCase());
            switch (pass) {
                case CLOSE:
                    cp.setPassivator(new ClosePassivator());
                    break;
                case BIND:
                    LOGGER.debug("Creating a bind passivator instance for the connection pool");
                    final BindRequest bindRequest = new BindRequest();
                    bindRequest.setDn(l.getBindDn());
                    bindRequest.setCredential(new Credential(l.getBindCredential()));
                    cp.setPassivator(new BindPassivator(bindRequest));
                    break;
                default:
                    break;
            }
        }
        
        LOGGER.debug("Initializing ldap connection pool for {} and bindDn {}", l.getLdapUrl(), l.getBindDn());
        cp.initialize();
        return cp;
    }

    /**
     * New pooled connection factory pooled connection factory.
     *
     * @param l the ldap properties
     * @return the pooled connection factory
     */
    public static PooledConnectionFactory newPooledConnectionFactory(final AbstractLdapProperties l) {
        final ConnectionPool cp = newBlockingConnectionPool(l);
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
        LOGGER.info("Ticket registry encryption/signing is turned off. This MAY NOT be safe in a "
                + "clustered production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "ticket registry tickets, and verify the chosen ticket registry does support this behavior.");
        return new NoOpCipherExecutor();
    }

    /**
     * Builds a new request.
     *
     * @param baseDn the base dn
     * @param filter the filter
     * @return the search request
     */
    public static SearchRequest newSearchRequest(final String baseDn, final SearchFilter filter) {
        final SearchRequest sr = new SearchRequest(baseDn, filter);
        sr.setBinaryAttributes(ReturnAttributes.ALL_USER.value());
        sr.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        sr.setSearchScope(SearchScope.SUBTREE);
        return sr;
    }

    /**
     * Constructs a new search filter using {@link SearchExecutor#searchFilter} as a template and
     * the username as a parameter.
     *
     * @param filterQuery the query filter
     * @param params      the username
     * @return Search filter with parameters applied.
     */
    public static SearchFilter newSearchFilter(final String filterQuery, final String... params) {
        final SearchFilter filter = new SearchFilter();
        filter.setFilter(filterQuery);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (filter.getFilter().contains("{" + i + "}")) {
                    filter.setParameter(i, params[i]);
                } else {
                    filter.setParameter("user", params[i]);
                }
            }
        }
        LOGGER.debug("Constructed LDAP search filter [{}]", filter.format());
        return filter;
    }

    /**
     * New search executor search executor.
     *
     * @param baseDn      the base dn
     * @param filterQuery the filter query
     * @param params      the params
     * @return the search executor
     */
    public static SearchExecutor newSearchExecutor(final String baseDn, final String filterQuery, final String... params) {
        final SearchExecutor executor = new SearchExecutor();
        executor.setBaseDn(baseDn);
        executor.setSearchFilter(newSearchFilter(filterQuery, params));
        executor.setReturnAttributes(ReturnAttributes.ALL.value());
        executor.setSearchScope(SearchScope.SUBTREE);
        return executor;
    }
}
