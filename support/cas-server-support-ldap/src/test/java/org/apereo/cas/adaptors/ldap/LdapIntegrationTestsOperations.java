package org.apereo.cas.adaptors.ldap;

import org.apereo.cas.util.LdapTestUtils;
import org.apereo.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.ldaptive.BindConnectionInitializer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for LDAP tests that provision and de-provision DIRECTORY data as part of test setup/tear-down.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class LdapIntegrationTestsOperations {
    private static final Map<Integer, InMemoryTestLdapDirectoryServer> DIRECTORY_MAP = new HashMap<>();

    /**
     * Init directory server.
     *
     * @param ldifFile the ldif file
     * @param port     the port
     */
    public static synchronized void initDirectoryServer(final InputStream ldifFile, final int port) {
        try {
            val directory = DIRECTORY_MAP.get(port);
            val createInstance = directory == null || !directory.isAlive();
            if (createInstance) {
                val properties = new ClassPathResource("ldapserver.properties");
                val schema = new ClassPathResource("schema/standard-ldap.schema");
                DIRECTORY_MAP.put(port, new InMemoryTestLdapDirectoryServer(properties.getInputStream(), ldifFile, schema.getInputStream(), port));
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Init directory server.
     *
     * @param port the port
     * @throws IOException the io exception
     */
    public static void initDirectoryServer(final int port) throws IOException {
        initDirectoryServer(new ClassPathResource("ldif/ldap-base.ldif").getInputStream(), port);
    }

    /**
     * Gets ldap directory.
     *
     * @param port the port
     * @return the ldap directory
     */
    public static InMemoryTestLdapDirectoryServer getLdapDirectory(final int port) {
        return DIRECTORY_MAP.get(port);
    }

    /**
     * Populate entries.
     *
     * @param c      the c
     * @param rs     the rs
     * @param baseDn the base dn
     * @throws Exception the exception
     */
    public static void populateEntries(final LDAPConnection c, final InputStream rs, final String baseDn) throws Exception {
        val entries = LdapTestUtils.readLdif(rs, baseDn);
        LdapTestUtils.createLdapEntries(c, entries, null);
    }

    /**
     * Populate entries.
     *
     * @param c        the c
     * @param rs       the rs
     * @param baseDn   the base dn
     * @param connInit the connection initializer
     * @throws Exception the exception
     */
    public static void populateEntries(final LDAPConnection c, final InputStream rs, final String baseDn, final BindConnectionInitializer connInit) throws Exception {
        LdapTestUtils.createLdapEntries(c, LdapTestUtils.readLdif(rs, baseDn), connInit);
    }

    /**
     * Populate default entries.
     *
     * @param c      the c
     * @param baseDn the base dn
     * @throws Exception the exception
     */
    public static void populateDefaultEntries(final LDAPConnection c, final String baseDn) throws Exception {
        populateEntries(c, new ClassPathResource("ldif/users-groups.ldif").getInputStream(), baseDn);
    }
}
