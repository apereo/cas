/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
import com.unboundid.ldap.sdk.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * @author Misagh Moayyed
 */
public final class InMemoryTestLdapDirectoryServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryTestLdapDirectoryServer.class);

    private final InMemoryDirectoryServer directoryServer;

    /**
     * Instantiates a new Ldap directory server.
     */
    public InMemoryTestLdapDirectoryServer(final File properties, final File schemaFile, final File ldifFile) {

        try {
            //final File rootResourcesDirectory = new File("cas-server-support-ldap/src/test/resources");
            //final File rootLdifDirectory = new File(rootResourcesDirectory, "ldif");
            //final File rootschemaDirectory = new File(rootResourcesDirectory, "schema");
            //final File schemaFile = new File(rootschemaDirectory, "standard-ldap.schema");
            //final Collection<File> collection = FileUtils.listFiles(rootLdifDirectory, new String[]{"ldif"}, true);

            final Properties p = new Properties();
            p.load(new FileInputStream(properties));

            final InMemoryDirectoryServerConfig config =
                    new InMemoryDirectoryServerConfig(p.getProperty("ldap.rootDn"));
            config.addAdditionalBindCredentials(p.getProperty("ldap.managerDn"), p.getProperty("ldap.managerPassword"));
            config.setListenerConfigs(
                    new InMemoryListenerConfig("ldapListener", InetAddress.getLocalHost(), 10389, null, null, null)
            );

            final Schema s = Schema.getSchema(schemaFile.getCanonicalPath());
            config.setSchema(s);

            this.directoryServer = new InMemoryDirectoryServer(config);

            LOGGER.debug("Populating directory with {}", ldifFile);
            this.directoryServer.importFromLDIF(true, ldifFile.getCanonicalPath());
            this.directoryServer.startListening();


        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
         this.directoryServer.shutDown(true);
    }
}
