package org.apereo.cas.adaptors.ldap;

import org.apereo.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Base class for LDAP tests that provision and deprovision DIRECTORY data as part of test setup/teardown.
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractLdapTests implements ApplicationContextAware {
    private static InMemoryTestLdapDirectoryServer DIRECTORY;

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext context;

    public static synchronized void initDirectoryServer(final InputStream ldifFile) throws IOException {
        try {
            if (DIRECTORY == null) {
                final ClassPathResource properties = new ClassPathResource("ldap.properties");
                final ClassPathResource schema = new ClassPathResource("schema/standard-ldap.schema");

                DIRECTORY = new InMemoryTestLdapDirectoryServer(properties.getInputStream(),
                        ldifFile,
                        schema.getInputStream());
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void initDirectoryServer() throws IOException {
        initDirectoryServer(new ClassPathResource("ldif/ldap-base.ldif").getInputStream());
    }

    protected static InMemoryTestLdapDirectoryServer getDirectory() {
        return DIRECTORY;
    }

    protected Collection<LdapEntry> getEntries() {
        return DIRECTORY.getLdapEntries();
    }

    protected String getUsername(final LdapEntry entry) {
        final String unameAttr = this.context.getBean("usernameAttribute", String.class);
        return entry.getAttribute(unameAttr).getStringValue();
    }
    
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
}
