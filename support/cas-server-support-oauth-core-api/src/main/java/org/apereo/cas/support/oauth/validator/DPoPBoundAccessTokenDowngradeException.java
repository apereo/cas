package org.apereo.cas.support.oauth.validator;

import module java.base;

/**
 * This is {@link DPoPBoundAccessTokenDowngradeException}, thrown when a DPoP-bound access token reaches a protected
 * resource other than in the {@code DPoP} authorization header: as a bearer token in the {@code Authorization} header,
 * the form body or the query (RFC 9449, section 7.2).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class DPoPBoundAccessTokenDowngradeException extends Exception {
    @Serial
    private static final long serialVersionUID = 3361972905417812734L;

    public DPoPBoundAccessTokenDowngradeException(final String message) {
        super(message);
    }
}
