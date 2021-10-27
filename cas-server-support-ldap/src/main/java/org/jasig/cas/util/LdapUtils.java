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
package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public final class LdapUtils {

    /** The Constant OBJECTCLASS_ATTRIBUTE. */
    public static final String OBJECTCLASS_ATTRIBUTE = "objectClass";

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);

    /**
     * Instantiates a new ldap utils.
     */
    private LdapUtils() {
        // private constructor so that no one can instantiate.
    }

    /**
     * Close the given context and ignore any thrown exception. This is useful
     * for typical finally blocks in manual Ldap statements.
     *
     * @param context the Ldap connection to close
     */
    public static void closeConnection(final Connection context) {
        if (context != null && context.isOpen()) {
            try {
                context.close();
            } catch (final Exception ex) {
                LOGGER.warn("Could not close ldap connection", ex);
            }
        }
    }

    /**
     * Reads a Boolean value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @return <code>true</code> if the attribute's value matches (case-insensitive) <code>"true"</code>, otherwise false
     */
    public static Boolean getBoolean(final LdapEntry ctx, final String attribute) {
        return getBoolean(ctx, attribute, Boolean.FALSE);
    }

    /**
     * Reads a Boolean value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return <code>true</code> if the attribute's value matches (case-insensitive) <code>"true"</code>, otherwise false
     */
    public static Boolean getBoolean(final LdapEntry ctx, final String attribute, final Boolean nullValue) {
        final String v = getString(ctx, attribute, nullValue.toString());
        if (v != null) {
            return v.equalsIgnoreCase(Boolean.TRUE.toString());
        }
        return nullValue;
    }

    /**
     * Reads a Long value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @return the long value
     */
    public static Long getLong(final LdapEntry ctx, final String attribute) {
        return getLong(ctx, attribute, Long.MIN_VALUE);
    }

    /**
     * Reads a Long value from the LdapEntry.
     *
     * @param entry       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return the long value
     */
    public static Long getLong(final LdapEntry entry, final String attribute, final Long nullValue) {
        final String v = getString(entry, attribute, nullValue.toString());
        if (v != null && NumberUtils.isNumber(v)) {
            return Long.valueOf(v);
        }
        return nullValue;
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param entry       the ldap entry
     * @param attribute the attribute name
     * @return the string
     */
    public static String getString(final LdapEntry entry, final String attribute) {
        return getString(entry, attribute, null);
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param entry       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     * @return the string
     */
    public static String getString(final LdapEntry entry, final String attribute, final String nullValue) {
        final LdapAttribute attr = entry.getAttribute(attribute);
        if (attr == null) {
            return nullValue;
        }

        String v = null;
        if (attr.isBinary()) {
            final byte[] b = attr.getBinaryValue();
            v = new String(b, Charset.forName("UTF-8"));
        } else {
            v = attr.getStringValue();
        }

        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        return nullValue;
    }
}
