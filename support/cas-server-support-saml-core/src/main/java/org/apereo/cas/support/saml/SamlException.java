package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.RootCasException;

/**
 * Represents the root SAML exception.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlException extends RootCasException {
    /** Code description. */
    public static final String CODE = "UNSATISFIED_SAML_REQUEST";

    private static final long serialVersionUID = 801270467754480446L;
    
    /**
     * Instantiates a new Saml exception.
     *
     * @param msg  the msg
     */
    public SamlException(final String msg) {
        super(CODE, msg);
    }

    /**
     * Instantiates a new Saml exception.
     *
     * @param msg      the msg
     * @param throwable the throwable
     */
    public SamlException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }
}
