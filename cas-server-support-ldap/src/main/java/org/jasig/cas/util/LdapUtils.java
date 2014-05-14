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

import org.apache.commons.lang.math.NumberUtils;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0
 */
public final class LdapUtils {

    public static final String OBJECTCLASS_ATTRIBUTE = "objectClass";

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);

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
        return getBoolean(ctx, attribute, false);
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
     */
    public static Long getLong(final LdapEntry ctx, final String attribute) {
        return getLong(ctx, attribute, Long.MIN_VALUE);
    }

    /**
     * Reads a Long value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     */
    public static Long getLong(final LdapEntry ctx, final String attribute, final Long nullValue) {
        final String v = getString(ctx, attribute, nullValue.toString());
        if (v != null && NumberUtils.isNumber(v)) {
            return Long.valueOf(v);
        }
        return nullValue;
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     */
    public static String getString(final LdapEntry ctx, final String attribute) {
        return getString(ctx, attribute, null);
    }

    /**
     * Reads a String value from the LdapEntry.
     *
     * @param ctx       the ldap entry
     * @param attribute the attribute name
     * @param nullValue the value which should be returning in case of a null value
     */
    public static String getString(final LdapEntry ctx, final String attribute, final String nullValue) {
        final LdapAttribute attr = ctx.getAttribute(attribute);
        if (attr == null) {
            return nullValue;
        }

        final String v = attr.getStringValue();
        if (v != null) {
            return v;
        }
        return nullValue;
    }
}
