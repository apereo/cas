/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap.util;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

/**
 * A basic LDAP Utility class
 *
 * @author Siegfried Puchbauer, SPP (http://www.spp.at)
 * 
 */
public final class SpringLdapUtils {

    public static final String OBJECTCLASS_ATTRIBUTE = "objectclass";

    public static final String LDAP_BOOLEAN_TRUE = "TRUE";
    public static final String LDAP_BOOLEAN_FALSE = "FALSE";

    /**
     * Reads a Boolean value from the DirContextAdapter
     *
     * @param ctx       the DirContextAdapter
     * @param attribute the attribute name
     * @return <code>true</code> if the attribute's value matches (case-insensitive) <code>"true"</code>, otherwise false
     */
    public static Boolean getBoolean(final DirContextOperations ctx, final String attribute) {
        return getBoolean(ctx, attribute, false);
    }

    /**
     * Reads a Boolean value from the DirContextAdapter
     *
     * @param ctx       the DirContextAdapter
     * @param attribute the attribute name
     * @param nullValue the value which sould be returing in case of a null value
     * @return <code>true</code> if the attribute's value matches (case-insensitive) <code>"true"</code>, otherwise false
     */
    public static Boolean getBoolean(final DirContextOperations ctx, final String attribute, final Boolean nullValue) {
        final String v = ctx.getStringAttribute(attribute);
        if (v != null) return v.equalsIgnoreCase(LDAP_BOOLEAN_TRUE);
        return nullValue;
    }

    /**
     * Sets the attribute <code>attribute</code> to the boolean value
     *
     * @param ctx       the DirContextAdapter
     * @param attribute the attribute name
     * @param value     the boolean value
     */
    public static void setBoolean(final DirContextOperations ctx, final String attribute, final Boolean value) {
        ctx.setAttributeValue(attribute, value ? LDAP_BOOLEAN_TRUE : LDAP_BOOLEAN_FALSE);
    }

    /**
     * Checks if the <code>objectclass</code> Attribute of the DirContext contains the given objectclass
     *
     * @param ctx         the DirContextAdaper to check
     * @param objectclass the objectclass value to look for (case does not matter)
     * @return <code>true</code>, if the DirContext contains the objectclass, otherwise <code>false</code>
     */
    public static boolean containsObjectClass(final DirContextAdapter ctx, final String objectclass) {
        final String[] objectclasses = ctx.getStringAttributes(OBJECTCLASS_ATTRIBUTE);
        if (objectclasses == null || objectclasses.length == 0)
            return false;
        for (int i = 0; i < objectclasses.length; i++) {
            final String ocls = objectclasses[i];
            if (ocls.equalsIgnoreCase(objectclass))
                return true;
        }
        return false;
    }
}
