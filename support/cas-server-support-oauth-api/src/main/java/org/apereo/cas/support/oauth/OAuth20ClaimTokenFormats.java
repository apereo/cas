package org.apereo.cas.support.oauth;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link OAuth20ClaimTokenFormats}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuth20ClaimTokenFormats {

    /**
     * JWT token format.
     */
    JWT("urn:ietf:params:oauth:token-type:jwt"),
    /**
     * IDToken token format.
     */
    IDTOKEN("http://openid.net/specs/openid-connect-core-1_0.html#IDToken");

    private final String type;
}
