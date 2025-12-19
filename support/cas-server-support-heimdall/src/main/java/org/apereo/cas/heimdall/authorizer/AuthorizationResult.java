package org.apereo.cas.heimdall.authorizer;
import module java.base;

/**
 * This is {@link AuthorizationResult}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public record AuthorizationResult(boolean authorized, String reason) {

    /**
     * Denied authorization result.
     *
     * @param reason the reason
     * @return the authorization result
     */
    public static AuthorizationResult denied(final String reason) {
        return new AuthorizationResult(false, reason);
    }

    /**
     * Granted authorization result.
     *
     * @param reason the reason
     * @return the authorization result
     */
    public static AuthorizationResult granted(final String reason) {
        return new AuthorizationResult(true, reason);
    }

    /**
     * From boolean result.
     *
     * @param allowed the allowed
     * @return the authorization result
     */
    public static AuthorizationResult from(final Boolean allowed) {
        return allowed ? granted("OK") : denied("Denied");
    }
}
