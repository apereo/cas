package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.authentication.RootCasException;

/**
 * This is {@link DuoSecurityException}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class DuoSecurityException extends RootCasException {
    @Serial
    private static final long serialVersionUID = 2334364364364364364L;

    public DuoSecurityException(final String message) {
        super(message);
    }
}
