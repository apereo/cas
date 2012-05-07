package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.AuthenticationException;

public class LdapAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public LdapAuthenticationException(final String code, final String msg) {
        super(code, msg);
    }

    public LdapAuthenticationException(final String code, final String msg, final String type) {
        super(code, msg, type);
    }
}
