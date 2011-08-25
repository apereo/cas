package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

public final class BadWorkstationException extends AuthenticationException {

    private static final long  serialVersionUID = -2064329139223133602L;

    /**
     * Spring uses this code to map the locale specific error message from the messages properties files in WEB-INF/classes/.
     */
    public static final String BAD_WORKSTATION_CODE = "error.authentication.account.badworkstation";

    /**
     * Regex to match Active directory error details 
     */
    public static final String BAD_WORKSTATION_ERROR_REGEX = "\\D531\\D";

    public BadWorkstationException() {

        super(BadWorkstationException.BAD_WORKSTATION_CODE);
    }

    public BadWorkstationException(final String code) {

        super(code);
    }

}
