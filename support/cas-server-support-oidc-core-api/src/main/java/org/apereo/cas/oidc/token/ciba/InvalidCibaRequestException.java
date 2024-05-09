package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.authentication.RootCasException;
import java.io.Serial;

/**
 * This is {@link InvalidCibaRequestException}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class InvalidCibaRequestException extends RootCasException {
    @Serial
    private static final long serialVersionUID = 4271789506297566481L;

    private static final String CODE = "OIDC_CIBA_BAD_REQUEST";

    protected InvalidCibaRequestException(final String msg) {
        super(CODE, msg);
    }
}
