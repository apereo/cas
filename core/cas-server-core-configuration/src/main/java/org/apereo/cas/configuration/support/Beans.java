package org.apereo.cas.configuration.support;

import com.google.common.base.Throwables;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.zaxxer.hikari.HikariDataSource;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.crypto.DefaultPasswordEncoder;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.DefaultConnectionStrategy;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.LdapAttribute;
import org.ldaptive.RandomConnectionStrategy;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.RoundRobinConnectionStrategy;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.ad.handler.PrimaryGroupIdHandler;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.PooledSearchEntryResolver;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.RecursiveEntryHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.pool.BindPassivator;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ClosePassivator;
import org.ldaptive.pool.CompareValidator;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.provider.Provider;
import org.ldaptive.referral.SearchReferralHandler;
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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * {@code @Bean} creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public final class Beans {
    /**
     * Default parameter name in search filters for ldap.
     */
    public static final String LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME = "user";

    private static final Logger LOGGER = LoggerFactory.getLogger(Beans.class);

    protected Beans() {
    }

    /**
     * Get new data source, from JNDI lookup or created via direct configuration
     * of Hikari pool.
     * <p>
     * If jpaProperties contains {@link AbstractJpaProperties#getDataSourceName()} a lookup will be
     * attempted. If the DataSource is not found via JNDI then CAS will attempt to
     * configure a Hikari connection pool.
     * <p>
     * Since the datasource beans are {@link org.springframework.cloud.context.config.annotation.RefreshScope},
     * they will be a proxied by Spring
     * and on some application servers there have been classloading issues. A workaround
     * for this is to use the {@link AbstractJpaProperties#isDataSourceProxy()} setting and then the dataSource will be
     * wrapped in an application level class. If that is an issue, don't do it.
     *
     * @param jpaProperties the jpa properties
     * @return the data source
     */
    public static DataSource newDataSource(final AbstractJpaProperties jpaProperties) {
        final String dataSourceName = jpaProperties.getDataSourceName();
        final boolean proxyDataSource = jpaProperties.isDataSourceProxy();

        if (StringUtils.isNotBlank(dataSourceName)) {
            try {
                final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
                /*
                 if user wants to do lookup as resource, they may include java:/comp/env
                 in dataSourceName and put resource reference in web.xml
                 otherwise dataSourceName is used as JNDI name
                  */
                dsLookup.setResourceRef(false);
                final DataSource containerDataSource = dsLookup.getDataSource(dataSourceName);
                if (!proxyDataSource) {
                    return containerDataSource;
                }
                return new DataSourceProxy(containerDataSource);
            } catch (final DataSourceLookupFailureException e) {
                LOGGER.warn("Lookup of datasource [{}] failed due to {} "
                        + "falling back to configuration via JPA properties.", dataSourceName, e.getMessage());
            }
        }

        try {
            final HikariDataSource bean = new HikariDataSource();
            if (StringUtils.isNotBlank(jpaProperties.getDriverClass())) {
                bean.setDriverClassName(jpaProperties.getDriverClass());
            }
            bean.setJdbcUrl(jpaProperties.getUrl());
            bean.setUsername(jpaProperties.getUser());
            bean.setPassword(jpaProperties.getPassword());
            bean.setLoginTimeout(Long.valueOf(jpaProperties.getPool().getMaxWait()).intValue());
            bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
            bean.setMinimumIdle(jpaProperties.getPool().getMinSize());
            bean.setIdleTimeout(jpaProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
            bean.setInitializationFailTimeout(jpaProperties.isFailFast() ? 1 : 0);
            bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
            bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
            bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
            bean.setAutoCommit(jpaProperties.isAutocommit());
            bean.setValidationTimeout(jpaProperties.getPool().getTimeoutMillis());
            return bean;
        } catch (final Exception e) {
            LOGGER.error("Error creating DataSource: [{}]", e.getMessage());
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
        bean.setKeepAliveSeconds(Long.valueOf(config.getMaxWait()).intValue());
        return bean;
    }

    /**
     * New entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newHibernateEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                              final AbstractJpaProperties jpaProperties) {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
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
        properties.putAll(jpaProperties.getProperties());
        bean.setJpaProperties(properties);
        bean.getJpaPropertyMap().put("hibernate.enable_lazy_load_no_trans", Boolean.TRUE);
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
            final Map<String, List<Object>> pdirMap = new HashMap<>();
            p.getStub().getAttributes().forEach((key, value) -> {
                final String[] vals = org.springframework.util.StringUtils.commaDelimitedListToStringArray(value);
                pdirMap.put(key, Arrays.asList((Object[]) vals));
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
        final String type = properties.getType();
        if (StringUtils.isBlank(type)) {
            LOGGER.debug("No password encoder type is defined, and so none shall be created");
            return NoOpPasswordEncoder.getInstance();
        }

        if (type.contains(".")) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password encoder [{}]", type);
                final Class<PasswordEncoder> clazz = (Class<PasswordEncoder>) Class.forName(type);
                return clazz.newInstance();
            } catch (final Exception e) {
                LOGGER.error("Falling back to a no-op password encoder as CAS has failed to create "
                        + "an instance of the custom password encoder class " + type, e);
                return NoOpPasswordEncoder.getInstance();
            }
        }

        final PasswordEncoderProperties.PasswordEncoderTypes encoderType = PasswordEncoderProperties.PasswordEncoderTypes.valueOf(type);
        switch (encoderType) {
            case DEFAULT:
                LOGGER.debug("Creating default password encoder with encoding alg [{}] and character encoding [{}]",
                        properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
                return new DefaultPasswordEncoder(properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
            case STANDARD:
                LOGGER.debug("Creating standard password encoder with the secret defined in the configuration");
                return new StandardPasswordEncoder(properties.getSecret());
            case BCRYPT:
                LOGGER.debug("Creating BCRYPT password encoder given the strength [{}] and secret in the configuration",
                        properties.getStrength());
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating BCRYPT encoder without secret");
                    return new BCryptPasswordEncoder(properties.getStrength());
                }
                LOGGER.debug("Creating BCRYPT encoder with secret");
                return new BCryptPasswordEncoder(properties.getStrength(),
                        new SecureRandom(properties.getSecret().getBytes(StandardCharsets.UTF_8)));
            case SCRYPT:
                LOGGER.debug("Creating SCRYPT encoder");
                return new SCryptPasswordEncoder();
            case PBKDF2:
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating PBKDF2 encoder without secret");
                    return new Pbkdf2PasswordEncoder();
                }
                final int hashWidth = 256;
                return new Pbkdf2PasswordEncoder(properties.getSecret(), properties.getStrength(), hashWidth);
            case NONE:
            default:
                LOGGER.debug("No password encoder shall be created given the requested encoder type [{}]", type);
                return NoOpPasswordEncoder.getInstance();
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
     * Creates the necessary search entry resolver.
     *
     * @param l       the ldap settings
     * @param factory the factory
     * @return the entry resolver
     */
    public static EntryResolver newLdaptiveSearchEntryResolver(final AbstractLdapAuthenticationProperties l,
                                                               final PooledConnectionFactory factory) {
        if (StringUtils.isBlank(l.getBaseDn())) {
            throw new IllegalArgumentException("To create a search entry resolver, base dn cannot be empty/blank ");
        }
        if (StringUtils.isBlank(l.getUserFilter())) {
            throw new IllegalArgumentException("To create a search entry resolver, user filter cannot be empty/blank");
        }

        final PooledSearchEntryResolver entryResolver = new PooledSearchEntryResolver();
        entryResolver.setBaseDn(l.getBaseDn());
        entryResolver.setUserFilter(l.getUserFilter());
        entryResolver.setSubtreeSearch(l.isSubtreeSearch());
        entryResolver.setConnectionFactory(factory);

        final List<SearchEntryHandler> handlers = new ArrayList<>();
        l.getSearchEntryHandlers().forEach(h -> {
            switch (h.getType()) {
                case CASE_CHANGE:
                    final CaseChangeEntryHandler eh = new CaseChangeEntryHandler();
                    eh.setAttributeNameCaseChange(h.getCasChange().getAttributeNameCaseChange());
                    eh.setAttributeNames(h.getCasChange().getAttributeNames());
                    eh.setAttributeValueCaseChange(h.getCasChange().getAttributeValueCaseChange());
                    eh.setDnCaseChange(h.getCasChange().getDnCaseChange());
                    handlers.add(eh);
                    break;
                case DN_ATTRIBUTE_ENTRY:
                    final DnAttributeEntryHandler ehd = new DnAttributeEntryHandler();
                    ehd.setAddIfExists(h.getDnAttribute().isAddIfExists());
                    ehd.setDnAttributeName(h.getDnAttribute().getDnAttributeName());
                    handlers.add(ehd);
                    break;
                case MERGE:
                    final MergeAttributeEntryHandler ehm = new MergeAttributeEntryHandler();
                    ehm.setAttributeNames(h.getMergeAttribute().getAttributeNames());
                    ehm.setMergeAttributeName(h.getMergeAttribute().getMergeAttributeName());
                    handlers.add(ehm);
                    break;
                case OBJECT_GUID:
                    handlers.add(new ObjectGuidHandler());
                    break;
                case OBJECT_SID:
                    handlers.add(new ObjectSidHandler());
                    break;
                case PRIMARY_GROUP:
                    final PrimaryGroupIdHandler ehp = new PrimaryGroupIdHandler();
                    ehp.setBaseDn(h.getPrimaryGroupId().getBaseDn());
                    ehp.setGroupFilter(h.getPrimaryGroupId().getGroupFilter());
                    handlers.add(ehp);
                    break;
                case RANGE_ENTRY:
                    handlers.add(new RangeEntryHandler());
                    break;
                case RECURSIVE_ENTRY:
                    handlers.add(new RecursiveEntryHandler(h.getRecursive().getSearchAttribute(), h.getRecursive().getMergeAttributes()));
                    break;
                default:
                    break;
            }
        });

        if (!handlers.isEmpty()) {
            LOGGER.debug("Search entry handlers defined for the entry resolver of [{}] are [{}]", l.getLdapUrl(), handlers);
            entryResolver.setSearchEntryHandlers(handlers.toArray(new SearchEntryHandler[]{}));
        }
        return entryResolver;
    }


    /**
     * Transform principal attributes into map.
     * Items in the list are defined in the syntax of "cn", or "cn:commonName" for virtual renaming and maps.
     *
     * @param list the list
     * @return the map
     */
    public static Map<String, String> transformPrincipalAttributesListIntoMap(final List<String> list) {
        final Map<String, String> attributes = new HashMap<>();

        if (list.isEmpty()) {
            LOGGER.debug("No principal attributes are defined");
        } else {
            list.forEach(a -> {
                final String attributeName = a.trim();
                if (attributeName.contains(":")) {
                    final String[] attrCombo = attributeName.split(":");
                    final String name = attrCombo[0].trim();
                    final String value = attrCombo[1].trim();
                    LOGGER.debug("Mapped principal attribute name [{}] to [{}]", name, value);
                    attributes.put(name, value);
                } else {
                    LOGGER.debug("Mapped principal attribute name [{}]", attributeName);
                    attributes.put(attributeName, attributeName);
                }
            });
        }
        return attributes;
    }

    /**
     * New connection config connection config.
     *
     * @param l the ldap properties
     * @return the connection config
     */
    public static ConnectionConfig newLdaptiveConnectionConfig(final AbstractLdapProperties l) {
        if (StringUtils.isBlank(l.getLdapUrl())) {
            throw new IllegalArgumentException("LDAP url cannot be empty/blank");
        }

        LOGGER.debug("Creating LDAP connection configuration for [{}]", l.getLdapUrl());
        final ConnectionConfig cc = new ConnectionConfig();

        final String urls = l.getLdapUrl().contains(" ")
                ? l.getLdapUrl()
                : Arrays.stream(l.getLdapUrl().split(",")).collect(Collectors.joining(" "));
        LOGGER.debug("Transformed LDAP urls from [{}] to [{}]", l.getLdapUrl(), urls);
        cc.setLdapUrl(urls);

        cc.setUseSSL(l.isUseSsl());
        cc.setUseStartTLS(l.isUseStartTls());
        cc.setConnectTimeout(newDuration(l.getConnectTimeout()));
        cc.setResponseTimeout(newDuration(l.getResponseTimeout()));

        if (StringUtils.isNotBlank(l.getConnectionStrategy())) {
            final AbstractLdapProperties.LdapConnectionStrategy strategy =
                    AbstractLdapProperties.LdapConnectionStrategy.valueOf(l.getConnectionStrategy());
            switch (strategy) {
                case RANDOM:
                    cc.setConnectionStrategy(new RandomConnectionStrategy());
                    break;
                case DNS_SRV:
                    cc.setConnectionStrategy(new DnsSrvConnectionStrategy());
                    break;
                case ACTIVE_PASSIVE:
                    cc.setConnectionStrategy(new ActivePassiveConnectionStrategy());
                    break;
                case ROUND_ROBIN:
                    cc.setConnectionStrategy(new RoundRobinConnectionStrategy());
                    break;
                case DEFAULT:
                default:
                    cc.setConnectionStrategy(new DefaultConnectionStrategy());
                    break;
            }
        }

        if (l.getTrustCertificates() != null) {
            LOGGER.debug("Creating LDAP SSL configuration via trust certificates [{}]", l.getTrustCertificates());
            final X509CredentialConfig cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(l.getTrustCertificates());
            cc.setSslConfig(new SslConfig(cfg));

        } else if (l.getKeystore() != null) {
            LOGGER.debug("Creating LDAP SSL configuration via keystore [{}]", l.getKeystore());
            final KeyStoreCredentialConfig cfg = new KeyStoreCredentialConfig();
            cfg.setKeyStore(l.getKeystore());
            cfg.setKeyStorePassword(l.getKeystorePassword());
            cfg.setKeyStoreType(l.getKeystoreType());
            cc.setSslConfig(new SslConfig(cfg));
        } else {
            LOGGER.debug("Creating LDAP SSL configuration via the native JVM truststore");
            cc.setSslConfig(new SslConfig());
        }
        if (l.getSaslMechanism() != null) {
            LOGGER.debug("Creating LDAP SASL mechanism via [{}]", l.getSaslMechanism());

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
            LOGGER.debug("Creating LDAP fast-bind connection initializer");
            cc.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        } else if (StringUtils.isNotBlank(l.getBindDn()) && StringUtils.isNotBlank(l.getBindCredential())) {
            LOGGER.debug("Creating LDAP bind connection initializer via [{}]", l.getBindDn());
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
    public static PoolConfig newLdaptivePoolConfig(final AbstractLdapProperties l) {
        LOGGER.debug("Creating LDAP connection pool configuration for [{}]", l.getLdapUrl());
        final PoolConfig pc = new PoolConfig();
        pc.setMinPoolSize(l.getMinPoolSize());
        pc.setMaxPoolSize(l.getMaxPoolSize());
        pc.setValidateOnCheckOut(l.isValidateOnCheckout());
        pc.setValidatePeriodically(l.isValidatePeriodically());
        pc.setValidatePeriod(newDuration(l.getValidatePeriod()));
        pc.setValidateTimeout(newDuration(l.getValidateTimeout()));
        return pc;
    }

    /**
     * New connection factory connection factory.
     *
     * @param l the l
     * @return the connection factory
     */
    public static DefaultConnectionFactory newLdaptiveConnectionFactory(final AbstractLdapProperties l) {
        LOGGER.debug("Creating LDAP connection factory for [{}]", l.getLdapUrl());
        final ConnectionConfig cc = newLdaptiveConnectionConfig(l);
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
    public static ConnectionPool newLdaptiveBlockingConnectionPool(final AbstractLdapProperties l) {
        final DefaultConnectionFactory bindCf = newLdaptiveConnectionFactory(l);
        final PoolConfig pc = newLdaptivePoolConfig(l);
        final BlockingConnectionPool cp = new BlockingConnectionPool(pc, bindCf);

        cp.setBlockWaitTime(newDuration(l.getBlockWaitTime()));
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        strategy.setIdleTime(newDuration(l.getIdleTime()));
        strategy.setPrunePeriod(newDuration(l.getPrunePeriod()));

        cp.setPruneStrategy(strategy);

        switch (l.getValidator().getType().trim().toLowerCase()) {
            case "compare":
                final CompareRequest compareRequest = new CompareRequest();
                compareRequest.setDn(l.getValidator().getDn());
                compareRequest.setAttribute(new LdapAttribute(l.getValidator().getAttributeName(),
                        l.getValidator().getAttributeValues().toArray(new String[]{})));
                compareRequest.setReferralHandler(new SearchReferralHandler());
                cp.setValidator(new CompareValidator(compareRequest));
                break;
            case "none":
                LOGGER.debug("No validator is configured for the LDAP connection pool of [{}]", l.getLdapUrl());
                break;
            case "search":
            default:
                final SearchRequest searchRequest = new SearchRequest();
                searchRequest.setBaseDn(l.getValidator().getBaseDn());
                searchRequest.setSearchFilter(new SearchFilter(l.getValidator().getSearchFilter()));
                searchRequest.setReturnAttributes(ReturnAttributes.NONE.value());
                searchRequest.setSearchScope(l.getValidator().getScope());
                searchRequest.setSizeLimit(1L);
                searchRequest.setReferralHandler(new SearchReferralHandler());
                cp.setValidator(new SearchValidator(searchRequest));
                break;
        }

        cp.setFailFastInitialize(l.isFailFast());

        if (StringUtils.isNotBlank(l.getPoolPassivator())) {
            final AbstractLdapProperties.LdapConnectionPoolPassivator pass =
                    AbstractLdapProperties.LdapConnectionPoolPassivator.valueOf(l.getPoolPassivator().toUpperCase());
            switch (pass) {
                case CLOSE:
                    cp.setPassivator(new ClosePassivator());
                    LOGGER.debug("Created [{}] passivator for [{}]", l.getPoolPassivator(), l.getLdapUrl());
                    break;
                case BIND:
                    if (StringUtils.isNotBlank(l.getBindDn()) && StringUtils.isNoneBlank(l.getBindCredential())) {
                        final BindRequest bindRequest = new BindRequest();
                        bindRequest.setDn(l.getBindDn());
                        bindRequest.setCredential(new Credential(l.getBindCredential()));
                        cp.setPassivator(new BindPassivator(bindRequest));
                        LOGGER.debug("Created [{}] passivator for [{}]", l.getPoolPassivator(), l.getLdapUrl());
                    } else {
                        LOGGER.warn("No [{}] passivator could be created for [{}] given bind credentials are not specified",
                                l.getPoolPassivator(), l.getLdapUrl());
                    }
                    break;
                default:
                    break;
            }
        }

        LOGGER.debug("Initializing ldap connection pool for [{}] and bindDn [{}]", l.getLdapUrl(), l.getBindDn());
        cp.initialize();
        return cp;
    }

    /**
     * Gets credential selection predicate.
     *
     * @param selectionCriteria the selection criteria
     * @return the credential selection predicate
     */
    public static Predicate<org.apereo.cas.authentication.Credential> newCredentialSelectionPredicate(final String selectionCriteria) {
        try {
            if (StringUtils.isBlank(selectionCriteria)) {
                return credential -> true;
            }

            if (selectionCriteria.endsWith(".groovy")) {
                final ResourceLoader loader = new DefaultResourceLoader();
                final Resource resource = loader.getResource(selectionCriteria);
                if (resource != null) {
                    final String script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                    final GroovyClassLoader classLoader = new GroovyClassLoader(Beans.class.getClassLoader(),
                            new CompilerConfiguration(), true);
                    final Class<Predicate> clz = classLoader.parseClass(script);
                    return clz.newInstance();
                }
            }

            final Class predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<org.apereo.cas.authentication.Credential>) predicateClazz.newInstance();
        } catch (final Exception e) {
            final Predicate<String> predicate = Pattern.compile(selectionCriteria).asPredicate();
            return credential -> predicate.test(credential.getId());
        }
    }

    /**
     * New pooled connection factory pooled connection factory.
     *
     * @param l the ldap properties
     * @return the pooled connection factory
     */
    public static PooledConnectionFactory newLdaptivePooledConnectionFactory(final AbstractLdapProperties l) {
        final ConnectionPool cp = newLdaptiveBlockingConnectionPool(l);
        return new PooledConnectionFactory(cp);
    }

    /**
     * New duration. If the provided length is duration,
     * it will be parsed accordingly, or if it's a numeric value
     * it will be pared as a duration assuming it's provided as seconds.
     *
     * @param length the length in seconds.
     * @return the duration
     */
    public static Duration newDuration(final String length) {
        try {
            if (NumberUtils.isCreatable(length)) {
                return Duration.ofSeconds(Long.valueOf(length));
            }
            return Duration.parse(length);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry the registry
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final CryptographyProperties registry) {
        return newTicketRegistryCipherExecutor(registry, false);
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry         the registry
     * @param forceIfBlankKeys the force if blank keys
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final CryptographyProperties registry, final boolean forceIfBlankKeys) {
        if (StringUtils.isNotBlank(registry.getEncryption().getKey())
                && StringUtils.isNotBlank(registry.getEncryption().getKey())
                || forceIfBlankKeys) {
            return new DefaultTicketCipherExecutor(
                    registry.getEncryption().getKey(),
                    registry.getSigning().getKey(),
                    registry.getAlg(),
                    registry.getSigning().getKeySize(),
                    registry.getEncryption().getKeySize());
        }
        LOGGER.debug("Ticket registry encryption/signing is turned off. This MAY NOT be safe in a clustered production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "ticket registry tickets, and verify the chosen ticket registry does support this behavior.");
        return NoOpCipherExecutor.getInstance();
    }

    /**
     * Builds a new request.
     *
     * @param baseDn           the base dn
     * @param filter           the filter
     * @param binaryAttributes the binary attributes
     * @param returnAttributes the return attributes
     * @return the search request
     */
    public static SearchRequest newLdaptiveSearchRequest(final String baseDn,
                                                         final SearchFilter filter,
                                                         final String[] binaryAttributes,
                                                         final String[] returnAttributes) {
        final SearchRequest sr = new SearchRequest(baseDn, filter);
        sr.setBinaryAttributes(binaryAttributes);
        sr.setReturnAttributes(returnAttributes);
        sr.setSearchScope(SearchScope.SUBTREE);
        return sr;
    }

    /**
     * New ldaptive search request.
     * Returns all attributes.
     *
     * @param baseDn the base dn
     * @param filter the filter
     * @return the search request
     */
    public static SearchRequest newLdaptiveSearchRequest(final String baseDn,
                                                         final SearchFilter filter) {
        return newLdaptiveSearchRequest(baseDn, filter, ReturnAttributes.ALL_USER.value(), ReturnAttributes.ALL_USER.value());
    }

    /**
     * Constructs a new search filter using {@link SearchExecutor#searchFilter} as a template and
     * the username as a parameter.
     *
     * @param filterQuery the query filter
     * @return Search filter with parameters applied.
     */
    public static SearchFilter newLdaptiveSearchFilter(final String filterQuery) {
        return newLdaptiveSearchFilter(filterQuery, Collections.emptyList());
    }

    /**
     * Constructs a new search filter using {@link SearchExecutor#searchFilter} as a template and
     * the username as a parameter.
     *
     * @param filterQuery the query filter
     * @param params      the username
     * @return Search filter with parameters applied.
     */
    public static SearchFilter newLdaptiveSearchFilter(final String filterQuery, final List<String> params) {
        return newLdaptiveSearchFilter(filterQuery, LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, params);
    }

    /**
     * Constructs a new search filter using {@link SearchExecutor#searchFilter} as a template and
     * the username as a parameter.
     *
     * @param filterQuery the query filter
     * @param paramName   the param name
     * @param params      the username
     * @return Search filter with parameters applied.
     */
    public static SearchFilter newLdaptiveSearchFilter(final String filterQuery, final String paramName, final List<String> params) {
        final SearchFilter filter = new SearchFilter();
        filter.setFilter(filterQuery);
        if (params != null) {
            IntStream.range(0, params.size()).forEach(i -> {
                if (filter.getFilter().contains("{" + i + '}')) {
                    filter.setParameter(i, params.get(i));
                } else {
                    filter.setParameter(paramName, params.get(i));
                }
            });
        }
        LOGGER.debug("Constructed LDAP search filter [{}]", filter.format());
        return filter;
    }

    /**
     * New search executor.
     *
     * @param baseDn      the base dn
     * @param filterQuery the filter query
     * @param params      the params
     * @return the search executor
     */
    public static SearchExecutor newLdaptiveSearchExecutor(final String baseDn, final String filterQuery, final List<String> params) {
        return newLdaptiveSearchExecutor(baseDn, filterQuery, params, ReturnAttributes.ALL.value());
    }

    /**
     * New ldaptive search executor search executor.
     *
     * @param baseDn           the base dn
     * @param filterQuery      the filter query
     * @param params           the params
     * @param returnAttributes the return attributes
     * @return the search executor
     */
    public static SearchExecutor newLdaptiveSearchExecutor(final String baseDn, final String filterQuery,
                                                           final List<String> params,
                                                           final List<String> returnAttributes) {
        return newLdaptiveSearchExecutor(baseDn, filterQuery, params, returnAttributes.toArray(new String[]{}));
    }

    /**
     * New ldaptive search executor search executor.
     *
     * @param baseDn           the base dn
     * @param filterQuery      the filter query
     * @param params           the params
     * @param returnAttributes the return attributes
     * @return the search executor
     */
    public static SearchExecutor newLdaptiveSearchExecutor(final String baseDn, final String filterQuery,
                                                           final List<String> params,
                                                           final String[] returnAttributes) {
        final SearchExecutor executor = new SearchExecutor();
        executor.setBaseDn(baseDn);
        executor.setSearchFilter(newLdaptiveSearchFilter(filterQuery, params));
        executor.setReturnAttributes(returnAttributes);
        executor.setSearchScope(SearchScope.SUBTREE);
        return executor;
    }

    /**
     * New search executor search executor.
     *
     * @param baseDn      the base dn
     * @param filterQuery the filter query
     * @return the search executor
     */
    public static SearchExecutor newLdaptiveSearchExecutor(final String baseDn, final String filterQuery) {
        return newLdaptiveSearchExecutor(baseDn, filterQuery, Collections.emptyList());
    }

    /**
     * New mongo db client options factory bean.
     *
     * @param mongo the mongo properties.
     * @return the mongo client options factory bean
     */
    public static MongoClientOptionsFactoryBean newMongoDbClientOptionsFactoryBean(final AbstractMongoInstanceProperties mongo) {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setWriteConcern(WriteConcern.valueOf(mongo.getWriteConcern()));
            bean.setHeartbeatConnectTimeout(Long.valueOf(mongo.getTimeout()).intValue());
            bean.setHeartbeatSocketTimeout(Long.valueOf(mongo.getTimeout()).intValue());
            bean.setMaxConnectionLifeTime(mongo.getConns().getLifetime());
            bean.setSocketKeepAlive(mongo.isSocketKeepAlive());
            bean.setMaxConnectionIdleTime(Long.valueOf(mongo.getIdleTimeout()).intValue());
            bean.setConnectionsPerHost(mongo.getConns().getPerHost());
            bean.setSocketTimeout(Long.valueOf(mongo.getTimeout()).intValue());
            bean.setConnectTimeout(Long.valueOf(mongo.getTimeout()).intValue());
            bean.afterPropertiesSet();
            return bean;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    /**
     * New mongo db client options.
     *
     * @param mongo the mongo
     * @return the mongo client options
     */
    public static MongoClientOptions newMongoDbClientOptions(final AbstractMongoInstanceProperties mongo) {
        try {
            return newMongoDbClientOptionsFactoryBean(mongo).getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    /**
     * New mongo db client.
     *
     * @param mongo the mongo
     * @return the mongo
     */
    public static Mongo newMongoDbClient(final AbstractMongoInstanceProperties mongo) {
        return new MongoClient(new ServerAddress(
                mongo.getHost(),
                mongo.getPort()),
                Collections.singletonList(
                        MongoCredential.createCredential(
                                mongo.getUserId(),
                                mongo.getDatabaseName(),
                                mongo.getPassword().toCharArray())),
                newMongoDbClientOptions(mongo));
    }

    /**
     * New ldap authenticator.
     *
     * @param l the ldap settings.
     * @return the authenticator
     */
    public static Authenticator newLdaptiveAuthenticator(final AbstractLdapAuthenticationProperties l) {
        switch (l.getType()) {
            case AD:
                LOGGER.debug("Creating active directory authenticator for [{}]", l.getLdapUrl());
                return getActiveDirectoryAuthenticator(l);
            case DIRECT:
                LOGGER.debug("Creating direct-bind authenticator for [{}]", l.getLdapUrl());
                return getDirectBindAuthenticator(l);
            case AUTHENTICATED:
                LOGGER.debug("Creating authenticated authenticator for [{}]", l.getLdapUrl());
                return getAuthenticatedOrAnonSearchAuthenticator(l);
            default:
                LOGGER.debug("Creating anonymous authenticator for [{}]", l.getLdapUrl());
                return getAuthenticatedOrAnonSearchAuthenticator(l);
        }
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final AbstractLdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getBaseDn())) {
            throw new IllegalArgumentException("Base dn cannot be empty/blank for authenticated/anonymous authentication");
        }
        if (StringUtils.isBlank(l.getUserFilter())) {
            throw new IllegalArgumentException("User filter cannot be empty/blank for authenticated/anonymous authentication");
        }
        final PooledConnectionFactory connectionFactoryForSearch = Beans.newLdaptivePooledConnectionFactory(l);
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(connectionFactoryForSearch);
        resolver.setUserFilter(l.getUserFilter());

        final Authenticator auth;
        if (StringUtils.isBlank(l.getPrincipalAttributePassword())) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, Beans.newLdaptivePooledConnectionFactory(l)));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(l, Beans.newLdaptivePooledConnectionFactory(l)));
        }

        if (l.isEnhanceWithEntryResolver()) {
            auth.setEntryResolver(Beans.newLdaptiveSearchEntryResolver(l, Beans.newLdaptivePooledConnectionFactory(l)));
        }
        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final AbstractLdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for direct bind authentication");
        }
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authenticator = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, Beans.newLdaptivePooledConnectionFactory(l)));

        if (l.isEnhanceWithEntryResolver()) {
            authenticator.setEntryResolver(Beans.newLdaptiveSearchEntryResolver(l, Beans.newLdaptivePooledConnectionFactory(l)));
        }
        return authenticator;
    }

    private static Authenticator getActiveDirectoryAuthenticator(final AbstractLdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for active directory authentication");
        }
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, Beans.newLdaptivePooledConnectionFactory(l)));

        if (l.isEnhanceWithEntryResolver()) {
            authn.setEntryResolver(Beans.newLdaptiveSearchEntryResolver(l, Beans.newLdaptivePooledConnectionFactory(l)));
        }
        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final AbstractLdapAuthenticationProperties l,
                                                                                      final PooledConnectionFactory factory) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(factory);
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private static PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(final AbstractLdapAuthenticationProperties l,
                                                                                            final PooledConnectionFactory factory) {
        final PooledCompareAuthenticationHandler handler = new PooledCompareAuthenticationHandler(factory);
        handler.setPasswordAttribute(l.getPrincipalAttributePassword());
        return handler;
    }
}
