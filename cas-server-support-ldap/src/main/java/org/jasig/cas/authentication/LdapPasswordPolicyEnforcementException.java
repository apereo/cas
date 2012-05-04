/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

public class LdapPasswordPolicyEnforcementException extends LdapAuthenticationException {

    public static final String CODE_PASSWORD_CHANGE  = "screen.accounterror.password.message";

    private static final long  serialVersionUID      = 4365292208441435202L;

    public LdapPasswordPolicyEnforcementException(final String msg) {
        this(CODE_PASSWORD_CHANGE, msg);
    }

    public LdapPasswordPolicyEnforcementException(final String code, final String msg) {
        super(code, msg);
    }

}
