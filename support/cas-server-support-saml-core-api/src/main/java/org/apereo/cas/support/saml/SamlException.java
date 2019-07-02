package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.RootCasException;

import java.util.List;

/**
 * Represents the root SAML exception.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlException extends RootCasException {
    /**
     * Code description.
     */
    public static final String CODE = "UNSATISFIED_SAML_REQUEST";

    private static final long serialVersionUID = 801270467754480446L;

    public SamlException(final String msg) {
        super(CODE, msg);
    }

    public SamlException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }

    public SamlException(final String code, final String msg, final List<Object> args) {
        super(code, msg, args);
    }

    public SamlException(final String code, final Throwable throwable, final List<Object> args) {
        super(code, throwable, args);
    }
}
