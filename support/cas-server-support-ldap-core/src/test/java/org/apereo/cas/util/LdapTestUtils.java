package org.apereo.cas.util;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.AttributeModification;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.io.LdifReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Utility class used by all tests that provision and deprovision LDAP test data.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@UtilityClass
public class LdapTestUtils {

    /**
     * Placeholder for base DN in LDIF files.
     */
    private static final String BASE_DN_PLACEHOLDER = "${ldapBaseDn}";

    /**
     * System-wide newline character string.
     */
    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Reads an LDIF into a collection of LDAP entries. The components performs a simple property
     * replacement in the LDIF data where <pre>${ldapBaseDn}</pre> is replaced with the environment-specific base
     * DN.
     *
     * @param ldif   LDIF resource, typically a file on filesystem or classpath.
     * @param baseDn The directory branch where the entry resides.
     * @return LDAP entries contained in the LDIF.
     * @throws IOException On IO errors reading LDIF.
     */
    public static Collection<LdapEntry> readLdif(final InputStream ldif, final String baseDn) throws IOException {
        var ldapString = StringUtils.EMPTY;
        try (val reader = new BufferedReader(new InputStreamReader(ldif, StandardCharsets.UTF_8))) {
            ldapString = reader.lines()
                .map(line -> {
                    LOGGER.debug("Reading LDAP entry line [{}]", line);
                    if (line.contains(BASE_DN_PLACEHOLDER)) {
                        return line.replace(BASE_DN_PLACEHOLDER, baseDn);
                    }
                    return line;
                })
                .collect(Collectors.joining(NEWLINE));
            LOGGER.debug("LDIF to process is [{}]", ldapString);
            val entries = new LdifReader(new StringReader(ldapString)).read().getEntries();
            LOGGER.debug("Total entries read from LDAP are [{}] with baseDn [{}]", entries.size(), baseDn);
            return entries;
        }
    }

    /**
     * Create ldap entries.
     *
     * @param connection the connection
     * @param entries    the entries
     */
    public static void createLdapEntries(final LDAPConnection connection, final Collection<LdapEntry> entries) {
        createLdapEntries(connection, entries, null);
    }

    /**
     * Creates the given LDAP entries.
     *
     * @param connection Open LDAP connection used to connect to directory.
     * @param entries    Collection of LDAP entries.
     * @param connInit   the connection initializer
     */
    public static void createLdapEntries(final LDAPConnection connection, final Collection<LdapEntry> entries,
                                         final BindConnectionInitializer connInit) {
        for (val entry : entries) {
            val attrs = new ArrayList<Attribute>(entry.getAttributeNames().length);
            attrs.addAll(entry.getAttributes().stream()
                .map(a -> new Attribute(a.getName(), a.getStringValues()))
                .collect(Collectors.toList()));

            val ad = new AddRequest(entry.getDn(), attrs);
            LOGGER.debug("Creating entry [{}] with attributes [{}]", entry, attrs);
            try {
                connection.add(ad);
            } catch (final LDAPException e) {
                LOGGER.debug(e.getMessage(), e);
                if (e.getResultCode().equals(ResultCode.ENTRY_ALREADY_EXISTS)) {
                    modifyLdapEntries(connection, entries, connInit);
                } else {
                    LOGGER.error(e.getMessage(), e);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Modify ldap entries.
     *
     * @param connection the connection
     * @param entries    the entries
     * @param connInit   the connection initializer
     */
    public static void modifyLdapEntries(final LDAPConnection connection, final Collection<LdapEntry> entries,
                                         final BindConnectionInitializer connInit) {
        entries.forEach(entry -> {
            LOGGER.debug("Modifying LDAP entry [{}]", entry);
            entry.getAttributes().forEach(ldapAttribute -> modifyLdapEntry(connection, entry, ldapAttribute, connInit));
        });
    }

    /**
     * Modify ldap entries.
     *
     * @param connection the connection
     * @param entries    the entries
     */
    public static void modifyLdapEntries(final LDAPConnection connection, final Collection<LdapEntry> entries) {
        modifyLdapEntries(connection, entries, null);
    }

    /**
     * Modify ldap entry.
     *
     * @param serverCon the server con
     * @param dn        the dn
     * @param attr      the attr
     * @param add       the add
     * @param connInit  the connection initializer
     */
    public static void modifyLdapEntry(final LDAPConnection serverCon, final String dn,
                                       final LdapAttribute attr,
                                       final AttributeModification.Type add,
                                       final BindConnectionInitializer connInit) {

        val address = "ldap://" + serverCon.getConnectedAddress() + ':' + serverCon.getConnectedPort();
        val config = new ConnectionConfig(address);
        if (connInit != null) {
            config.setConnectionInitializers(connInit);
        }
        LOGGER.debug("Created modification request connection configuration [{}] for [{}]", config, address);
        val connectionFactory = new DefaultConnectionFactory(config);
        try {
            val modify = new ModifyOperation(connectionFactory);
            val request = new ModifyRequest(dn, new AttributeModification(add, attr));
            LOGGER.debug("Executing modification request [{}] with type [{}] for [{}]", request, add, dn);
            modify.execute(request);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            connectionFactory.close();
        }
    }

    /**
     * Modify ldap entry.
     *
     * @param serverCon the server con
     * @param dn        the dn
     * @param attr      the attr
     * @param connInit  the connection initializer
     */
    public static void modifyLdapEntry(final LDAPConnection serverCon, final LdapEntry dn,
                                       final LdapAttribute attr,
                                       final BindConnectionInitializer connInit) {
        modifyLdapEntry(serverCon, dn.getDn(), attr, AttributeModification.Type.ADD, connInit);
    }

    /**
     * Modify ldap entry.
     *
     * @param serverCon the server con
     * @param dn        the dn
     * @param attr      the attr
     */
    public static void modifyLdapEntry(final LDAPConnection serverCon, final LdapEntry dn,
                                       final LdapAttribute attr) {
        modifyLdapEntry(serverCon, dn.getDn(), attr, AttributeModification.Type.ADD, null);
    }
}
