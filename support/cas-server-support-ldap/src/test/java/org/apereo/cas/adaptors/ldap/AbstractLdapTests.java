package org.apereo.cas.adaptors.ldap;

import org.apereo.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;
import org.ldaptive.LdapEntry;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Base class for LDAP tests that provision and de-provision DIRECTORY data as part of test setup/teardown.
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractLdapTests {

    protected static InMemoryTestLdapDirectoryServer DIRECTORY;

    public static synchronized void initDirectoryServer(final InputStream ldifFile,
                                                        final int port) {
        try {
            final boolean createInstance = DIRECTORY == null || !DIRECTORY.isAlive();
            if (createInstance) {
                final ClassPathResource properties = new ClassPathResource("ldap.properties");
                final ClassPathResource schema = new ClassPathResource("schema/standard-ldap.schema");
                DIRECTORY = new InMemoryTestLdapDirectoryServer(properties.getInputStream(), ldifFile, schema.getInputStream(), port);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static void initDirectoryServer(final int port) throws IOException {
        initDirectoryServer(new ClassPathResource("ldif/ldap-base.ldif").getInputStream(), port);
    }

    public static void initDirectoryServer() throws IOException {
        initDirectoryServer(1389);
    }

    protected static InMemoryTestLdapDirectoryServer getDirectory() {
        return DIRECTORY;
    }

    protected Collection<LdapEntry> getEntries() {
        return DIRECTORY.getLdapEntries();
    }
}
