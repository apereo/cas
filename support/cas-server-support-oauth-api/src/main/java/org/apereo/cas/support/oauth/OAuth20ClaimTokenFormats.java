package org.apereo.cas.support.oauth;

import lombok.Getter;

/**
 * This is {@link OAuth20ClaimTokenFormats}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
public enum OAuth20ClaimTokenFormats {

    JWT("urn:ietf:params:oauth:token-type:jwt"),
    IDTOKEN("http://openid.net/specs/openid-connect-core-1_0.html#IDToken");

    private final String type;

    OAuth20ClaimTokenFormats(final String type) {
        this.type = type;
    }
}
