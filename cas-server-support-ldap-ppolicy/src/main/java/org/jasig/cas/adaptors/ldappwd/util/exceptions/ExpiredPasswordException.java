package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

public final class ExpiredPasswordException extends AuthenticationException {

    private static final long serialVersionUID = 1001151255006510917L;

    /**
     * Spring uses this code to map the locale specific error message from properties files in WEB-INF/classes/.
     */
    public static final String EXPIRED_PASSWORD_CODE = "error.authentication.password.expired";

    public static final String EXPIRED_PASSWORD_ERROR_REGEX = "\\D532\\D|expired|\\D773\\D";

    public ExpiredPasswordException() {
        super(ExpiredPasswordException.EXPIRED_PASSWORD_CODE);
    }

    public ExpiredPasswordException(final String code) {
        super(code);
    }
}
