/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util.ldap.uboundid;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import org.jasig.cas.util.LdapTestUtils;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class InMemoryTestLdapDirectoryServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryTestLdapDirectoryServer.class);

    private final InMemoryDirectoryServer directoryServer;

    private Collection<LdapEntry> ldapEntries;

    /**
     * Instantiates a new Ldap directory server.
     */
    public InMemoryTestLdapDirectoryServer(final File properties, final File ldifFile, final File... schemaFile) {

        try {

            final Properties p = new Properties();
            p.load(new FileInputStream(properties));

            final InMemoryDirectoryServerConfig config =
                    new InMemoryDirectoryServerConfig(p.getProperty("ldap.rootDn"));
            config.addAdditionalBindCredentials(p.getProperty("ldap.managerDn"), p.getProperty("ldap.managerPassword"));

            final String serverKeyStorePath = new ClassPathResource("/ldapServerTrustStore").getFile().getCanonicalPath();
            final SSLUtil serverSSLUtil = new SSLUtil(
                    new KeyStoreKeyManager(serverKeyStorePath, "changeit".toCharArray()), new TrustStoreTrustManager(serverKeyStorePath));
            final SSLUtil clientSSLUtil = new SSLUtil(new TrustStoreTrustManager(serverKeyStorePath));
            config.setListenerConfigs(
                    InMemoryListenerConfig.createLDAPConfig("LDAP", // Listener name
                            null, // Listen address. (null = listen on all interfaces)
                            1389, // Listen port (0 = automatically choose an available port)
                            serverSSLUtil.createSSLSocketFactory()), // StartTLS factory
                    InMemoryListenerConfig.createLDAPSConfig("LDAPS", // Listener name
                            null, // Listen address. (null = listen on all interfaces)
                            1636, // Listen port (0 = automatically choose an available port)
                            serverSSLUtil.createSSLServerSocketFactory(), // Server factory
                            clientSSLUtil.createSSLSocketFactory())); // Client factory

            config.setEnforceSingleStructuralObjectClass(false);
            config.setEnforceAttributeSyntaxCompliance(true);

            final Schema s = Schema.mergeSchemas(Schema.getSchema(schemaFile));
            config.setSchema(s);

            this.directoryServer = new InMemoryDirectoryServer(config);

            LOGGER.debug("Populating directory with {}", ldifFile);
            this.directoryServer.importFromLDIF(true, ldifFile.getCanonicalPath());
            this.directoryServer.startListening();

            final LDAPConnection c = getConnection();
            LOGGER.debug("Connected to {}:{}", c.getConnectedAddress(), c.getConnectedPort());

            populateDefaultEntries(c);

            c.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateDefaultEntries(final LDAPConnection c) throws Exception {
        populateEntries(c, new ClassPathResource("ldif/users-groups.ldif"));
    }

    public void populateEntries(final Resource rs) throws Exception {
        populateEntries(getConnection(), rs);
    }

    protected void populateEntries(final LDAPConnection c, final Resource rs) throws Exception {
        this.ldapEntries = LdapTestUtils.readLdif(rs, getBaseDn());
        LdapTestUtils.createLdapEntries(c, ldapEntries);
        populateEntriesInternal(c);
    }

    protected void populateEntriesInternal(final LDAPConnection c) {}

    public String getBaseDn() {
        return this.directoryServer.getBaseDNs().get(0).toNormalizedString();
    }

    public  Collection<LdapEntry> getLdapEntries() {
        return this.ldapEntries;
    }

    public LDAPConnection getConnection() throws LDAPException {
        return this.directoryServer.getConnection();
    }

    @Override
    public void close() throws IOException {
         this.directoryServer.shutDown(true);
    }
}
