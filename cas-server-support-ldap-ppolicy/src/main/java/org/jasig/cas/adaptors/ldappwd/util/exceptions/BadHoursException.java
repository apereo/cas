package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Exception thrown if a user attempts to connect within unauthorized hours
 * 
 * @author Philippe MARASSE
 */
public final class BadHoursException extends AuthenticationException {

    private static final long  serialVersionUID = -3991751972698211478L;

    public static final String BAD_HOURS_CODE = "error.authentication.account.badhours";

    /**
     * Regex to match Active directory error details 
     */
    public static final String BAD_HOURS_ERROR_REGEX = "\\D530\\D";

    public BadHoursException() {

        super(BadHoursException.BAD_HOURS_CODE);
    }

    public BadHoursException(final String code) {

        super(code);
    }

}
