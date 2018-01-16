package org.apereo.cas.adaptors.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;
import org.springframework.core.io.ClassPathResource;

/**
 * Base class for LDAP tests that provision and de-provision DIRECTORY data as part of test setup/teardown.
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public abstract class AbstractLdapTests {

    private static Map<Integer, InMemoryTestLdapDirectoryServer> DIRECTORY_MAP = new HashMap<>();

    public static synchronized void initDirectoryServer(final InputStream ldifFile,
                                                        final int port) {
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

    public static void initDirectoryServer(final int port) throws IOException {
        initDirectoryServer(new ClassPathResource("ldif/ldap-base.ldif").getInputStream(), port);
    }

    protected static InMemoryTestLdapDirectoryServer getDirectory(final int port) {
        return getLdapDirectory(port);
    }

    protected static InMemoryTestLdapDirectoryServer getLdapDirectory(final int port) {
        return DIRECTORY_MAP.get(port);
    }
}
