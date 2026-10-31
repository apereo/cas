package org.apereo.cas.support.oauth.validator.token;

import module java.base;

/**
 * Thrown when a request names a resource indicator that is invalid, or that the client is not allowed to request
 * (RFC 8707, section 2), reported to the client as {@code invalid_target}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20InvalidTargetException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4123581247729851340L;

    public OAuth20InvalidTargetException(final String message) {
        super(message);
    }
}
