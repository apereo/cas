package org.jasig.cas.mfa.exceptions;

/**
 * Multifactor authentication exception that is thrown
 * when the request authentication method cannot be accepted/provided
 * by this CAS server.
 * @author Misagh Moayyed
 */
public class UnacceptableMfaMethodException extends AbstractMfaTicketValidationException {
    private static final long serialVersionUID = -8544747236126342213L;

    /**
     * Constructor to spin up the exception instance.
     * @param code  error code
     * @param msg error message
     * @param authnMethod authentication method associated with the error
     */
    public UnacceptableMfaMethodException(final String code, final String msg, final String authnMethod) {
        super(code, msg, authnMethod);
    }
}
