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

package org.jasig.cas.adaptors.x509.authentication.handler.support;

import com.unboundid.ldap.sdk.LDAPConnection;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.jasig.cas.util.CompressionUtils;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;

/**
 * Parent class to help with testing x509 operations that deal with LDAP.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractX509LdapTests extends AbstractLdapTests {
    private static final String DN = "CN=x509,ou=people,dc=example,dc=org";

    public static void bootstrap() throws Exception {
        initDirectoryServer();
        getDirectory().populateEntries(new ClassPathResource("ldif/users-x509.ldif").getInputStream());

        /**
         * Dynamically set the attribute value to the crl content.
         * Encode it as base64 first. Doing this in the code rather
         * than in the ldif file to ensure the attribute can be populated
         * without dependencies on the classpath and or filesystem.
         */
        final Collection<LdapEntry> col = getDirectory().getLdapEntries();
        for (final LdapEntry ldapEntry : col) {
            if (ldapEntry.getDn().equals(DN)) {
                final LdapAttribute attr = new LdapAttribute(true);

                byte[] value = new byte[1024];
                IOUtils.read(new ClassPathResource("userCA-valid.crl").getInputStream(), value);
                value = CompressionUtils.encodeBase64ToByteArray(value);
                attr.setName("certificateRevocationList");
                attr.addBinaryValue(value);

                final LDAPConnection serverCon = getDirectory().getConnection();
                final String address = "ldap://" + serverCon.getConnectedAddress() + ":" + serverCon.getConnectedPort();
                final Connection conn = DefaultConnectionFactory.getConnection(address);
                conn.open();
                final ModifyOperation modify = new ModifyOperation(conn);
                modify.execute(new ModifyRequest(ldapEntry.getDn(),
                        new AttributeModification(AttributeModificationType.ADD, attr)));
                conn.close();
                serverCon.close();
                return;
            }
        }
    }

    public final String getTestDN() {
        return DN;
    }
}
