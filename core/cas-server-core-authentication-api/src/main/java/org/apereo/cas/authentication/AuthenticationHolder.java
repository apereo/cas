package org.apereo.cas.authentication;

import lombok.experimental.UtilityClass;

/**
 * This is {@link AuthenticationHolder}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@UtilityClass
public final class AuthenticationHolder {
    private static final ThreadLocal<Authentication> AUTHENTICATION_HOLDER = new InheritableThreadLocal<>();

    /**
     * Sets authentication.
     *
     * @param authentication the authentication
     */
    public static void setCurrentAuthentication(final Authentication authentication) {
        AUTHENTICATION_HOLDER.set(authentication);
    }

    /**
     * Gets authentication.
     *
     * @return the authentication
     */
    public static Authentication getCurrentAuthentication() {
        return AUTHENTICATION_HOLDER.get();
    }

    /**
     * Clear.
     */
    public static void clear() {
        AUTHENTICATION_HOLDER.remove();
    }
}
