package org.apereo.cas.util.ldap.uboundid;

import org.apereo.cas.util.LdapTestUtils;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class InMemoryTestLdapDirectoryServer implements AutoCloseable, DisposableBean {

    private final InMemoryDirectoryServer directoryServer;

    private Collection<LdapEntry> ldapEntries;

    /**
     * Instantiates a new Ldap directory server.
     * Parameters need to be streams so they can be read from JARs.
     */
    @SneakyThrows
    public InMemoryTestLdapDirectoryServer(final InputStream properties,
                                           final InputStream ldifFile,
                                           final InputStream schemaFile,
                                           final int port) {
        LOGGER.debug("Loading properties...");
        val p = new Properties();
        p.load(properties);

        val config =
            new InMemoryDirectoryServerConfig(p.getProperty("ldap.rootDn"));
        config.addAdditionalBindCredentials(p.getProperty("ldap.managerDn"), p.getProperty("ldap.managerPassword"));

        LOGGER.debug("Loading keystore file...");
        val keystoreFile = File.createTempFile("key", "store");
        try (OutputStream outputStream = new FileOutputStream(keystoreFile)) {
            IOUtils.copy(new ClassPathResource("/ldapServerTrustStore").getInputStream(), outputStream);
        }

        val serverKeyStorePath = keystoreFile.getCanonicalPath();
        val serverSSLUtil = new SSLUtil(
            new KeyStoreKeyManager(serverKeyStorePath, "changeit".toCharArray()),
            new TrustStoreTrustManager(serverKeyStorePath));
        val clientSSLUtil = new SSLUtil(new TrustStoreTrustManager(serverKeyStorePath));

        LOGGER.debug("Loading LDAP listeners and ports...");
        config.setListenerConfigs(
            InMemoryListenerConfig.createLDAPConfig("LDAP",
                null,
                port,
                serverSSLUtil.createSSLSocketFactory()),
            InMemoryListenerConfig.createLDAPSConfig("LDAPS",
                null,
                0,
                serverSSLUtil.createSSLServerSocketFactory(),
                clientSSLUtil.createSSLSocketFactory()));

        config.setEnforceSingleStructuralObjectClass(false);
        config.setEnforceAttributeSyntaxCompliance(true);
        config.setMaxConnections(-1);

        LOGGER.debug("Loading LDAP schema...");
        val file = File.createTempFile("ldap", "schema");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(schemaFile, outputStream);
        }

        LOGGER.debug("Setting LDAP schema...");
        val s = Schema.mergeSchemas(Schema.getSchema(file));
        config.setSchema(s);

        this.directoryServer = new InMemoryDirectoryServer(config);
        LOGGER.debug("Populating directory...");

        LOGGER.debug("Loading LDIF file...");
        val ldif = File.createTempFile("ldiff", "file");
        try (OutputStream outputStream = new FileOutputStream(ldif)) {
            IOUtils.copy(ldifFile, outputStream);
        }

        LOGGER.debug("Importing LDIF file...");
        this.directoryServer.importFromLDIF(true, ldif.getCanonicalPath());

        var retryCount = 5;
        while (retryCount > 0) {
            try {
                LOGGER.debug("Trying to restart LDAP server: attempt [{}]", retryCount);
                this.directoryServer.restartServer();
                try (val c = getConnection()) {
                    LOGGER.debug("Connected to [{}]:[{}]", c.getConnectedAddress(), c.getConnectedPort());
                    populateDefaultEntries(c);
                }
                retryCount = 0;
            } catch (final Exception e) {
                retryCount--;
            }
        }
    }

    private void populateDefaultEntries(final LDAPConnection c) throws Exception {
        populateEntries(c, new ClassPathResource("ldif/users-groups.ldif").getInputStream());
    }

    public void populateEntries(final InputStream rs) throws Exception {
        try (val connection = getConnection()) {
            populateEntries(connection, rs);
        }
    }

    protected void populateEntries(final LDAPConnection c, final InputStream rs) throws Exception {
        this.ldapEntries = LdapTestUtils.readLdif(rs, getBaseDn());
        LdapTestUtils.createLdapEntries(c, ldapEntries);
        populateEntriesInternal(c);
    }

    protected void populateEntriesInternal(final LDAPConnection c) {
    }

    public String getBaseDn() {
        return this.directoryServer.getBaseDNs().get(0).toNormalizedString();
    }

    public Collection<LdapEntry> getLdapEntries() {
        return this.ldapEntries;
    }

    public LDAPConnection getConnection() throws LDAPException {
        return this.directoryServer.getConnection();
    }

    @Override
    public void close() {
        LOGGER.debug("Shutting down LDAP server...");
        this.directoryServer.closeAllConnections(true);
        this.directoryServer.shutDown(true);
        LOGGER.debug("Shut down LDAP server.");
    }

    @Override
    public void destroy() {
        close();
    }

    public boolean isAlive() {
        try {
            return getConnection() != null;
        } catch (final Exception e) {
            return false;
        }
    }
}
