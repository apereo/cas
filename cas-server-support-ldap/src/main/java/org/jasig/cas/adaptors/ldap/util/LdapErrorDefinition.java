/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LdapErrorDefinition {

    private Pattern ldapPattern = null;

    private String type  = null;

    public String getType() {
        return this.type;
    }

    public boolean matches(final String msg) {
        final Matcher matcher = getLdapPattern().matcher(msg);
        return matcher.find();
    }

    public void setLdapPattern(final String ldapPattern) {
        this.ldapPattern = Pattern.compile(ldapPattern);
    }

    public void setType(final String errMessage) {
        this.type = errMessage;
    }

    private Pattern getLdapPattern() {
        return this.ldapPattern;
    }

}
