package org.apereo.cas.oidc.discovery.webfinger;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface OidcWebFingerUserInfoRepository {

    /**
     * Find by email address.
     *
     * @param email the email
     * @return the map
     */
    default Map<String, Object> findByEmailAddress(final String email) {
        return new HashMap<>(0);
    }

    /**
     * Find by username.
     *
     * @param username the username
     * @return the map
     */
    default Map<String, Object> findByUsername(final String username) {
        return new HashMap<>(0);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
