package org.apereo.cas.util;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.DefaultConnectionStrategy;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.RandomConnectionStrategy;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.RoundRobinConnectionStrategy;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.ad.UnicodePwdAttribute;
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
import org.ldaptive.extended.PasswordModifyOperation;
import org.ldaptive.extended.PasswordModifyRequest;
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
import org.ldaptive.referral.DeleteReferralHandler;
import org.ldaptive.referral.ModifyReferralHandler;
import org.ldaptive.referral.SearchReferralHandler;
import org.ldaptive.sasl.CramMd5Config;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.ExternalConfig;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public final class LdapUtils {
    /**
     * Default parameter name in search filters for ldap.
     */
    public static final String LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME = "user";

    /**
     * The objectClass attribute.
     */
    public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);

    private static final String LDAP_PREFIX = "ldap";

    /**
     * Instantiates a new ldap utils.
     */
    private LdapUtils() {
        // private constructor so that no one can instantiate.
    }

    /**
     * Reads a Boolean value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @return {@code true} if the attribute's value matches (case-insensitive) {@code "true"}, otherwise false
     */
    public static Boolean getBoolean(final LdapEntry ctx, final String attribute) {
        return getBoolean(ctx, attribute, Boolean.FALSE);
    }

    /**
     * Reads a Boolean value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return {@code true} if the attribute's value matches (case-insensitive) {@code "true"}, otherwise false
     */
    public static Boolean getBoolean(final LdapEntry ctx, final String attribute, final Boolean nullValue) {
        final String v = getString(ctx, attribute, nullValue.toString());
        if (v != null) {
            return v.equalsIgnoreCase(Boolean.TRUE.toString());
        }
        return nullValue;
    }

    /**
     * Reads a Long value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @return the long value
     */
    public static Long getLong(final LdapEntry ctx, final String attribute) {
        return getLong(ctx, attribute, Long.MIN_VALUE);
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
        final String v = getString(entry, attribute, nullValue.toString());
        if (v != null && NumberUtils.isCreatable(v)) {
            return Long.valueOf(v);
        }
        return nullValue;
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
        final LdapAttribute attr = entry.getAttribute(attribute);
        if (attr == null) {
            return nullValue;
        }

        final String v;
        if (attr.isBinary()) {
            final byte[] b = attr.getBinaryValue();
            v = new String(b, StandardCharsets.UTF_8);
        } else {
            v = attr.getStringValue();
        }

        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        return nullValue;
    }

    /**
     * Execute search operation.
     *
     * @param connectionFactory the connection factory
     * @param baseDn            the base dn
     * @param filter            the filter
     * @param returnAttributes  the return attributes
     * @return the response
     * @throws LdapException the ldap exception
     */
    public static Response<SearchResult> executeSearchOperation(final ConnectionFactory connectionFactory,
                                                                final String baseDn,
                                                                final SearchFilter filter,
                                                                final String... returnAttributes) throws LdapException {
        return executeSearchOperation(connectionFactory, baseDn, filter, null, returnAttributes);
    }

    /**
     * Execute search operation.
     *
     * @param connectionFactory the connection factory
     * @param baseDn            the base dn
     * @param filter            the filter
     * @param binaryAttributes  the binary attributes
     * @param returnAttributes  the return attributes
     * @return the response
     * @throws LdapException the ldap exception
     */
    public static Response<SearchResult> executeSearchOperation(final ConnectionFactory connectionFactory,
                                                                final String baseDn,
                                                                final SearchFilter filter,
                                                                final String[] binaryAttributes,
                                                                final String[] returnAttributes) throws LdapException {
        try (Connection connection = createConnection(connectionFactory)) {
            final SearchOperation searchOperation = new SearchOperation(connection);
            final SearchRequest request = LdapUtils.newLdaptiveSearchRequest(baseDn, filter, binaryAttributes, returnAttributes);
            request.setReferralHandler(new SearchReferralHandler());
            return searchOperation.execute(request);
        }
    }

    /**
     * Execute search operation response.
     *
     * @param connectionFactory the connection factory
     * @param baseDn            the base dn
     * @param filter            the filter
     * @return the response
     * @throws LdapException the ldap exception
     */
    public static Response<SearchResult> executeSearchOperation(final ConnectionFactory connectionFactory,
                                                                final String baseDn,
                                                                final SearchFilter filter) throws LdapException {
        return executeSearchOperation(connectionFactory, baseDn, filter, ReturnAttributes.ALL_USER.value(), ReturnAttributes.ALL_USER.value());
    }

    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    public static boolean containsResultEntry(final Response<SearchResult> response) {
        final SearchResult result = response.getResult();
        return result != null && result.getEntry() != null;
    }

    /**
     * Gets connection from the factory.
     * Opens the connection if needed.
     *
     * @param connectionFactory the connection factory
     * @return the connection
     * @throws LdapException the ldap exception
     */
    public static Connection createConnection(final ConnectionFactory connectionFactory) throws LdapException {
        final Connection c = connectionFactory.getConnection();
        if (!c.isOpen()) {
            c.open();
        }
        return c;
    }

    /**
     * Execute a password modify operation.
     *
     * @param currentDn         the current dn
     * @param connectionFactory the connection factory
     * @param oldPassword       the old password
     * @param newPassword       the new password
     * @param type              the type
     * @return true /false
     */
    public static boolean executePasswordModifyOperation(final String currentDn,
                                                         final ConnectionFactory connectionFactory,
                                                         final String oldPassword,
                                                         final String newPassword,
                                                         final AbstractLdapProperties.LdapType type) {
        try (Connection modifyConnection = createConnection(connectionFactory)) {
            if (!modifyConnection.getConnectionConfig().getUseSSL()
                    && !modifyConnection.getConnectionConfig().getUseStartTLS()) {
                LOGGER.warn("Executing password modification op under a non-secure LDAP connection; "
                        + "To modify password attributes, the connection to the LDAP server SHOULD be secured and/or encrypted.");
            }
            if (type == AbstractLdapProperties.LdapType.AD) {
                LOGGER.debug("Executing password modification op for active directory based on "
                        + "[https://support.microsoft.com/en-us/kb/269190]");
                final ModifyOperation operation = new ModifyOperation(modifyConnection);
                final Response response = operation.execute(new ModifyRequest(currentDn,
                        new AttributeModification(AttributeModificationType.REPLACE, new UnicodePwdAttribute(newPassword))));
                LOGGER.debug("Result code [{}], message: [{}]", response.getResult(), response.getMessage());
                return response.getResultCode() == ResultCode.SUCCESS;
            }

            LOGGER.debug("Executing password modification op for generic LDAP");
            final PasswordModifyOperation operation = new PasswordModifyOperation(modifyConnection);
            final Response response = operation.execute(new PasswordModifyRequest(currentDn,
                    StringUtils.isNotBlank(oldPassword) ? new Credential(oldPassword) : null,
                    new Credential(newPassword)));
            LOGGER.debug("Result code [{}], message: [{}]", response.getResult(), response.getMessage());
            return response.getResultCode() == ResultCode.SUCCESS;
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Execute modify operation boolean.
     *
     * @param currentDn         the current dn
     * @param connectionFactory the connection factory
     * @param attributes        the attributes
     * @return true/false
     */
    public static boolean executeModifyOperation(final String currentDn, final ConnectionFactory connectionFactory,
                                                 final Map<String, Set<String>> attributes) {
        try (Connection modifyConnection = createConnection(connectionFactory)) {
            final ModifyOperation operation = new ModifyOperation(modifyConnection);
            final List<AttributeModification> mods = attributes.entrySet()
                    .stream()
                    .map(entry -> new AttributeModification(AttributeModificationType.REPLACE,
                            new LdapAttribute(entry.getKey(), entry.getValue().toArray(new String[]{}))))
                    .collect(Collectors.toList());
            final ModifyRequest request = new ModifyRequest(currentDn, mods.toArray(new AttributeModification[]{}));
            request.setReferralHandler(new ModifyReferralHandler());
            operation.execute(request);
            return true;
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Execute modify operation boolean.
     *
     * @param currentDn         the current dn
     * @param connectionFactory the connection factory
     * @param entry             the entry
     * @return true/false
     */
    public static boolean executeModifyOperation(final String currentDn, final ConnectionFactory connectionFactory, final LdapEntry entry) {
        final Map<String, Set<String>> attributes = entry.getAttributes().stream()
                .collect(Collectors.toMap(LdapAttribute::getName, ldapAttribute -> new HashSet<>(ldapAttribute.getStringValues())));

        return executeModifyOperation(currentDn, connectionFactory, attributes);
    }

    /**
     * Execute add operation boolean.
     *
     * @param connectionFactory the connection factory
     * @param entry             the entry
     * @return true/false
     */
    public static boolean executeAddOperation(final ConnectionFactory connectionFactory, final LdapEntry entry) {
        try (Connection connection = createConnection(connectionFactory)) {
            final AddOperation operation = new AddOperation(connection);
            operation.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            return true;
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Execute delete operation boolean.
     *
     * @param connectionFactory the connection factory
     * @param entry             the entry
     * @return true/false
     */
    public static boolean executeDeleteOperation(final ConnectionFactory connectionFactory, final LdapEntry entry) {
        try (Connection connection = createConnection(connectionFactory)) {
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(entry.getDn());
            request.setReferralHandler(new DeleteReferralHandler());
            final Response<Void> res = delete.execute(request);
            return res.getResultCode() == ResultCode.SUCCESS;
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Is ldap connection url?.
     *
     * @param r the resource
     * @return true/false
     */
    public static boolean isLdapConnectionUrl(final String r) {
        return r.toLowerCase().startsWith(LDAP_PREFIX);
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
        return newLdaptiveSearchFilter(filterQuery, new ArrayList<>(0));
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
        return newLdaptiveSearchExecutor(baseDn, filterQuery, new ArrayList<>(0));
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
        final PooledConnectionFactory connectionFactoryForSearch = newLdaptivePooledConnectionFactory(l);
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(connectionFactoryForSearch);
        resolver.setUserFilter(l.getUserFilter());
        resolver.setReferralHandler(new SearchReferralHandler());
        
        final Authenticator auth;
        if (StringUtils.isBlank(l.getPrincipalAttributePassword())) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, newLdaptivePooledConnectionFactory(l)));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(l, newLdaptivePooledConnectionFactory(l)));
        }

        if (l.isEnhanceWithEntryResolver()) {
            auth.setEntryResolver(newLdaptiveSearchEntryResolver(l, newLdaptivePooledConnectionFactory(l)));
        }
        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final AbstractLdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for direct bind authentication");
        }
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authenticator = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, newLdaptivePooledConnectionFactory(l)));

        if (l.isEnhanceWithEntryResolver()) {
            authenticator.setEntryResolver(newLdaptiveSearchEntryResolver(l, newLdaptivePooledConnectionFactory(l)));
        }
        return authenticator;
    }

    private static Authenticator getActiveDirectoryAuthenticator(final AbstractLdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for active directory authentication");
        }
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, newLdaptivePooledConnectionFactory(l)));

        if (l.isEnhanceWithEntryResolver()) {
            authn.setEntryResolver(newLdaptiveSearchEntryResolver(l, newLdaptivePooledConnectionFactory(l)));
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
        cc.setConnectTimeout(Beans.newDuration(l.getConnectTimeout()));
        cc.setResponseTimeout(Beans.newDuration(l.getResponseTimeout()));

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
        if (StringUtils.isNotBlank(l.getSaslMechanism())) {
            LOGGER.debug("Creating LDAP SASL mechanism via [{}]", l.getSaslMechanism());

            final BindConnectionInitializer bc = new BindConnectionInitializer();
            final SaslConfig sc;
            switch (Mechanism.valueOf(l.getSaslMechanism())) {
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
                default:
                    sc = new GssApiConfig();
                    ((GssApiConfig) sc).setRealm(l.getSaslRealm());
                    break;
            }
            if (StringUtils.isNotBlank(l.getSaslAuthorizationId())) {
                sc.setAuthorizationId(l.getSaslAuthorizationId());
            }
            sc.setMutualAuthentication(l.getSaslMutualAuth());
            if (StringUtils.isNotBlank(l.getSaslQualityOfProtection())) {
                sc.setQualityOfProtection(QualityOfProtection.valueOf(l.getSaslQualityOfProtection()));
            }
            if (StringUtils.isNotBlank(l.getSaslSecurityStrength())) {
                sc.setSecurityStrength(SecurityStrength.valueOf(l.getSaslSecurityStrength()));
            }
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
        pc.setValidatePeriod(Beans.newDuration(l.getValidatePeriod()));
        pc.setValidateTimeout(Beans.newDuration(l.getValidateTimeout()));
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

        cp.setBlockWaitTime(Beans.newDuration(l.getBlockWaitTime()));
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        strategy.setIdleTime(Beans.newDuration(l.getIdleTime()));
        strategy.setPrunePeriod(Beans.newDuration(l.getPrunePeriod()));

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
                searchRequest.setSearchScope(SearchScope.valueOf(l.getValidator().getScope()));
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
                    eh.setAttributeNameCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(h.getCasChange().getAttributeNameCaseChange()));
                    eh.setAttributeNames(h.getCasChange().getAttributeNames().toArray(new String[]{}));
                    eh.setAttributeValueCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(h.getCasChange().getAttributeValueCaseChange()));
                    eh.setDnCaseChange(CaseChangeEntryHandler.CaseChange.valueOf(h.getCasChange().getDnCaseChange()));
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
                    ehm.setAttributeNames(h.getMergeAttribute().getAttributeNames().toArray(new String[]{}));
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
                    handlers.add(new RecursiveEntryHandler(h.getRecursive().getSearchAttribute(),
                            h.getRecursive().getMergeAttributes().toArray(new String[]{})));
                    break;
                default:
                    break;
            }
        });

        if (!handlers.isEmpty()) {
            LOGGER.debug("Search entry handlers defined for the entry resolver of [{}] are [{}]", l.getLdapUrl(), handlers);
            entryResolver.setSearchEntryHandlers(handlers.toArray(new SearchEntryHandler[]{}));
        }
        entryResolver.setReferralHandler(new SearchReferralHandler());
        return entryResolver;
    }

}
