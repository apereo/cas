package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

public final class MustChangePasswordException extends AuthenticationException {

    private static final long  serialVersionUID = -2162234756901030153L;

    /**
     * Spring uses this code to map the locale specific error message from properties files in WEB-INF/classes/
     */
    public static final String MUST_CHANGE_PASSWORD_CODE = "error.authentication.password.mustchange";

    /**
     * Regex to match Active directory error details
     */
    public static final String MUST_CHANGE_PASSWORD_ERROR_REGEX = "\\D773\\D";

    public MustChangePasswordException() {

        super(MustChangePasswordException.MUST_CHANGE_PASSWORD_CODE);
    }

    public MustChangePasswordException(final String code) {

        super(code);
    }

}
