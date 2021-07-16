package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Controller
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseOAuth20Controller<T extends OAuth20ConfigurationContext> {
    private final T configurationContext;

    /**
     * Extract access token from token.
     *
     * @param token the token
     * @return the string
     */
    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(getConfigurationContext().getAccessTokenJwtBuilder())
            .build()
            .decode(token);
    }
}
