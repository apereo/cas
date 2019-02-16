package org.apereo.cas.oidc.discovery.webfinger;

import java.util.Map;

/**
 * This is {@link OidcWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface OidcWebFingerUserInfoRepository {

    Map<String, Object> findByEmailAddress(String email);

    Map<String, Object> findByUsername(String username);
}
