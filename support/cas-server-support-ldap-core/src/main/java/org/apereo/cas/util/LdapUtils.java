package org.apereo.cas.util;

import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandler;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandler;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties.PasswordPolicyHandlingOptions;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPasswordPolicyProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapSearchEntryHandlersProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.persondir.ActiveDirectoryLdapEntryHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.CompareConnectionValidator;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.DerefAliases;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.RandomConnectionStrategy;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.RoundRobinConnectionStrategy;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchScope;
import org.ldaptive.SimpleBindRequest;
import org.ldaptive.ad.extended.FastBindConnectionInitializer;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.ad.handler.PrimaryGroupIdHandler;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.auth.AuthenticationCriteria;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationHandlerResponse;
import org.ldaptive.auth.AuthenticationRequestHandler;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.CompareAuthenticationHandler;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;
import org.ldaptive.auth.User;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationRequestHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.MergeResultHandler;
import org.ldaptive.handler.RecursiveResultHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.pool.BindConnectionPassivator;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.ldaptive.ssl.AllowAnyHostnameVerifier;
import org.ldaptive.ssl.AllowAnyTrustManager;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.DefaultTrustManager;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.context.ApplicationContext;
import javax.security.auth.login.AccountNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Slf4j
@UtilityClass
public class LdapUtils {
    /**
     * Default parameter name in search filters for ldap.
     */
    public static final String LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME = "user";

    /**
     * The objectClass attribute.
     */
    public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

    /**
     * Delimiter character to separate multiple base-dns
     * that belongs to the same LDAP instance.
     */
    private static final String BASE_DN_DELIMITER = "|";

    private static final String LDAP_PREFIX = "ldap";

