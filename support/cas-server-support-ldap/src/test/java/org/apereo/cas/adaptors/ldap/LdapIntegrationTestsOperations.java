package org.apereo.cas.adaptors.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.LdapTestUtils;
import org.apereo.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;
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
@Slf4j
public class LdapIntegrationTestsOperations {
    private static Map<Integer, InMemoryTestLdapDirectoryServer> DIRECTORY_MAP = new HashMap<>();

    /**
     * Init directory server.
     *
     * @param ldifFile the ldif file
     * @param port     the port
     */
    public static synchronized void initDirectoryServer(final InputStream ldifFile, final int port) {
        try {
            final InMemoryTestLdapDirectoryServer directory = DIRECTORY_MAP.get(port);
            final boolean createInstance = directory == null || !directory.isAlive();
            if (createInstance) {
                final ClassPathResource properties = new ClassPathResource("ldapserver.properties");
                final ClassPathResource schema = new ClassPathResource("schema/standard-ldap.schema");
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
        LdapTestUtils.createLdapEntries(c, LdapTestUtils.readLdif(rs, baseDn));
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
