package org.jasig.cas.util;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.referral.DeleteReferralHandler;
import org.ldaptive.referral.ModifyReferralHandler;
import org.ldaptive.referral.SearchReferralHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public final class LdapUtils {
    /**
     * The objectClass attribute.
     */
    public static final String OBJECTCLASS_ATTRIBUTE = "objectClass";

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);


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
        if (v != null && NumberUtils.isNumber(v)) {
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
            v = new String(b, Charset.forName("UTF-8"));
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
     * @return the response
     * @throws LdapException the ldap exception
     */
    public static Response<SearchResult> executeSearchOperation(final ConnectionFactory connectionFactory,
                                                                final String baseDn,
                                                                final SearchFilter filter)
            throws LdapException {
        try (Connection connection = createConnection(connectionFactory)) {
            final SearchOperation searchOperation = new SearchOperation(connection);
            final SearchRequest request = createSearchRequest(baseDn, filter);
            request.setReferralHandler(new SearchReferralHandler());
            return searchOperation.execute(request);
        }
    }

    /**
     * Builds a new request.
     *
     * @param baseDn the base dn
     * @param filter the filter
     * @return the search request
     */
    public static SearchRequest createSearchRequest(final String baseDn, final SearchFilter filter) {
        final SearchRequest sr = new SearchRequest(baseDn, filter);
        sr.setBinaryAttributes(ReturnAttributes.ALL_USER.value());
        sr.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        sr.setSearchScope(SearchScope.SUBTREE);
        return sr;
    }

    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    public static boolean containsResultEntry(final Response<SearchResult> response) {
        final SearchResult result = response.getResult();
        if (result != null && result.getEntry() != null) {
            return true;
        }
        return false;
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
     * Execute modify operation boolean.
     *
     * @param currentDn         the current dn
     * @param connectionFactory the connection factory
     * @param attributes        the attributes
     * @return the boolean
     */
    public static boolean executeModifyOperation(final String currentDn,
                                                 final ConnectionFactory connectionFactory,
                                                 final Map<String, Set<String>> attributes) {
        try (Connection modifyConnection = createConnection(connectionFactory)) {
            final ModifyOperation operation = new ModifyOperation(modifyConnection);
            final List<AttributeModification> mods = new ArrayList<>();
            for (final Map.Entry<String, Set<String>> entry : attributes.entrySet()) {
                mods.add(new AttributeModification(AttributeModificationType.REPLACE,
                        new LdapAttribute(entry.getKey(), entry.getValue().toArray(new String[]{}))));
            }
            final ModifyRequest request = new ModifyRequest(currentDn,
                    mods.toArray(new AttributeModification[]{}));
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
     * @return the boolean
     */
    public static boolean executeModifyOperation(final String currentDn,
                                                 final ConnectionFactory connectionFactory,
                                                 final LdapEntry entry) {
        final Map<String, Set<String>> attributes = new HashMap<>(entry.getAttribute().size());
        for (final LdapAttribute ldapAttribute : entry.getAttributes()) {
            attributes.put(ldapAttribute.getName(), ImmutableSet.copyOf(ldapAttribute.getStringValues()));
        }
        return executeModifyOperation(currentDn, connectionFactory, attributes);
    }

    /**
     * Execute add operation boolean.
     *
     * @param connectionFactory the connection factory
     * @param entry             the entry
     * @return the boolean
     * @throws LdapException the ldap exception
     */
    public static boolean executeAddOperation(final ConnectionFactory connectionFactory, final LdapEntry entry)
            throws LdapException {

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
     * @return the boolean
     * @throws LdapException the ldap exception
     */
    public static boolean executeDeleteOperation(final ConnectionFactory connectionFactory,
                                                 final LdapEntry entry) throws LdapException {

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
}