    /**
     * Reads a Boolean value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return {@code true} if the attribute's value matches (case-insensitive) {@code "true"}, otherwise false
     */
    public static Boolean getBoolean(final LdapEntry ctx, final String attribute, final Boolean nullValue) {
        val v = getString(ctx, attribute, nullValue.toString());
        return v.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * Reads a Long value from the LdapEntry.
     *
     * @param entry     the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return the long value
     */
    public static Long getLong(final LdapEntry entry, final String attribute, final Long nullValue) {
        val v = getString(entry, attribute, String.valueOf(nullValue));
        return Long.valueOf(v);
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param entry     the ldap entry
     * @param attribute the attribute name
     * @return the string
     */
    public static String getString(final LdapEntry entry, final String attribute) {
        return getString(entry, attribute, null);
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param entry     the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return the string
     */
    public static String getString(final LdapEntry entry, final String attribute, final String nullValue) {
        val attr = entry.getAttribute(attribute);
        if (attr == null) {
            return nullValue;
        }

        val v = attr.isBinary()
            ? new String(attr.getBinaryValue(), StandardCharsets.UTF_8)
            : attr.getStringValue();

        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        return nullValue;
    }


    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    public static boolean containsResultEntry(final SearchResponse response) {
        return response != null && response.getEntry() != null;
    }

    /**
     * Is ldap connection url?.
     *
     * @param r the resource
     * @return true/false
     */
    public static boolean isLdapConnectionUrl(final String r) {
        return r.toLowerCase(Locale.ENGLISH).startsWith(LDAP_PREFIX);
    }

    /**
     * Is ldap connection url?.
     *
     * @param r the resource
     * @return true/false
     */
    public static boolean isLdapConnectionUrl(final URI r) {
        return r.getScheme().equalsIgnoreCase(LDAP_PREFIX);
    }

    /**
     * Is ldap connection url?.
     *
     * @param r the resource
     * @return true/false
     */
    public static boolean isLdapConnectionUrl(final URL r) {
        return r.getProtocol().equalsIgnoreCase(LDAP_PREFIX);
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
                                                         final FilterTemplate filter,
                                                         final String[] binaryAttributes,
                                                         final String[] returnAttributes) {
        val sr = new SearchRequest(baseDn, filter);
        sr.setBinaryAttributes(binaryAttributes);
        sr.setReturnAttributes(returnAttributes);
        sr.setSearchScope(SearchScope.SUBTREE);
        return sr;
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
    public static SearchRequest newLdaptiveSearchRequest(final String baseDn,
                                                         final String filterQuery,
                                                         final List<String> params,
                                                         final String[] returnAttributes) {
        val request = new SearchRequest();
        request.setBaseDn(baseDn);
        request.setFilter(newLdaptiveSearchFilter(filterQuery, params));
        request.setReturnAttributes(returnAttributes);
        request.setSearchScope(SearchScope.SUBTREE);
        return request;
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
                                                         final FilterTemplate filter) {
        return newLdaptiveSearchRequest(baseDn, filter, ReturnAttributes.ALL_USER.value(), ReturnAttributes.ALL_USER.value());
    }

    /**
     * Constructs a new search filter.
     *
     * @param filterQuery the query filter
     * @return Search filter with parameters applied.
     */
    public static FilterTemplate newLdaptiveSearchFilter(final String filterQuery) {
        return newLdaptiveSearchFilter(filterQuery, new ArrayList<>());
    }

    /**
     * Constructs a new search filter.
     *
     * @param filterQuery the query filter
     * @param params      the username
     * @return Search filter with parameters applied.
     */
    public static FilterTemplate newLdaptiveSearchFilter(final String filterQuery, final List<String> params) {
        return newLdaptiveSearchFilter(filterQuery, LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, params);
    }

    /**
     * Constructs a new search filter.
     *
     * @param filterQuery the query filter
     * @param paramName   the param name
     * @param params      the username
     * @return Search filter with parameters applied.
     */
    public static FilterTemplate newLdaptiveSearchFilter(final String filterQuery, final String paramName, final List<String> params) {
        return newLdaptiveSearchFilter(filterQuery, List.of(paramName), params);
    }

    /**
     * New ldaptive search filter search filter.
     *
     * @param filterQuery the filter query
     * @param paramName   the param name
     * @param values      the params
     * @return the search filter
     */
    public static FilterTemplate newLdaptiveSearchFilter(final String filterQuery,
                                                         final List<String> paramName,
                                                         final List<String> values) {
        val filter = new FilterTemplate();
        if (ResourceUtils.doesResourceExist(filterQuery)) {
            ApplicationContextProvider.getScriptResourceCacheManager()
                .ifPresentOrElse(cacheMgr -> FunctionUtils.doUnchecked(__ -> {
                    val cacheKey = cacheMgr.computeKey(filterQuery);
                    var script = (ExecutableCompiledScript) null;
                    if (cacheMgr.containsKey(cacheKey)) {
                        script = cacheMgr.get(cacheKey);
                        LOGGER.trace("Located cached groovy script [{}] for key [{}]", script, cacheKey);
                    } else {
                        val resource = Unchecked.supplier(() -> ResourceUtils.getRawResourceFrom(filterQuery)).get();
                        LOGGER.trace("Groovy script [{}] for key [{}] is not cached", resource, cacheKey);
                        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                        script = scriptFactory.fromResource(resource);
                        cacheMgr.put(cacheKey, script);
                        LOGGER.trace("Cached groovy script [{}] for key [{}]", script, cacheKey);
                    }
                    if (script != null) {
                        val parameters = IntStream.range(0, values.size())
                            .boxed()
                            .collect(Collectors.toMap(paramName::get, values::get, (a, b) -> b, LinkedHashMap::new));
                        val args = CollectionUtils.<String, Object>wrap("filter", filter,
                            "parameters", parameters,
                            "applicationContext", ApplicationContextProvider.getApplicationContext(),
                            "logger", LOGGER);
                        script.setBinding(args);
                        script.execute(args.values().toArray(), FilterTemplate.class);
                    }
                }),
                    () -> {
                        throw new RuntimeException("Script cache manager unavailable to handle LDAP filter");
                    });
        } else {
            filter.setFilter(filterQuery);
            if (values != null) {
                IntStream.range(0, values.size()).forEach(i -> {
                    val value = values.get(i);
                    if (filter.getFilter().contains("{" + i + '}')) {
                        filter.setParameter(i, value);
                    }
                    val name = paramName.get(i);
                    if (filter.getFilter().contains('{' + name + '}')) {
                        filter.setParameter(name, value);
                    }
                });
            }
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
    public static SearchOperation newLdaptiveSearchOperation(final String baseDn, final String filterQuery, final List<String> params) {
        return newLdaptiveSearchOperation(baseDn, filterQuery, params, List.of(ReturnAttributes.ALL.value()));
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
    public static SearchOperation newLdaptiveSearchOperation(final String baseDn, final String filterQuery,
                                                             final List<String> params,
                                                             final List<String> returnAttributes) {
        val operation = new SearchOperation();
        val request = newLdaptiveSearchRequest(baseDn, filterQuery, params, returnAttributes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        operation.setRequest(request);
        operation.setTemplate(newLdaptiveSearchFilter(filterQuery, params));
        return operation;
    }

    /**
     * New search executor search executor.
     *
     * @param baseDn      the base dn
     * @param filterQuery the filter query
     * @return the search executor
     */
    public static SearchOperation newLdaptiveSearchOperation(final String baseDn, final String filterQuery) {
        return newLdaptiveSearchOperation(baseDn, filterQuery, new ArrayList<>());
    }

    /**
     * New ldap authenticator.
     *
     * @param props the ldap settings.
     * @return the authenticator
     */
    public static Authenticator newLdaptiveAuthenticator(final AbstractLdapAuthenticationProperties props) {
        switch (props.getType()) {
            case AD -> {
                LOGGER.debug("Creating active directory authenticator for [{}]", props.getLdapUrl());
                return getActiveDirectoryAuthenticator(props);
            }
            case DIRECT -> {
                LOGGER.debug("Creating direct-bind authenticator for [{}]", props.getLdapUrl());
                return getDirectBindAuthenticator(props);
            }
            case AUTHENTICATED -> {
                LOGGER.debug("Creating authenticated authenticator for [{}]", props.getLdapUrl());
                return getAuthenticatedOrAnonSearchAuthenticator(props);
            }
            default -> {
                LOGGER.debug("Creating anonymous authenticator for [{}]", props.getLdapUrl());
                return getAuthenticatedOrAnonSearchAuthenticator(props);
            }
        }
    }

    /**
     * New pooled connection factory pooled connection factory.
     *
     * @param props the ldap properties
     * @return the pooled connection factory
     */
    public static PooledConnectionFactory newLdaptivePooledConnectionFactory(final AbstractLdapProperties props) {
        val connectionConfig = newLdaptiveConnectionConfig(props);

        LOGGER.debug("Creating LDAP connection pool configuration for [{}]", props.getLdapUrl());
        val pooledCf = new PooledConnectionFactory(connectionConfig);
        pooledCf.setMinPoolSize(props.getMinPoolSize());
        pooledCf.setMaxPoolSize(props.getMaxPoolSize());
        pooledCf.setValidateOnCheckOut(props.isValidateOnCheckout());
        pooledCf.setValidatePeriodically(props.isValidatePeriodically());
        pooledCf.setBlockWaitTime(Beans.newDuration(props.getBlockWaitTime()));

        val strategy = new IdlePruneStrategy();
        strategy.setIdleTime(Beans.newDuration(props.getIdleTime()));
        strategy.setPrunePeriod(Beans.newDuration(props.getPrunePeriod()));

        pooledCf.setPruneStrategy(strategy);

        val validator = props.getValidator();
        switch (validator.getType().trim().toLowerCase(Locale.ENGLISH)) {
            case "compare" -> {
                val compareRequest = new CompareRequest(
                    validator.getDn(),
                    validator.getAttributeName(),
                    validator.getAttributeValue());
                val compareValidator = new CompareConnectionValidator(compareRequest);
                compareValidator.setValidatePeriod(Beans.newDuration(props.getValidatePeriod()));
                compareValidator.setValidateTimeout(Beans.newDuration(props.getValidateTimeout()));
                pooledCf.setValidator(compareValidator);
            }
            case "none" -> LOGGER.debug("No validator is configured for the LDAP connection pool of [{}]", props.getLdapUrl());
            case "search" -> {
                val searchRequest = new SearchRequest();
                searchRequest.setBaseDn(validator.getBaseDn());
                searchRequest.setFilter(validator.getSearchFilter());
                searchRequest.setReturnAttributes(ReturnAttributes.NONE.value());
                searchRequest.setSearchScope(SearchScope.valueOf(validator.getScope()));
                searchRequest.setSizeLimit(1);
                val searchValidator = new SearchConnectionValidator(searchRequest);
                searchValidator.setValidatePeriod(Beans.newDuration(props.getValidatePeriod()));
                searchValidator.setValidateTimeout(Beans.newDuration(props.getValidateTimeout()));
                pooledCf.setValidator(searchValidator);
            }
        }

        pooledCf.setFailFastInitialize(props.isFailFast());

        if (StringUtils.isNotBlank(props.getPoolPassivator())) {
            val pass = AbstractLdapProperties.LdapConnectionPoolPassivator.valueOf(props.getPoolPassivator().toUpperCase(Locale.ENGLISH));
            if (pass == AbstractLdapProperties.LdapConnectionPoolPassivator.BIND) {
                if (StringUtils.isNotBlank(props.getBindDn()) && StringUtils.isNoneBlank(props.getBindCredential())) {
                    val bindRequest = new SimpleBindRequest(props.getBindDn(), props.getBindCredential());
                    pooledCf.setPassivator(new BindConnectionPassivator(bindRequest));
                    LOGGER.debug("Created [{}] passivator for [{}]", props.getPoolPassivator(), props.getLdapUrl());
                } else if (LOGGER.isInfoEnabled()) {
                    val values = Arrays.stream(AbstractLdapProperties.LdapConnectionPoolPassivator.values())
                        .filter(v -> v != AbstractLdapProperties.LdapConnectionPoolPassivator.BIND)
                        .collect(Collectors.toList());
                    LOGGER.info("[{}] pool passivator could not be created for [{}] given bind credentials are not specified. "
                            + "If you are dealing with LDAP in such a way that does not require bind credentials, you may need to "
                            + "set the pool passivator setting to one of [{}]",
                        props.getPoolPassivator(), props.getLdapUrl(), values);
                }
            }
        }
        LOGGER.debug("Initializing ldap connection pool for [{}] and bindDn [{}]", props.getLdapUrl(), props.getBindDn());
        pooledCf.initialize();
        return pooledCf;
    }

    /**
     * New connection config connection config.
     *
     * @param properties the ldap properties
     * @return the connection config
     */
    public static ConnectionConfig newLdaptiveConnectionConfig(final AbstractLdapProperties properties) {
        if (StringUtils.isBlank(properties.getLdapUrl())) {
            throw new IllegalArgumentException("LDAP url cannot be empty/blank");
        }

        LOGGER.debug("Creating LDAP connection configuration for [{}]", properties.getLdapUrl());
        val connectionConfig = new ConnectionConfig();

        val urls = properties.getLdapUrl().contains(" ")
            ? properties.getLdapUrl()
            : String.join(" ", properties.getLdapUrl().split(","));
        LOGGER.debug("Transformed LDAP urls from [{}] to [{}]", properties.getLdapUrl(), urls);
        connectionConfig.setLdapUrl(urls);

        connectionConfig.setUseStartTLS(properties.isUseStartTls());
        connectionConfig.setConnectTimeout(Beans.newDuration(properties.getConnectTimeout()));
        connectionConfig.setResponseTimeout(Beans.newDuration(properties.getResponseTimeout()));

        if (StringUtils.isNotBlank(properties.getConnectionStrategy())) {
            val strategy = AbstractLdapProperties.LdapConnectionStrategy.valueOf(properties.getConnectionStrategy());
            switch (strategy) {
                case RANDOM -> connectionConfig.setConnectionStrategy(new RandomConnectionStrategy());
                case DNS_SRV -> connectionConfig.setConnectionStrategy(new DnsSrvConnectionStrategy());
                case ROUND_ROBIN -> connectionConfig.setConnectionStrategy(new RoundRobinConnectionStrategy());
                case ACTIVE_PASSIVE -> connectionConfig.setConnectionStrategy(new ActivePassiveConnectionStrategy());
            }
        }

        if (properties.getTrustCertificates() != null) {
            LOGGER.debug("Creating LDAP SSL configuration via trust certificates [{}]", properties.getTrustCertificates());
            val cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(properties.getTrustCertificates());
            connectionConfig.setSslConfig(new SslConfig(cfg));
        } else if (properties.getTrustStore() != null || properties.getKeystore() != null) {
            val cfg = new KeyStoreCredentialConfig();
            FunctionUtils.doIfNotNull(properties.getTrustStore(), store -> {
                val activeTrustStore = SpringExpressionLanguageValueResolver.getInstance().resolve(store);
                LOGGER.trace("Creating LDAP SSL configuration with truststore [{}]", activeTrustStore);
                cfg.setTrustStore(activeTrustStore);
                cfg.setTrustStoreType(properties.getTrustStoreType());
                val password = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getTrustStorePassword());
                cfg.setTrustStorePassword(password);
            });
            FunctionUtils.doIfNotNull(properties.getKeystore(), store -> {
                val activeStore = SpringExpressionLanguageValueResolver.getInstance().resolve(store);
                LOGGER.trace("Creating LDAP SSL configuration via keystore [{}]", activeStore);
                cfg.setKeyStore(activeStore);
                cfg.setKeyStoreType(properties.getKeystoreType());
                val password = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getKeystorePassword());
                cfg.setKeyStorePassword(password);
            });
            connectionConfig.setSslConfig(new SslConfig(cfg));
        } else {
            LOGGER.debug("Creating LDAP SSL configuration via the native JVM truststore");
            connectionConfig.setSslConfig(new SslConfig());
        }

        val sslConfig = connectionConfig.getSslConfig();
        if (sslConfig != null) {
            switch (properties.getHostnameVerifier()) {
                case ANY -> sslConfig.setHostnameVerifier(new AllowAnyHostnameVerifier());
                case DEFAULT -> sslConfig.setHostnameVerifier(new DefaultHostnameVerifier());
            }

            if (StringUtils.isNotBlank(properties.getTrustManager())) {
                val manager = properties.getTrustManager().trim().toUpperCase(Locale.ENGLISH);
                switch (AbstractLdapProperties.LdapTrustManagerOptions.valueOf(manager)) {
                    case ANY -> {
                        sslConfig.setCredentialConfig(null);
                        sslConfig.setTrustManagers(new AllowAnyTrustManager());
                    }
                    case DEFAULT -> sslConfig.setTrustManagers(new DefaultTrustManager());
                }
            }
        }

        if (StringUtils.isNotBlank(properties.getSaslMechanism())) {
            LOGGER.debug("Creating LDAP SASL mechanism via [{}]", properties.getSaslMechanism());

            val initializer = new BindConnectionInitializer();
            val saslConfig = getSaslConfigFrom(properties);

            FunctionUtils.doIfNotBlank(properties.getSaslAuthorizationId(), __ -> saslConfig.setAuthorizationId(properties.getSaslAuthorizationId()));
            saslConfig.setMutualAuthentication(properties.getSaslMutualAuth());
            if (StringUtils.isNotBlank(properties.getSaslQualityOfProtection())) {
                saslConfig.setQualityOfProtection(QualityOfProtection.valueOf(properties.getSaslQualityOfProtection()));
            }
            if (StringUtils.isNotBlank(properties.getSaslSecurityStrength())) {
                saslConfig.setSecurityStrength(SecurityStrength.valueOf(properties.getSaslSecurityStrength()));
            }
            if (StringUtils.isNotBlank(properties.getBindDn())) {
                initializer.setBindDn(properties.getBindDn());
                if (StringUtils.isNotBlank(properties.getBindCredential())) {
                    initializer.setBindCredential(new Credential(properties.getBindCredential()));
                }
            }
            initializer.setBindSaslConfig(saslConfig);
            connectionConfig.setConnectionInitializers(initializer);
        } else if (StringUtils.equals(properties.getBindCredential(), "*") && StringUtils.equals(properties.getBindDn(), "*")) {
            LOGGER.debug("Creating LDAP fast-bind connection initializer");
            connectionConfig.setConnectionInitializers(new FastBindConnectionInitializer());
        } else if (StringUtils.isNotBlank(properties.getBindDn()) && StringUtils.isNotBlank(properties.getBindCredential())) {
            LOGGER.debug("Creating LDAP bind connection initializer via [{}]", properties.getBindDn());
            connectionConfig.setConnectionInitializers(new BindConnectionInitializer(properties.getBindDn(), new Credential(properties.getBindCredential())));
        }
        return connectionConfig;
    }

    /**
     * Returns a pooled connection factory or default connection factory based on {@link AbstractLdapProperties#isDisablePooling()}.
     *
     * @param properties ldap properties
     * @return the connection factory
     */
    public static ConnectionFactory newLdaptiveConnectionFactory(final AbstractLdapProperties properties) {
        return properties.isDisablePooling() ? newLdaptiveDefaultConnectionFactory(properties) : newLdaptivePooledConnectionFactory(properties);
    }

    /**
     * New dn resolver entry resolver.
     * Creates the necessary search entry resolver.
     *
     * @param properties the ldap settings
     * @param factory    the factory
     * @return the entry resolver
     */
    public static EntryResolver newLdaptiveSearchEntryResolver(final AbstractLdapAuthenticationProperties properties,
                                                               final ConnectionFactory factory) {

        var resolvers = Arrays.stream(StringUtils.split(properties.getBaseDn(), BASE_DN_DELIMITER))
            .map(baseDn -> {
                val entryResolver = new SearchEntryResolver();
                entryResolver.setBaseDn(baseDn.trim());
                entryResolver.setUserFilter(properties.getSearchFilter());
                entryResolver.setSubtreeSearch(properties.isSubtreeSearch());
                entryResolver.setConnectionFactory(factory);
                entryResolver.setAllowMultipleEntries(properties.isAllowMultipleEntries());
                entryResolver.setBinaryAttributes(properties.getBinaryAttributes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

                if (StringUtils.isNotBlank(properties.getDerefAliases())) {
                    entryResolver.setDerefAliases(DerefAliases.valueOf(properties.getDerefAliases()));
                }

                val entryHandlers = newLdaptiveEntryHandlers(properties.getSearchEntryHandlers());
                val searchResultHandlers = newLdaptiveSearchResultHandlers(properties.getSearchEntryHandlers());
                if (!entryHandlers.isEmpty()) {
                    LOGGER.debug("Search entry handlers defined for the entry resolver of [{}] are [{}]", properties.getLdapUrl(), entryHandlers);
                    entryResolver.setEntryHandlers(entryHandlers.toArray(LdapEntryHandler[]::new));
                }
                if (!searchResultHandlers.isEmpty()) {
                    LOGGER.debug("Search entry handlers defined for the entry resolver of [{}] are [{}]", properties.getLdapUrl(), searchResultHandlers);
                    entryResolver.setSearchResultHandlers(searchResultHandlers.toArray(SearchResultHandler[]::new));
                }
                if (properties.isFollowReferrals()) {
                    entryResolver.setSearchResultHandlers(new FollowSearchReferralHandler());
                }
                return entryResolver;
            })
            .collect(Collectors.toList());
        return new ChainingLdapEntryResolver(resolvers);
    }

    /**
     * New list of ldap entry handlers derived from the supplied properties.
     *
     * @param properties to inspect
     * @return the list of entry handlers
     */
    @SuppressWarnings("MissingCasesInEnumSwitch")
    public static List<LdapEntryHandler> newLdaptiveEntryHandlers(final List<LdapSearchEntryHandlersProperties> properties) {
        val entryHandlers = new ArrayList<LdapEntryHandler>();
        properties.forEach(prop -> {
            switch (prop.getType()) {
                case ACTIVE_DIRECTORY -> {
                    val handler = new ActiveDirectoryLdapEntryHandler();
                    entryHandlers.add(handler);
                }
                case CASE_CHANGE -> {
                    val entryHandler = new CaseChangeEntryHandler();
                    val caseChange = prop.getCaseChange();
                    entryHandler.setAttributeNameCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(caseChange.getAttributeNameCaseChange()));
                    entryHandler.setAttributeNames(caseChange.getAttributeNames().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    entryHandler.setAttributeValueCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(caseChange.getAttributeValueCaseChange()));
                    entryHandler.setDnCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(caseChange.getDnCaseChange()));
                    entryHandlers.add(entryHandler);
                }
                case DN_ATTRIBUTE_ENTRY -> {
                    val entryHandler = new DnAttributeEntryHandler();
                    val dnAttribute = prop.getDnAttribute();
                    entryHandler.setAddIfExists(dnAttribute.isAddIfExists());
                    entryHandler.setDnAttributeName(dnAttribute.getDnAttributeName());
                    entryHandlers.add(entryHandler);
                }
                case MERGE -> {
                    val entryHandler = new MergeAttributeEntryHandler();
                    val mergeAttribute = prop.getMergeAttribute();
                    entryHandler.setAttributeNames(mergeAttribute.getAttributeNames().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    entryHandler.setMergeAttributeName(mergeAttribute.getMergeAttributeName());
                    entryHandlers.add(entryHandler);
                }
                case OBJECT_GUID -> entryHandlers.add(new ObjectGuidHandler());
                case OBJECT_SID -> entryHandlers.add(new ObjectSidHandler());
            }
        });
        return entryHandlers;
    }

    /**
     * New list of ldap search result handlers derived from the supplied properties.
     *
     * @param properties to inspect
     * @return the list of search result handlers
     */
    public static List<SearchResultHandler> newLdaptiveSearchResultHandlers(final List<LdapSearchEntryHandlersProperties> properties) {
        val searchResultHandlers = new ArrayList<SearchResultHandler>();
        properties.forEach(prop -> {
            switch (prop.getType()) {
                case FOLLOW_SEARCH_REFERRAL -> searchResultHandlers.add(new FollowSearchReferralHandler(prop.getSearchReferral().getLimit()));
                case FOLLOW_SEARCH_RESULT_REFERENCE -> searchResultHandlers.add(new FollowSearchResultReferenceHandler(prop.getSearchResult().getLimit()));
                case PRIMARY_GROUP -> {
                    val handler = new PrimaryGroupIdHandler();
                    val primaryGroupId = prop.getPrimaryGroupId();
                    handler.setBaseDn(primaryGroupId.getBaseDn());
                    handler.setGroupFilter(primaryGroupId.getGroupFilter());
                    searchResultHandlers.add(handler);
                }
                case RANGE_ENTRY -> searchResultHandlers.add(new RangeEntryHandler());
                case RECURSIVE_ENTRY -> {
                    val recursive = prop.getRecursive();
                    searchResultHandlers.add(
                        new RecursiveResultHandler(recursive.getSearchAttribute(),
                            recursive.getMergeAttributes().toArray(ArrayUtils.EMPTY_STRING_ARRAY)));
                }
                default -> searchResultHandlers.add(new MergeResultHandler());
            }
        });
        return searchResultHandlers;
    }

    /**
     * Gets authenticated authenticator.
     *
     * @param properties the lDAP properties
     * @return the authenticated or anon search authenticator
     */
    public static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final AbstractLdapAuthenticationProperties properties) {
        if (StringUtils.isBlank(properties.getBaseDn())) {
            throw new IllegalArgumentException("Base dn cannot be empty/blank for authenticated/anonymous authentication");
        }
        if (StringUtils.isBlank(properties.getSearchFilter())) {
            throw new IllegalArgumentException("User filter cannot be empty/blank for authenticated/anonymous authentication");
        }
        val connectionFactoryForSearch = newLdaptiveConnectionFactory(properties);
        val resolver = buildAggregateDnResolver(properties, connectionFactoryForSearch);

        val auth = StringUtils.isBlank(properties.getPrincipalAttributePassword())
            ? new Authenticator(resolver, getBindAuthenticationHandler(newLdaptiveConnectionFactory(properties)))
            : new Authenticator(resolver, getCompareAuthenticationHandler(properties, newLdaptiveConnectionFactory(properties)));

        if (properties.isEnhanceWithEntryResolver()) {
            auth.setEntryResolver(newLdaptiveSearchEntryResolver(properties, newLdaptiveConnectionFactory(properties)));
        }
        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final AbstractLdapAuthenticationProperties properties) {
        if (StringUtils.isBlank(properties.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for direct bind authentication");
        }
        return getAuthenticatorViaDnFormat(properties, null);
    }

    private static Authenticator getActiveDirectoryAuthenticator(final AbstractLdapAuthenticationProperties properties) {
        if (StringUtils.isBlank(properties.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for active directory authentication");
        }
        return getAuthenticatorViaDnFormat(properties, newLdaptiveConnectionFactory(properties));
    }

    private static Authenticator getAuthenticatorViaDnFormat(final AbstractLdapAuthenticationProperties properties,
                                                             final ConnectionFactory factory) {
        val resolver = new FormatDnResolver(properties.getDnFormat());
        val authenticator = new Authenticator(resolver, getBindAuthenticationHandler(newLdaptiveConnectionFactory(properties)));

        if (properties.isEnhanceWithEntryResolver()) {
            authenticator.setEntryResolver(newLdaptiveSearchEntryResolver(properties, factory));
        }
        return authenticator;
    }

    private static AuthenticationHandler getBindAuthenticationHandler(final ConnectionFactory factory) {
        return new SimpleBindAuthenticationHandler(factory);
    }

    private static AuthenticationHandler getCompareAuthenticationHandler(final AbstractLdapAuthenticationProperties properties,
                                                                         final ConnectionFactory factory) {
        val handler = new CompareAuthenticationHandler(factory);
        handler.setPasswordAttribute(properties.getPrincipalAttributePassword());
        return handler;
    }

    private static SaslConfig getSaslConfigFrom(final AbstractLdapProperties properties) {

        if (Mechanism.valueOf(properties.getSaslMechanism()) == Mechanism.DIGEST_MD5) {
            val sc = new SaslConfig();
            sc.setMechanism(Mechanism.DIGEST_MD5);
            sc.setRealm(properties.getSaslRealm());
            return sc;
        }
        if (Mechanism.valueOf(properties.getSaslMechanism()) == Mechanism.CRAM_MD5) {
            val sc = new SaslConfig();
            sc.setMechanism(Mechanism.CRAM_MD5);
            return sc;
        }
        if (Mechanism.valueOf(properties.getSaslMechanism()) == Mechanism.EXTERNAL) {
            val sc = new SaslConfig();
            sc.setMechanism(Mechanism.EXTERNAL);
            return sc;
        }
        val sc = new SaslConfig();
        sc.setMechanism(Mechanism.GSSAPI);
        sc.setRealm(properties.getSaslRealm());
        return sc;
    }

    /**
     * New default connection factory.
     *
     * @param properties LDAP properties
     * @return the connection factory
     */
    public static ConnectionFactory newLdaptiveDefaultConnectionFactory(final AbstractLdapProperties properties) {
        LOGGER.debug("Creating LDAP connection factory for [{}]", properties.getLdapUrl());
        val connectionConfig = newLdaptiveConnectionConfig(properties);
        return new DefaultConnectionFactory(connectionConfig);
    }

    /**
     * Build aggregate dn resolver dn resolver.
     *
     * @param properties        the LDAP properties
     * @param connectionFactory the connection factory
     * @return the dn resolver
     */
    public static DnResolver buildAggregateDnResolver(final AbstractLdapAuthenticationProperties properties,
                                                      final ConnectionFactory connectionFactory) {
        var resolvers = Arrays.stream(StringUtils.split(properties.getBaseDn(), BASE_DN_DELIMITER))
            .map(baseDn -> {
                val resolver = new SearchDnResolver();
                resolver.setBaseDn(baseDn);
                resolver.setSubtreeSearch(properties.isSubtreeSearch());
                resolver.setAllowMultipleDns(properties.isAllowMultipleDns());
                resolver.setConnectionFactory(connectionFactory);
                resolver.setUserFilter(properties.getSearchFilter());
                resolver.setResolveFromAttribute(properties.getResolveFromAttribute());
                if (properties.isFollowReferrals()) {
                    resolver.setSearchResultHandlers(new FollowSearchReferralHandler());
                }
                if (StringUtils.isNotBlank(properties.getDerefAliases())) {
                    resolver.setDerefAliases(DerefAliases.valueOf(properties.getDerefAliases()));
                }
                return resolver;
            })
            .collect(Collectors.toList());
        return new ChainingLdapDnResolver(resolvers);
    }

    /**
     * Create ldap password policy handling strategy.
     *
     * @param properties         the lDAP properties
     * @param applicationContext the application context
     * @return the authentication password policy handling strategy
     */
    public static AuthenticationPasswordPolicyHandlingStrategy<AuthenticationResponse, PasswordPolicyContext>
        createLdapPasswordPolicyHandlingStrategy(final LdapAuthenticationProperties properties, final ApplicationContext applicationContext) {
        if (properties.getPasswordPolicy().getStrategy() == PasswordPolicyHandlingOptions.REJECT_RESULT_CODE) {
            LOGGER.debug("Created LDAP password policy handling strategy based on blocked authentication result codes");
            return new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        }

        val location = properties.getPasswordPolicy().getGroovy().getLocation();
        if (properties.getPasswordPolicy().getStrategy() == PasswordPolicyHandlingOptions.GROOVY
            && location != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
            LOGGER.debug("Created LDAP password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy(location, applicationContext);
        }

        LOGGER.debug("Created default LDAP password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy();
    }


    /**
     * Create ldap password policy configuration.
     *
     * @param passwordPolicy the password policy
     * @param authenticator  the authenticator
     * @param attributes     the attributes
     * @return the password policy context
     */
    public static PasswordPolicyContext createLdapPasswordPolicyConfiguration(
        final LdapPasswordPolicyProperties passwordPolicy,
        final Authenticator authenticator,
        final Multimap<String, Object> attributes) {
        val cfg = new PasswordPolicyContext(passwordPolicy);
        val requestHandlers = new HashSet<AuthenticationRequestHandler>();
        val responseHandlers = new HashSet<AuthenticationResponseHandler>();

        val customPolicyClass = passwordPolicy.getCustomPolicyClass();
        if (StringUtils.isNotBlank(customPolicyClass)) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password policy handler [{}]", customPolicyClass);
                val clazz = (Class<AuthenticationResponseHandler>) Class.forName(customPolicyClass);
                responseHandlers.add(clazz.getDeclaredConstructor().newInstance());
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, "Unable to construct an instance of the password policy handler", e);
            }
        }
        LOGGER.debug("Password policy authentication response handler is set to accommodate directory type: [{}]", passwordPolicy.getType());
        switch (passwordPolicy.getType()) {
            case AD -> {
                val warningPeriod = Period.ofDays(cfg.getPasswordWarningNumberOfDays());
                val handler = FunctionUtils.doIf(passwordPolicy.getPasswordExpirationNumberOfDays() > 0,
                        () -> {
                            val expirationPeriod = Period.ofDays(passwordPolicy.getPasswordExpirationNumberOfDays());
                            LOGGER.debug("Creating active directory authentication response handler with expiration period [{}] and warning period [{}]", expirationPeriod, warningPeriod);
                            return new ActiveDirectoryAuthenticationResponseHandler(expirationPeriod, warningPeriod);
                        },
                        () -> {
                            LOGGER.debug("Creating active directory authentication response handler with warning period [{}]", warningPeriod);
                            return new ActiveDirectoryAuthenticationResponseHandler(warningPeriod);
                        })
                    .get();
                responseHandlers.add(handler);
                Arrays.stream(ActiveDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(attr -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", attr);
                    attributes.put(attr, attr);
                });
            }
            case FreeIPA -> {
                Arrays.stream(FreeIPAAuthenticationResponseHandler.ATTRIBUTES).forEach(attr -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", attr);
                    attributes.put(attr, attr);
                });
                responseHandlers.add(new FreeIPAAuthenticationResponseHandler(
                    Period.ofDays(cfg.getPasswordWarningNumberOfDays()), cfg.getLoginFailures()));
            }
            case EDirectory -> {
                Arrays.stream(EDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(attr -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", attr);
                    attributes.put(attr, attr);
                });
                responseHandlers.add(new EDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
            }
            default -> {
                requestHandlers.add(new PasswordPolicyAuthenticationRequestHandler());
                responseHandlers.add(new PasswordPolicyAuthenticationResponseHandler());
                responseHandlers.add(new PasswordExpirationAuthenticationResponseHandler());
            }
        }
        if (!requestHandlers.isEmpty()) {
            authenticator.setRequestHandlers(requestHandlers.toArray(AuthenticationRequestHandler[]::new));
        }
        authenticator.setResponseHandlers(responseHandlers.toArray(AuthenticationResponseHandler[]::new));

        LOGGER.debug("LDAP authentication response handlers configured are: [{}]", responseHandlers);

        if (!passwordPolicy.isAccountStateHandlingEnabled()) {
            cfg.setAccountStateHandler((response, configuration) -> new ArrayList<>());
            LOGGER.trace("Handling LDAP account states is disabled via CAS configuration");
        } else if (StringUtils.isNotBlank(passwordPolicy.getWarningAttributeName()) && StringUtils.isNotBlank(passwordPolicy.getWarningAttributeValue())) {
            val accountHandler = new OptionalWarningLdapAccountStateHandler();
            accountHandler.setDisplayWarningOnMatch(passwordPolicy.isDisplayWarningOnMatch());
            accountHandler.setWarnAttributeName(passwordPolicy.getWarningAttributeName());
            accountHandler.setWarningAttributeValue(passwordPolicy.getWarningAttributeValue());
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring an warning account state handler for LDAP authentication for warning attribute [{}] and value [{}]",
                passwordPolicy.getWarningAttributeName(), passwordPolicy.getWarningAttributeValue());
        } else {
            val accountHandler = new DefaultLdapAccountStateHandler();
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring the default account state handler for LDAP authentication");
        }
        return cfg;
    }

    /**
     * Create ldap authentication handler.
     *
     * @param props              the ldap authentication properties
     * @param applicationContext the application context
     * @param servicesManager    the services manager
     * @param principalFactory   the principal factory
     * @return the ldap authentication handler
     */
    public static LdapAuthenticationHandler createLdapAuthenticationHandler(final LdapAuthenticationProperties props,
                                                                            final ApplicationContext applicationContext,
                                                                            final ServicesManager servicesManager,
                                                                            final PrincipalFactory principalFactory) {
        val multiMapAttributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(props.getPrincipalAttributeList());
        LOGGER.debug("Created and mapped principal attributes [{}] for [{}]...", multiMapAttributes, props.getLdapUrl());

        LOGGER.debug("Creating LDAP authenticator for [{}] and baseDn [{}]", props.getLdapUrl(), props.getBaseDn());
        val authenticator = LdapUtils.newLdaptiveAuthenticator(props);
        LOGGER.debug("Ldap authenticator configured with return attributes [{}] for [{}] and baseDn [{}]",
            multiMapAttributes.keySet(), props.getLdapUrl(), props.getBaseDn());

        LOGGER.debug("Creating LDAP password policy handling strategy for [{}]", props.getLdapUrl());
        val strategy = createLdapPasswordPolicyHandlingStrategy(props, applicationContext);

        LOGGER.debug("Creating LDAP authentication handler for [{}]", props.getLdapUrl());
        val handler = new LdapAuthenticationHandler(props.getName(),
            servicesManager, principalFactory,
            props.getOrder(), authenticator, strategy);
        handler.setCollectDnAttribute(props.isCollectDnAttribute());

        if (!props.getAdditionalAttributes().isEmpty()) {
            val additional = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(props.getAdditionalAttributes());
            multiMapAttributes.putAll(additional);
        }

        FunctionUtils.doIfNotBlank(props.getPrincipalDnAttributeName(),
            __ -> handler.setPrincipalDnAttributeName(props.getPrincipalDnAttributeName()));
        handler.setAllowMultiplePrincipalAttributeValues(props.isAllowMultiplePrincipalAttributeValues());
        handler.setAllowMissingPrincipalAttributeValue(props.isAllowMissingPrincipalAttributeValue());
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(props.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(props.getPrincipalTransformation()));

        if (StringUtils.isNotBlank(props.getCredentialCriteria())) {
            LOGGER.trace("Ldap authentication for [{}] is filtering credentials by [{}]", props.getLdapUrl(), props.getCredentialCriteria());
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(props.getCredentialCriteria()));
        }

        if (StringUtils.isBlank(props.getPrincipalAttributeId())) {
            LOGGER.trace("No principal id attribute is found for LDAP authentication via [{}]", props.getLdapUrl());
        } else {
            handler.setPrincipalIdAttribute(props.getPrincipalAttributeId());
            LOGGER.trace("Using principal id attribute [{}] for LDAP authentication via [{}]",
                props.getPrincipalAttributeId(), props.getLdapUrl());
        }

        val passwordPolicy = props.getPasswordPolicy();
        if (passwordPolicy.isEnabled()) {
            LOGGER.trace("Password policy is enabled for [{}]. Constructing password policy configuration", props.getLdapUrl());
            val cfg = createLdapPasswordPolicyConfiguration(passwordPolicy, authenticator, multiMapAttributes);
            handler.setPasswordPolicyConfiguration(cfg);
        }

        val attributes = CollectionUtils.wrap(multiMapAttributes);
        handler.setPrincipalAttributeMap(attributes);

        LOGGER.debug("Initializing LDAP authentication handler for [{}]", props.getLdapUrl());
        handler.initialize();
        return handler;
    }

    @SuppressWarnings("UnusedVariable")
    private record ChainingLdapDnResolver(List<? extends DnResolver> resolvers) implements DnResolver {
        @Override
        public String resolve(final User user) {
            return resolvers
                .stream()
                .map(resolver -> FunctionUtils.doAndHandle(
                        () -> resolver.resolve(user),
                        throwable -> {
                            LoggingUtils.warn(LOGGER, throwable);
                            return null;
                        })
                    .get())
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(new AccountNotFoundException("Unable to resolve user dn for " + user.getIdentifier())));
        }
    }

    @SuppressWarnings("UnusedVariable")
    private record ChainingLdapEntryResolver(List<? extends EntryResolver> resolvers) implements EntryResolver {
        @Override
        public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response) {
            return resolvers.stream()
                .map(resolver -> FunctionUtils.doAndHandle(
                        () -> resolver.resolve(criteria, response),
                        throwable -> {
                            LoggingUtils.warn(LOGGER, throwable);
                            return null;
                        })
                    .get())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Is ldap authentication configured?.
     *
     * @param prop the prop
     * @return true/false
     */
    public static boolean isLdapAuthenticationConfigured(final AbstractLdapAuthenticationProperties prop) {
        return prop.getType() != null && StringUtils.isNotBlank(prop.getLdapUrl());
    }
}
