package org.apereo.cas.support.oauth.session;

import org.apereo.cas.authentication.RootCasException;

/**
 * This is {@link OAuth20SessionStoreMismatchException}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20SessionStoreMismatchException extends RootCasException {
    private static final long serialVersionUID = -893364906643103618L;

    private static final String CODE = "OAUTH_BAD_SESSION_REQUEST";

    public OAuth20SessionStoreMismatchException(final String msg) {
        super(CODE, msg);
    }
}
