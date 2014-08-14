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
package org.jasig.cas.util;

import org.apache.commons.io.IOUtils;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.io.LdifReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;

/**
 * Utility class used by all tests that provision and deprovision LDAP test data.
 *
 * @author Marvin S. Addison
 */
public final class LdapTestUtils {

    public enum DirectoryType {
        ActiveDirectory,
        OpenLdap
    }

    /** Placeholder for base DN in LDIF files. */
    private static final String BASE_DN_PLACEHOLDER = "${ldapBaseDn}";

    /** System-wide newline character string. */
    private static final String NEWLINE = System.getProperty("line.separator");

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapTestUtils.class);

    /** Private constructor of utility class. */
    private LdapTestUtils() {}


    /**
     * Reads an LDIF into a collection of LDAP entries. The components performs a simple property
     * replacement in the LDIF data where <pre>${ldapBaseDn}</pre> is replaced with the environment-specific base
     * DN.
     *
     * @param ldif LDIF resource, typically a file on filesystem or classpath.
     * @param baseDn The directory branch where the entry resides.
     *
     * @return LDAP entries contained in the LDIF.
     *
     * @throws IOException On IO errors reading LDIF.
     */
    public static Collection<LdapEntry> readLdif(final Resource ldif, final String baseDn) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(ldif.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(BASE_DN_PLACEHOLDER)) {
                    builder.append(line.replace(BASE_DN_PLACEHOLDER, baseDn));
                } else {
                    builder.append(line);
                }
                builder.append(NEWLINE);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return new LdifReader(new StringReader(builder.toString())).read().getEntries();
    }

    /**
     * Creates the given LDAP entries.
     *
     * @param connection Open LDAP connection used to connect to directory.
     * @param dirType Directory type (AD, OpenLDAP).
     * @param entries Collection of LDAP entries.
     *
     * @throws LdapException On LDAP errors.
     */
    public static void createLdapEntries(
            final Connection connection, final DirectoryType dirType, final Collection<LdapEntry> entries)
            throws Exception {

        for (final LdapEntry entry : entries) {
            try {
                new AddOperation(connection).execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            } catch (final LdapException e) {
                // ignore entry already exists
                if (ResultCode.ENTRY_ALREADY_EXISTS != e.getResultCode()) {
                    LOGGER.warn("LDAP error creating entry {}", entry, e);
                    throw e;
                }
            }
        }

    }
}
