package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.util.NamedObject;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface OidcWebFingerUserInfoRepository extends NamedObject {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcWebFingerUserInfoRepository";

    /**
     * Find by email address.
     *
     * @param email the email
     * @return the map
     */
    default Map<String, Object> findByEmailAddress(final String email) throws Throwable {
        return new HashMap<>();
    }

    /**
     * Find by username.
     *
     * @param username the username
     * @return the map
     */
    default Map<String, Object> findByUsername(final String username) throws Throwable {
        return new HashMap<>();
    }
}
