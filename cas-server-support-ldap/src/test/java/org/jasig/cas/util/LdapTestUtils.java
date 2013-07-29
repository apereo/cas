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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.io.LdifReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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

    /** Prefix for creating strong but predictable passwords of the format {prefix}{sn}. */
    public static final String PASSWORD_PREFIX = "Pa$$word.";

    /** AD user password attribute name. */
    private static final String AD_PASSWORD_ATTR = "unicodePwd";

    /** AD user password character encoding. */
    private static final Charset AD_PASSWORD_ENCODING = Charset.forName("UTF-16LE");

    /** AD user account control attribute name. */
    private static final String AD_ACCT_CONTROL_ATTR = "userAccountControl";

    /** AD user account control value for active account. */
    private static final String AD_ACCT_ACTIVE = "512";

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
            reader.close();
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
            throws LdapException {

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

        // AD requires some special handling for setting password and account state
        if (DirectoryType.ActiveDirectory.equals(dirType)) {
            for (final LdapEntry entry : entries) {
                // AD requires quotes around literal password string
                final String password = '\"' + getPassword(entry) + '\"';
                final ModifyRequest modify = new ModifyRequest(
                        entry.getDn(),
                        new AttributeModification(
                                AttributeModificationType.REPLACE,
                                new LdapAttribute(AD_PASSWORD_ATTR, password.getBytes(AD_PASSWORD_ENCODING))),
                        new AttributeModification(
                                AttributeModificationType.REPLACE,
                                new LdapAttribute(AD_ACCT_CONTROL_ATTR, AD_ACCT_ACTIVE)));
                try {
                    new ModifyOperation(connection).execute(modify);
                } catch (final LdapException e) {
                    LOGGER.warn("LDAP error modifying entry {}", entry, e);
                    throw e;
                }
            }
        }
    }

    /**
     * Removes the given LDAP entries.
     *
     * @param connection Open LDAP connection used to connect to directory.
     * @param entries Collection of LDAP entries.
     */
    public static void removeLdapEntries(final Connection connection, final Collection<LdapEntry> entries) {
        for (final LdapEntry entry : entries) {
            try {
                new DeleteOperation(connection).execute(new DeleteRequest(entry.getDn()));
            } catch (final LdapException e) {
                LOGGER.warn("LDAP error removing entry {}", entry, e);
            }
        }
    }

    public static String getPassword(final LdapEntry entry) {
        return PASSWORD_PREFIX + entry.getAttribute("sn").getStringValue();
    }
}
