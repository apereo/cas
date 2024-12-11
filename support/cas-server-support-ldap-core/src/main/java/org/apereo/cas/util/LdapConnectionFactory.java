package org.apereo.cas.util;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;
import org.ldaptive.ad.UnicodePwdAttribute;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.referral.FollowSearchReferralHandler;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link LdapConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class LdapConnectionFactory implements Closeable {
    private final ConnectionFactory connectionFactory;
    /**
     * Execute add operation.
     *
     * @param entry the entry
     * @return true/false
     */
    public boolean executeAddOperation(final LdapEntry entry) {
        return FunctionUtils.doAndHandle(() -> {
            val operation = new AddOperation(connectionFactory);
            val response = operation.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            LOGGER.debug("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
            return response.getResultCode() == ResultCode.SUCCESS;
        }, e -> false).get();
    }


    /**
     * Execute delete operation boolean.
     *
     * @param entry the entry
     * @return true/false
     */
    public boolean executeDeleteOperation(final LdapEntry entry) {
        return FunctionUtils.doAndHandle(() -> {
            val delete = new DeleteOperation(connectionFactory);
            val request = new DeleteRequest(entry.getDn());
            val response = delete.execute(request);
            LOGGER.debug("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
            return response.getResultCode() == ResultCode.SUCCESS;
        }, e -> false).get();
    }


    /**
     * Execute modify operation.
     *
     * @param currentDn  the current dn
     * @param attributes the attributes
     * @return true/false
     */
    public boolean executeModifyOperation(final String currentDn,
                                          final Map<String, ? extends Set<String>> attributes) {
        return FunctionUtils.doAndHandle(() -> {
            val operation = new ModifyOperation(connectionFactory);
            val mods = attributes.entrySet()
                .stream()
                .map(entry -> {
                    val values = entry.getValue().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
                    val attr = new LdapAttribute(entry.getKey(), values);
                    LOGGER.debug("Constructed new attribute [{}]", attr);
                    return new AttributeModification(AttributeModification.Type.REPLACE, attr);
                })
                .toArray(AttributeModification[]::new);
            val request = new ModifyRequest(currentDn, mods);
            val response = operation.execute(request);
            LOGGER.debug("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
            return response.getResultCode() == ResultCode.SUCCESS;
        }, e -> false).get();
    }

    /**
     * Execute modify operation boolean.
     *
     * @param currentDn the current dn
     * @param entry     the entry
     * @return true/false
     */
    public boolean executeModifyOperation(final String currentDn, final LdapEntry entry) {
        val attributes = entry.getAttributes().stream()
            .collect(Collectors.toMap(LdapAttribute::getName,
                ldapAttribute -> new HashSet<>(ldapAttribute.getStringValues())));
        return executeModifyOperation(currentDn, attributes);
    }


    /**
     * Execute search operation.
     *
     * @param baseDn           the base dn
     * @param filter           the filter
     * @param pageSize         the page size
     * @param returnAttributes the return attributes
     * @return the response
     * @throws LdapException the ldap exception
     */
    public SearchResponse executeSearchOperation(
        final String baseDn,
        final FilterTemplate filter,
        final int pageSize,
        final String... returnAttributes) throws LdapException {
        return executeSearchOperation(baseDn, filter, pageSize, null, returnAttributes);
    }

    /**
     * Execute search operation.
     *
     * @param baseDn           the base dn
     * @param filter           the filter
     * @param pageSize         the page size
     * @param binaryAttributes the binary attributes
     * @param returnAttributes the return attributes
     * @return the response
     * @throws LdapException the ldap exception
     */
    public SearchResponse executeSearchOperation(
        final String baseDn,
        final FilterTemplate filter,
        final int pageSize,
        final String[] binaryAttributes,
        final String[] returnAttributes) throws LdapException {
        val request = LdapUtils.newLdaptiveSearchRequest(baseDn, filter, binaryAttributes, returnAttributes);
        if (pageSize <= 0) {
            val searchOperation = new SearchOperation(connectionFactory);
            searchOperation.setSearchResultHandlers(new FollowSearchReferralHandler());
            return searchOperation.execute(request);
        }
        val client = new PagedResultsClient(connectionFactory, pageSize);
        return client.executeToCompletion(request);
    }

    /**
     * Execute search operation response.
     *
     * @param baseDn   the base dn
     * @param filter   the filter
     * @param pageSize the page size
     * @return the response
     * @throws LdapException the ldap exception
     */
    public SearchResponse executeSearchOperation(
        final String baseDn,
        final FilterTemplate filter,
        final int pageSize) throws LdapException {
        return executeSearchOperation(baseDn, filter, pageSize,
            ReturnAttributes.ALL_USER.value(), ReturnAttributes.ALL_USER.value());
    }


    /**
     * Execute a password modify operation.
     *
     * @param currentDn   the current dn
     * @param oldPassword the old password
     * @param newPassword the new password
     * @param type        the type
     * @return true /false
     * <p>
     * AD NOTE: Resetting passwords requires binding to AD as user with privileges to reset other users passwords
     * and it does not validate old password or respect directory policies such as history or minimum password age.
     * Changing a password with the old password does respect directory policies and requires no account operator
     * privileges on the bind user. Pass in blank old password if reset is in order (e.g. forgot password) vs.
     * letting user change their own (e.g. expiring) password.
     */
    public boolean executePasswordModifyOperation(final String currentDn,
                                                  final char[] oldPassword,
                                                  final char[] newPassword,
                                                  final AbstractLdapProperties.LdapType type) {
        try {
            val oldPasswordAvailable = oldPassword != null && oldPassword.length > 0;
            val connConfig = connectionFactory.getConnectionConfig();
            val secureLdap = connConfig.getLdapUrl() != null && !connConfig.getLdapUrl().toLowerCase(Locale.ENGLISH).contains("ldaps://");
            if (connConfig.getUseStartTLS() || secureLdap) {
                LOGGER.warn("Executing password modification op under a non-secure LDAP connection; "
                            + "To modify password attributes, the connection to the LDAP server {} be secured and/or encrypted.",
                    type == AbstractLdapProperties.LdapType.AD ? "MUST" : "SHOULD");
            }
            if (type == AbstractLdapProperties.LdapType.AD) {
                LOGGER.debug("Executing password change op for active directory based on "
                             + "[https://support.microsoft.com/en-us/kb/269190]"
                             + "change type: [{}]", oldPasswordAvailable ? "change" : "reset");
                val operation = new ModifyOperation(connectionFactory);
                val response = oldPasswordAvailable
                    ?
                    operation.execute(new ModifyRequest(currentDn,
                        new AttributeModification(AttributeModification.Type.DELETE, new UnicodePwdAttribute(new String(oldPassword))),
                        new AttributeModification(AttributeModification.Type.ADD, new UnicodePwdAttribute(new String(newPassword)))))
                    :
                    operation.execute(new ModifyRequest(currentDn,
                        new AttributeModification(AttributeModification.Type.REPLACE, new UnicodePwdAttribute(new String(newPassword)))));

                val success = response.getResultCode() == ResultCode.SUCCESS;
                val logLevel = success ? LOGGER.atDebug() : LOGGER.atError();
                logLevel.log("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
                return success;
            }

            LOGGER.debug("Executing password modification op for generic LDAP");
            val operation = new ExtendedOperation(connectionFactory);
            val response = operation.execute(new PasswordModifyRequest(currentDn,
                oldPasswordAvailable ? new String(oldPassword) : null, new String(newPassword)));
            LOGGER.debug("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
            return response.getResultCode() == ResultCode.SUCCESS;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void close() {
        connectionFactory.close();
    }
}
