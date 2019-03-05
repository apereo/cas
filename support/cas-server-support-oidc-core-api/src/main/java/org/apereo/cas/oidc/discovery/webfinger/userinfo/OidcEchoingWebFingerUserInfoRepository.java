package org.apereo.cas.oidc.discovery.webfinger.userinfo;

import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepository;
import org.apereo.cas.util.CollectionUtils;

import java.util.Map;

/**
 * This is {@link OidcEchoingWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OidcEchoingWebFingerUserInfoRepository implements OidcWebFingerUserInfoRepository {
    @Override
    public Map<String, Object> findByEmailAddress(final String email) {
        return CollectionUtils.wrap("email", email);
    }

    @Override
    public Map<String, Object> findByUsername(final String username) {
        return CollectionUtils.wrap("username", username);
    }
}
