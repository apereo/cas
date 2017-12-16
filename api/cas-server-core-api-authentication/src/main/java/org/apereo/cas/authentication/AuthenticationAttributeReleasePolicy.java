package org.apereo.cas.authentication;

import java.util.Map;

/**
 * This component is used to handle release of authentication attributes in validation responses.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@FunctionalInterface
public interface AuthenticationAttributeReleasePolicy {
    /**
     * This method will return the Authentication attributes that should be released.
     *
     * @param authentication The authentication object we are processing.
     * @return The attributes to be released
     */
    Map<String, Object> getAuthenticationAttributesForRelease(Authentication authentication);
}
