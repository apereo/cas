package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
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
import org.ldaptive.ad.UnicodePwdAttribute;
import org.ldaptive.extended.PasswordModifyOperation;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.referral.DeleteReferralHandler;
import org.ldaptive.referral.ModifyReferralHandler;
import org.ldaptive.referral.SearchReferralHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            final SearchRequest request = Beans.newLdaptiveSearchRequest(baseDn, filter, binaryAttributes, returnAttributes);
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
                    .stream().map(entry -> new AttributeModification(AttributeModificationType.REPLACE,
                            new LdapAttribute(entry.getKey(), entry.getValue().toArray(new String[]{})))).collect(Collectors.toList());
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
     * @throws LdapException the ldap exception
     */
    public static boolean executeAddOperation(final ConnectionFactory connectionFactory, final LdapEntry entry) throws LdapException {
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
     * @throws LdapException the ldap exception
     */
    public static boolean executeDeleteOperation(final ConnectionFactory connectionFactory, final LdapEntry entry) throws LdapException {
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
}
